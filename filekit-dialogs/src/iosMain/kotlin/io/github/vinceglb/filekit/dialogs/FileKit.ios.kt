package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.util.CameraControllerDelegate
import io.github.vinceglb.filekit.dialogs.util.DocumentPickerDelegate
import io.github.vinceglb.filekit.dialogs.util.PhPickerDelegate
import io.github.vinceglb.filekit.dialogs.util.PhPickerDismissDelegate
import io.github.vinceglb.filekit.path
import io.github.vinceglb.filekit.startAccessingSecurityScopedResource
import io.github.vinceglb.filekit.stopAccessingSecurityScopedResource
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Foundation.temporaryDirectory
import platform.Foundation.writeToURL
import platform.Photos.PHPhotoLibrary.Companion.sharedPhotoLibrary
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerConfigurationAssetRepresentationModeAutomatic
import platform.PhotosUI.PHPickerConfigurationAssetRepresentationModeCompatible
import platform.PhotosUI.PHPickerConfigurationAssetRepresentationModeCurrent
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIDevice
import platform.UIKit.UIDocumentInteractionController
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerCameraDevice
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UISceneActivationStateForegroundActive
import platform.UIKit.UIUserInterfaceIdiomPad
import platform.UIKit.UIViewController
import platform.UIKit.UIWindowScene
import platform.UIKit.popoverPresentationController
import platform.UIKit.presentationController
import platform.UniformTypeIdentifiers.UTType
import platform.UniformTypeIdentifiers.UTTypeContent
import platform.UniformTypeIdentifiers.UTTypeFolder
import platform.UniformTypeIdentifiers.UTTypeImage
import platform.UniformTypeIdentifiers.UTTypeItem
import platform.UniformTypeIdentifiers.UTTypeMovie
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private object FileKitDialog {
    // Keep every active delegate strongly referenced without restricting independent suspend callers.
    val documentPickerSessions = IosDialogSessionRegistry<DocumentPickerDelegate>()
    val photoPickerSessions = IosDialogSessionRegistry<PhotoPickerSession>()
    val cameraPickerSessions = IosDialogSessionRegistry<CameraControllerDelegate>()
}

private class PhotoPickerSession(
    val pickerDelegate: PhPickerDelegate,
    val dismissDelegate: PhPickerDismissDelegate,
)

internal actual suspend fun FileKit.platformOpenFilePicker(
    type: FileKitType,
    mode: PickerMode,
    directory: PlatformFile?,
    dialogSettings: FileKitDialogSettings,
): Flow<FileKitPickerState<List<PlatformFile>>> = when (type) {
    // Use PHPickerViewController for images and videos
    is FileKitType.Image,
    is FileKitType.Video,
    is FileKitType.ImageAndVideo,
    -> callPhPicker(
        mode = mode,
        type = type,
        dialogSettings = dialogSettings,
    )

    // Use UIDocumentPickerViewController for other types
    else -> flow {
        val picked = callPicker(
            mode = when (mode) {
                is PickerMode.Single -> Mode.Single
                is PickerMode.Multiple -> Mode.Multiple
            },
            contentTypes = type.contentTypes,
            directory = directory,
            dialogSettings = dialogSettings,
            missingPresenterFailure = {
                FileKitPickerException("No view controller is available to present the file picker.")
            },
        )?.map { PlatformFile(it) }

        if (picked.isNullOrEmpty()) {
            emit(FileKitPickerState.Cancelled)
        } else {
            emit(FileKitPickerState.Completed(picked))
        }
    }
}

/**
 * Opens a directory picker dialog.
 *
 * @param directory The initial directory. Supported on desktop platforms.
 * @param dialogSettings Platform-specific settings for the dialog.
 * @return The picked directory as a [PlatformFile], or null if canceled.
 */
public actual suspend fun FileKit.openDirectoryPicker(
    directory: PlatformFile?,
    dialogSettings: FileKitDialogSettings,
): PlatformFile? = callPicker(
    mode = Mode.Directory,
    contentTypes = listOf(UTTypeFolder),
    directory = directory,
    dialogSettings = dialogSettings,
    missingPresenterFailure = {
        FileKitDialogException("No view controller is available to present the directory picker.")
    },
)?.firstOrNull()?.let { PlatformFile(it) }

/**
 * Opens a file saver dialog.
 *
 * @param suggestedName The suggested name for the file.
 * @param defaultExtension The default file extension without the dot.
 * @param allowedExtensions Allowed file extensions for the native save dialog. Ignored on iOS.
 * @param directory The initial directory. Supported on desktop platforms.
 * @param dialogSettings Platform-specific settings for the dialog.
 * @return The path where the file should be saved as a [PlatformFile], or null if canceled.
 */
@OptIn(ExperimentalForeignApi::class)
internal actual suspend fun FileKit.platformOpenFileSaver(
    suggestedName: String,
    defaultExtension: String?,
    allowedExtensions: Set<String>?,
    directory: PlatformFile?,
    dialogSettings: FileKitDialogSettings,
): PlatformFile? = withContext(Dispatchers.Main) {
    val presenter = dialogSettings.presenterViewController(
        failure = { FileKitDialogException("No view controller is available to present the file saver.") },
    )

    // the suggestedName cannot include "/" because the OS interprets it as a directory separator.
    // However, "Files" renders ":" as "/", so we can just use ":" and the user will see "/".
    val sanitizedSuggestedName = suggestedName.replace("/", ":")
    val normalizedDefaultExtension = normalizeFileSaverExtension(defaultExtension)
    val fileName = buildFileSaverSuggestedName(
        suggestedName = sanitizedSuggestedName,
        extension = normalizedDefaultExtension,
    )

    val fileManager = NSFileManager.defaultManager
    val fileComponents = requireIosFileSaverTemporaryValue(
        fileManager.temporaryDirectory.pathComponents?.plus(fileName),
        description = "temporary directory path",
    )
    val fileUrl = requireIosFileSaverTemporaryValue(
        NSURL.fileURLWithPathComponents(fileComponents),
        description = "temporary file URL",
    )

    val emptyData = NSData()
    requireIosFileSaverWrite(emptyData.writeToURL(fileUrl, true))

    suspendCancellableCoroutine { continuation ->
        val pickerController = UIDocumentPickerViewController(
            forExportingURLs = listOf(fileUrl),
        )
        directory?.let { pickerController.directoryURL = NSURL.fileURLWithPath(it.path) }

        lateinit var delegate: DocumentPickerDelegate
        lateinit var session: IosDialogContinuationSession<DocumentPickerDelegate, PlatformFile?>

        delegate = DocumentPickerDelegate(
            onFilesPicked = { urls ->
                val file = urls.firstOrNull()?.let { nsUrl ->
                    // UIDocumentPickerViewController(forExportingURLs) creates an empty file at the
                    // specified URL. We remove it to keep consistency with the other platforms.
                    nsUrl.startAccessingSecurityScopedResource()
                    NSFileManager.defaultManager.removeItemAtURL(nsUrl, null)
                    nsUrl.stopAccessingSecurityScopedResource()

                    // Return the file as a PlatformFile
                    PlatformFile(nsUrl)
                }
                session.complete(file)
            },
            onPickerCancelled = {
                session.complete(null)
            },
        )
        session = createIosPresentedDialogSession(
            session = delegate,
            registry = FileKitDialog.documentPickerSessions,
            continuation = continuation,
            dismiss = pickerController::dismissOnCancellation,
        )
        session.present {
            // Assign the delegate to the picker controller
            pickerController.delegate = delegate

            // Present the picker controller
            presenter.presentViewController(
                pickerController,
                animated = true,
                completion = null,
            )
        }
    }
}

/**
 * Opens a camera picker dialog.
 *
 * @param type The type of media to capture (Image or Video).
 * @param cameraFacing The camera facing (System, Back or Front).
 * @param destinationFile The file where the captured media will be saved.
 * @param openCameraSettings Platform-specific settings for the camera.
 * @return The saved file as a [PlatformFile], or null if canceled.
 */
public actual suspend fun FileKit.openCameraPicker(
    type: FileKitCameraType,
    cameraFacing: FileKitCameraFacing,
    destinationFile: PlatformFile,
    openCameraSettings: FileKitOpenCameraSettings,
): PlatformFile? {
    val image = withContext(Dispatchers.Main) {
        val presenter = openCameraSettings.presenterViewController()

        suspendCancellableCoroutine<UIImage?> { continuation ->
            lateinit var delegate: CameraControllerDelegate
            lateinit var session: IosDialogContinuationSession<CameraControllerDelegate, UIImage?>
            val pickerController = UIImagePickerController()
            pickerController.sourceType =
                UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera

            delegate = CameraControllerDelegate(
                onImagePicked = { image ->
                    session.complete(image)
                },
            )
            session = createIosPresentedDialogSession(
                session = delegate,
                registry = FileKitDialog.cameraPickerSessions,
                continuation = continuation,
                dismiss = pickerController::dismissOnCancellation,
            )
            session.present {
                pickerController.delegate = delegate

                when (cameraFacing) {
                    FileKitCameraFacing.Front -> {
                        pickerController.cameraDevice =
                            UIImagePickerControllerCameraDevice.UIImagePickerControllerCameraDeviceFront
                    }

                    FileKitCameraFacing.Back -> {
                        pickerController.cameraDevice =
                            UIImagePickerControllerCameraDevice.UIImagePickerControllerCameraDeviceRear
                    }

                    FileKitCameraFacing.System -> {}
                }

                presenter.presentViewController(
                    pickerController,
                    animated = true,
                    completion = null,
                )
            }
        }
    } ?: return null

    // Encode and write off the main thread: JPEG encoding a full-resolution photo
    // at quality 1.0 is expensive and used to freeze the UI right after the capture
    return withContext(Dispatchers.IO) {
        // Convert UIImage to NSData (JPEG format with compression quality 1.0)
        val imageData = UIImageJPEGRepresentation(image, 1.0)

        // Create an NSURL for the file path
        val fileUrl = NSURL.fileURLWithPath(destinationFile.path)

        // Write the NSData to the file, returning the saved file on success.
        requireIosCameraWrite(imageData?.writeToURL(fileUrl, true) == true)
        destinationFile
    }
}

/**
 * Shares a file using the iOS share sheet.
 *
 * @param file The file to share.
 * @param shareSettings Platform-specific settings for sharing.
 */
@OptIn(ExperimentalForeignApi::class)
public actual suspend fun FileKit.shareFile(
    file: PlatformFile,
    shareSettings: FileKitShareSettings,
) {
    shareFile(
        files = listOf(file),
        shareSettings = shareSettings,
    )
}

/**
 * Shares multiple files using the iOS share sheet.
 *
 * @param files The list of files to share.
 * @param shareSettings Platform-specific settings for sharing.
 */
@OptIn(ExperimentalForeignApi::class)
public actual suspend fun FileKit.shareFile(
    files: List<PlatformFile>,
    shareSettings: FileKitShareSettings,
) {
    if (files.isEmpty()) return

    val viewController = shareSettings.presenterViewController()

    files.forEach { it.startAccessingSecurityScopedResource() }
    // Ensure we always pass a file URL to the activity items; otherwise iOS may treat the
    // provided value as plain text and share the path string instead of the actual file.
    val activityItems = files.map { NSURL.fileURLWithPath(it.path) }

    val shareVC = UIActivityViewController(activityItems, null)

    if (isIpad()) {
        // iPad need sourceView for show
        shareVC.popoverPresentationController?.apply {
            sourceView = viewController.view
            sourceRect = viewController.view.center.useContents { CGRectMake(x, y, 0.0, 0.0) }
            permittedArrowDirections = 0uL
        }
    }

    shareSettings.addOptionUIActivityViewController(shareVC)

    shareVC.setCompletionWithItemsHandler { _, _, _, _ ->
        files.forEach { it.stopAccessingSecurityScopedResource() }
    }

    viewController.presentViewController(
        viewControllerToPresent = shareVC,
        animated = true,
        completion = null,
    )
}

/**
 * Opens a file with the default application associated with its file type.
 *
 * @param file The file to open.
 * @param openFileSettings Platform-specific settings for opening the file.
 */
@OptIn(ExperimentalForeignApi::class)
public actual fun FileKit.openFileWithDefaultApplication(
    file: PlatformFile,
    openFileSettings: FileKitOpenFileSettings,
) {
    // Try to open with the system's default app first
    val opened = UIApplication.sharedApplication.openURL(file.nsUrl)

    // If that fails, fall back to document interaction controller
    if (!opened) {
        val documentController = UIDocumentInteractionController()
        documentController.URL = file.nsUrl

        // Get the root view controller from the key window
        val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController

        if (rootViewController != null) {
            // Present the options menu to let user choose how to open
            documentController.presentOptionsMenuFromRect(
                rect = rootViewController.view.bounds,
                inView = rootViewController.view,
                animated = true,
            )
        }
    }
}

private fun isIpad(): Boolean {
    val device = UIDevice.currentDevice
    return device.userInterfaceIdiom == UIUserInterfaceIdiomPad
}

private fun FileKitDialogSettings.presenterViewController(
    failure: () -> FileKitDialogException,
): UIViewController = resolveIosDialogPresenter(
    configuredPresenter = presenter,
    fallbackPresenter = { UIApplication.sharedApplication.topMostViewController() },
    failure = failure,
)

private fun FileKitOpenCameraSettings.presenterViewController(): UIViewController = resolveIosDialogPresenter(
    configuredPresenter = presenter,
    fallbackPresenter = { UIApplication.sharedApplication.topMostViewController() },
    failure = { FileKitDialogException("No view controller is available to present the camera picker.") },
)

private fun FileKitShareSettings.presenterViewController(): UIViewController = resolveIosDialogPresenter(
    configuredPresenter = presenter,
    fallbackPresenter = { UIApplication.sharedApplication.topMostViewController() },
    failure = { FileKitDialogException("No view controller is available to present the share sheet.") },
)

private fun UIViewController.dismissOnCancellation(finishDismissal: () -> Unit) {
    dispatch_async(dispatch_get_main_queue()) {
        dismissViewControllerAnimated(true) {
            finishDismissal()
        }
    }
}

private suspend fun callPicker(
    mode: Mode,
    contentTypes: List<UTType>,
    directory: PlatformFile?,
    dialogSettings: FileKitDialogSettings,
    missingPresenterFailure: () -> FileKitDialogException,
): List<NSURL>? = withContext(Dispatchers.Main) {
    val presenter = dialogSettings.presenterViewController(missingPresenterFailure)

    suspendCancellableCoroutine { continuation ->
        val pickerController = UIDocumentPickerViewController(forOpeningContentTypes = contentTypes)
        directory?.let { pickerController.directoryURL = NSURL.fileURLWithPath(it.path) }
        pickerController.allowsMultipleSelection = mode == Mode.Multiple

        lateinit var delegate: DocumentPickerDelegate
        lateinit var session: IosDialogContinuationSession<DocumentPickerDelegate, List<NSURL>?>

        delegate = DocumentPickerDelegate(
            onFilesPicked = { urls -> session.complete(urls) },
            onPickerCancelled = { session.complete(null) },
        )
        session = createIosPresentedDialogSession(
            session = delegate,
            registry = FileKitDialog.documentPickerSessions,
            continuation = continuation,
            dismiss = pickerController::dismissOnCancellation,
        )
        session.present {
            // Assign the delegate to the picker controller
            pickerController.delegate = delegate

            // Present the picker controller
            presenter.presentViewController(
                pickerController,
                animated = true,
                completion = null,
            )
        }
    }
}

private suspend fun getPhPickerResults(
    mode: PickerMode,
    type: FileKitType,
    dialogSettings: FileKitDialogSettings,
): List<PHPickerResult> {
    val presenter = dialogSettings.presenterViewController(
        failure = { FileKitPickerException("No view controller is available to present the photo picker.") },
    )

    return suspendCancellableCoroutine { continuation ->
        val configuration = PHPickerConfiguration(sharedPhotoLibrary())
        configuration.selectionLimit = when (mode) {
            is PickerMode.Multiple -> mode.maxItems?.toLong() ?: 0
            PickerMode.Single -> 1
        }
        configuration.preferredAssetRepresentationMode = when (dialogSettings.assetRepresentationMode) {
            FileKitAssetRepresentationMode.Automatic -> PHPickerConfigurationAssetRepresentationModeAutomatic
            FileKitAssetRepresentationMode.Current -> PHPickerConfigurationAssetRepresentationModeCurrent
            FileKitAssetRepresentationMode.Compatible -> PHPickerConfigurationAssetRepresentationModeCompatible
        }
        configuration.filter = when (type) {
            is FileKitType.Image -> PHPickerFilter.imagesFilter

            is FileKitType.Video -> PHPickerFilter.videosFilter

            is FileKitType.ImageAndVideo -> PHPickerFilter.anyFilterMatchingSubfilters(
                listOf(
                    PHPickerFilter.imagesFilter,
                    PHPickerFilter.videosFilter,
                ),
            )

            else -> throw IllegalArgumentException("Unsupported type: $type")
        }

        lateinit var pickerDelegate: PhPickerDelegate
        lateinit var dismissDelegate: PhPickerDismissDelegate
        lateinit var pickerSession: PhotoPickerSession
        lateinit var continuationSession: IosDialogContinuationSession<PhotoPickerSession, List<PHPickerResult>>

        pickerDelegate = PhPickerDelegate(onFilesPicked = { continuationSession.complete(it) })
        dismissDelegate = PhPickerDismissDelegate(onFilesPicked = { continuationSession.complete(it) })
        pickerSession = PhotoPickerSession(pickerDelegate, dismissDelegate)
        val controller = PHPickerViewController(configuration = configuration)
        controller.delegate = pickerDelegate
        controller.presentationController?.delegate = dismissDelegate

        continuationSession = createIosPresentedDialogSession(
            session = pickerSession,
            registry = FileKitDialog.photoPickerSessions,
            continuation = continuation,
            dismiss = controller::dismissOnCancellation,
        )
        continuationSession.present {
            // Present the picker controller
            presenter.presentViewController(
                controller,
                animated = true,
                completion = null,
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun callPhPicker(
    mode: PickerMode,
    type: FileKitType,
    dialogSettings: FileKitDialogSettings,
): Flow<FileKitPickerState<List<PlatformFile>>> = channelFlow {
    // Fetch picker results on Main
    val pickerResults = withContext(Dispatchers.Main) {
        getPhPickerResults(mode, type, dialogSettings)
    }

    if (pickerResults.isEmpty()) {
        send(FileKitPickerState.Cancelled)
        return@channelFlow
    }

    send(FileKitPickerState.Started(pickerResults.size))

    val fileManager = NSFileManager.defaultManager
    val tempRoot = fileManager.temporaryDirectory
        .URLByAppendingPathComponent(NSUUID().UUIDString)
        ?: throw FileKitPickerException("Failed to create a temporary directory for the selected files.")
    fileManager.createDirectoryAtURL(
        url = tempRoot,
        withIntermediateDirectories = true,
        attributes = null,
        error = null,
    )

    // Pre-allocated array to preserve selection order
    val orderedFiles = arrayOfNulls<PlatformFile>(pickerResults.size)
    val lock = Mutex()
    var failure: FileKitPickerException? = null

    // Launch a child coroutine for every copy, preserving the index
    pickerResults
        .mapIndexed { index, result ->
            launch(Dispatchers.IO) {
                try {
                    val src = suspendCancellableCoroutine<NSURL> { cont ->
                        result.itemProvider.loadFileRepresentationForTypeIdentifier(
                            when (type) {
                                is FileKitType.Image -> UTTypeImage.identifier
                                is FileKitType.Video -> UTTypeMovie.identifier
                                is FileKitType.ImageAndVideo -> UTTypeContent.identifier
                                else -> error("Unsupported type $type")
                            },
                        ) { url, error ->
                            when {
                                error != null -> {
                                    cont.resumeWithException(
                                        FileKitPickerException(
                                            message = error.localizedDescription,
                                        ),
                                    )
                                }

                                url == null -> {
                                    cont.resumeWithException(
                                        FileKitPickerException("The selected file could not be resolved."),
                                    )
                                }

                                else -> {
                                    // Must copy the URL here because it becomes invalid outside the loadFileRepresentationForTypeIdentifier callback scope
                                    runCatching {
                                        copyToTempFile(fileManager, url, tempRoot.lastPathComponent!!)
                                    }.onSuccess(cont::resume)
                                        .onFailure { cont.resumeWithException(it) }
                                }
                            }
                        }
                    }

                    lock.withLock {
                        if (failure != null) return@launch

                        // Insert at the original index to preserve selection order
                        orderedFiles[index] = PlatformFile(src)
                        send(FileKitPickerState.Progress(orderedFiles.filterNotNull(), pickerResults.size))
                    }
                } catch (pickerFailure: FileKitPickerException) {
                    lock.withLock {
                        if (failure == null) {
                            failure = pickerFailure
                        }
                    }
                }
            }
        }.joinAll()

    failure?.let {
        send(FileKitPickerState.Failed(it))
        return@channelFlow
    }

    val files = orderedFiles.filterNotNull()
    when {
        files.isEmpty() -> send(FileKitPickerState.Cancelled)
        else -> send(FileKitPickerState.Completed(files))
    }
}

private val FileKitType.contentTypes: List<UTType>
    get() = when (this) {
        is FileKitType.Image -> {
            listOf(UTTypeImage)
        }

        is FileKitType.Video -> {
            listOf(UTTypeMovie)
        }

        is FileKitType.ImageAndVideo -> {
            listOf(UTTypeImage, UTTypeMovie)
        }

        is FileKitType.File -> {
            extensions
                ?.mapNotNull { UTType.typeWithFilenameExtension(it) }
                .ifNullOrEmpty { listOf(UTTypeItem) }
        }
    }

private fun <R> List<R>?.ifNullOrEmpty(block: () -> List<R>): List<R> =
    if (this.isNullOrEmpty()) block() else this

@OptIn(ExperimentalForeignApi::class)
private fun copyToTempFile(
    fileManager: NSFileManager,
    url: NSURL,
    id: String,
): NSURL {
    // Get the temporary directory
    val fileComponents = fileManager.temporaryDirectory.pathComponents
        ?.plus(id)
        ?.plus(url.lastPathComponent)
        ?: throw FileKitPickerException("Failed to get the temporary directory for the selected file.")

    // Create a file URL
    val fileUrl = NSURL.fileURLWithPathComponents(fileComponents)
        ?: throw FileKitPickerException("Failed to create the temporary URL for the selected file.")

    // Write the data to the file URL
    val didCopy = fileManager.copyItemAtURL(
        srcURL = url,
        toURL = fileUrl,
        error = null,
    )
    if (!didCopy) {
        throw FileKitPickerException("Failed to copy the selected file to a temporary location.")
    }

    return fileUrl
}

private fun UIApplication.topMostViewController(): UIViewController? {
    val keyWindow = this.connectedScenes
        .filterIsInstance<UIWindowScene>()
        .firstOrNull { it.activationState == UISceneActivationStateForegroundActive }
        ?.keyWindow

    var topController = keyWindow?.rootViewController
    while (topController?.presentedViewController != null) {
        topController = topController.presentedViewController
    }

    return topController
}

private enum class Mode {
    Single,
    Multiple,
    Directory,
}
