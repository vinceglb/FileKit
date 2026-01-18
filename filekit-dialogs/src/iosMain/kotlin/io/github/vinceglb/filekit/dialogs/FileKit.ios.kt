package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialog.cameraControllerDelegate
import io.github.vinceglb.filekit.dialogs.FileKitDialog.documentPickerDelegate
import io.github.vinceglb.filekit.dialogs.FileKitDialog.phPickerDelegate
import io.github.vinceglb.filekit.dialogs.FileKitDialog.phPickerDismissDelegate
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
import platform.PhotosUI.PHPickerConfigurationAssetRepresentationModeCurrent
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIDevice
import platform.UIKit.UIDocumentInteractionController
import platform.UIKit.UIDocumentPickerViewController
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
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private object FileKitDialog {
    // Create a reference to the picker delegate to prevent it from being garbage collected
    lateinit var documentPickerDelegate: DocumentPickerDelegate
    lateinit var phPickerDelegate: PhPickerDelegate
    lateinit var phPickerDismissDelegate: PhPickerDismissDelegate
    lateinit var cameraControllerDelegate: CameraControllerDelegate
}

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
 * @return The picked directory as a [PlatformFile], or null if cancelled.
 */
public actual suspend fun FileKit.openDirectoryPicker(
    directory: PlatformFile?,
    dialogSettings: FileKitDialogSettings,
): PlatformFile? = callPicker(
    mode = Mode.Directory,
    contentTypes = listOf(UTTypeFolder),
    directory = directory,
)?.firstOrNull()?.let { PlatformFile(it) }

/**
 * Opens a file saver dialog.
 *
 * @param suggestedName The suggested name for the file.
 * @param extension The file extension (optional).
 * @param directory The initial directory. Supported on desktop platforms.
 * @param dialogSettings Platform-specific settings for the dialog.
 * @return The path where the file should be saved as a [PlatformFile], or null if cancelled.
 */
@OptIn(ExperimentalForeignApi::class)
public actual suspend fun FileKit.openFileSaver(
    suggestedName: String,
    extension: String?,
    directory: PlatformFile?,
    dialogSettings: FileKitDialogSettings,
): PlatformFile? = withContext(Dispatchers.Main) {
    suspendCoroutine { continuation ->
        // Create a picker delegate
        documentPickerDelegate = DocumentPickerDelegate(
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
                continuation.resume(file)
            },
            onPickerCancelled = {
                continuation.resume(null)
            },
        )

        // suggestedName cannot include "/" because the OS interprets it as a directory separator.
        // However, "Files" renders ":" as "/", so we can just use ":" and the user will see "/".
        val sanitizedSuggestedName = suggestedName.replace("/", ":")
        val fileName = when {
            extension != null -> "$sanitizedSuggestedName.$extension"
            else -> sanitizedSuggestedName
        }

        // Get the fileManager
        val fileManager = NSFileManager.defaultManager

        // Get the temporary directory
        val fileComponents = fileManager.temporaryDirectory.pathComponents?.plus(fileName)
            ?: throw IllegalStateException("Failed to get temporary directory")

        // Create a file URL
        val fileUrl = NSURL.fileURLWithPathComponents(fileComponents)
            ?: throw IllegalStateException("Failed to create file URL")

        // Write an empty string to the file to ensure it exists
        val emptyData = NSData()
        if (!emptyData.writeToURL(fileUrl, true)) {
            throw IllegalStateException("Failed to write to file URL")
        }

        // Create a picker controller
        val pickerController = UIDocumentPickerViewController(
            forExportingURLs = listOf(fileUrl),
        )

        // Set the initial directory
        directory?.let { pickerController.directoryURL = NSURL.fileURLWithPath(it.path) }

        // Assign the delegate to the picker controller
        pickerController.delegate = documentPickerDelegate

        // Present the picker controller
        UIApplication.sharedApplication.topMostViewController()?.presentViewController(
            pickerController,
            animated = true,
            completion = null,
        )
    }
}

/**
 * Opens a camera picker dialog.
 *
 * @param type The type of media to capture (Image or Video).
 * @param cameraFacing The camera facing (System, Back or Front).
 * @param destinationFile The file where the captured media will be saved.
 * @param openCameraSettings Platform-specific settings for the camera.
 * @return The saved file as a [PlatformFile], or null if cancelled.
 */
public actual suspend fun FileKit.openCameraPicker(
    type: FileKitCameraType,
    cameraFacing: FileKitCameraFacing,
    destinationFile: PlatformFile,
    openCameraSettings: FileKitOpenCameraSettings,
): PlatformFile? = withContext(Dispatchers.Main) {
    suspendCoroutine { continuation ->
        cameraControllerDelegate = CameraControllerDelegate(
            onImagePicked = { image ->
                if (image != null) {
                    // Convert UIImage to NSData (JPEG format with compression quality 1.0)
                    val imageData = UIImageJPEGRepresentation(image, 1.0)

                    // Create an NSURL for the file path
                    val fileUrl = NSURL.fileURLWithPath(destinationFile.path)

                    // Write the NSData to the file
                    if (imageData?.writeToURL(fileUrl, true) == true) {
                        // Return the NSURL of the saved image file
                        continuation.resume(destinationFile)
                    } else {
                        // If saving fails, return null
                        continuation.resume(null)
                    }
                } else {
                    continuation.resume(null)
                }
            },
        )

        val pickerController = UIImagePickerController()
        pickerController.sourceType =
            UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
        pickerController.delegate = cameraControllerDelegate

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

        UIApplication.sharedApplication.topMostViewController()?.presentViewController(
            pickerController,
            animated = true,
            completion = null,
        )
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

    val viewController = UIApplication.sharedApplication.topMostViewController() ?: return

    files.forEach { it.startAccessingSecurityScopedResource() }
    // Ensure we always pass a file URL to the activity items; otherwise iOS may treat the
    // provided value as plain text and share the path string instead of the actual file.
    val activityItems = files.map { NSURL.fileURLWithPath(it.path) }

    val shareVC = UIActivityViewController(activityItems, null)

    if (isIpad()) {
        // ipad need sourceView for show
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

private suspend fun callPicker(
    mode: Mode,
    contentTypes: List<UTType>,
    directory: PlatformFile?,
): List<NSURL>? = withContext(Dispatchers.Main) {
    suspendCoroutine { continuation ->
        // Create a picker delegate
        documentPickerDelegate = DocumentPickerDelegate(
            onFilesPicked = { urls -> continuation.resume(urls) },
            onPickerCancelled = { continuation.resume(null) },
        )

        // Create a picker controller
        val pickerController = UIDocumentPickerViewController(forOpeningContentTypes = contentTypes)

        // Set the initial directory
        directory?.let { pickerController.directoryURL = NSURL.fileURLWithPath(it.path) }

        // Setup the picker mode
        pickerController.allowsMultipleSelection = mode == Mode.Multiple

        // Assign the delegate to the picker controller
        pickerController.delegate = documentPickerDelegate

        // Present the picker controller
        UIApplication.sharedApplication.topMostViewController()?.presentViewController(
            pickerController,
            animated = true,
            completion = null,
        )
    }
}

private suspend fun getPhPickerResults(
    mode: PickerMode,
    type: FileKitType,
): List<PHPickerResult> = suspendCoroutine { continuation ->
    // Create a picker delegate
    phPickerDelegate = PhPickerDelegate(onFilesPicked = continuation::resume)
    phPickerDismissDelegate = PhPickerDismissDelegate(onFilesPicked = continuation::resume)

    // Define configuration
    val configuration = PHPickerConfiguration(sharedPhotoLibrary())

    // Number of medias to select
    configuration.selectionLimit = when (mode) {
        is PickerMode.Multiple -> mode.maxItems?.toLong() ?: 0
        PickerMode.Single -> 1
    }

    // Use current mode per Apple documentation for faster file provider
    configuration.preferredAssetRepresentationMode = PHPickerConfigurationAssetRepresentationModeCurrent

    // Filter configuration
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

    // Create a picker controller
    val controller = PHPickerViewController(configuration = configuration)
    controller.delegate = phPickerDelegate
    controller.presentationController?.delegate = phPickerDismissDelegate

    // Present the picker controller
    UIApplication.sharedApplication.topMostViewController()?.presentViewController(
        controller,
        animated = true,
        completion = null,
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun callPhPicker(
    mode: PickerMode,
    type: FileKitType,
): Flow<FileKitPickerState<List<PlatformFile>>> = channelFlow {
    // Fetch picker results on Main
    val pickerResults = withContext(Dispatchers.Main) {
        getPhPickerResults(mode, type)
    }

    if (pickerResults.isEmpty()) {
        send(FileKitPickerState.Cancelled)
        return@channelFlow
    }

    send(FileKitPickerState.Started(pickerResults.size))

    val fileManager = NSFileManager.defaultManager
    val tempRoot = fileManager.temporaryDirectory
        .URLByAppendingPathComponent(NSUUID().UUIDString)
        ?: throw IllegalStateException("Failed to create temporary directory")
    fileManager.createDirectoryAtURL(
        url = tempRoot,
        withIntermediateDirectories = true,
        attributes = null,
        error = null,
    )

    // Pre-allocated array to preserve selection order
    val orderedFiles = arrayOfNulls<PlatformFile>(pickerResults.size)
    val lock = Mutex()

    // Launch a child coroutine for every copy, preserving index
    pickerResults
        .mapIndexed { index, result ->
            launch(Dispatchers.IO) {
                val src = suspendCancellableCoroutine<NSURL?> { cont ->
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
                                cont.resumeWithException(IllegalStateException(error.localizedFailureReason()))
                            }

                            else -> {
                                // Must copy the URL here because it becomes invalid outside of the loadFileRepresentationForTypeIdentifier callback scope
                                val tempUrl = url?.let {
                                    copyToTempFile(fileManager, it, tempRoot.lastPathComponent!!)
                                }
                                cont.resume(tempUrl)
                            }
                        }
                    }
                } ?: return@launch // skip nulls

                lock.withLock {
                    // Insert at original index to preserve selection order
                    orderedFiles[index] = PlatformFile(src)
                    send(FileKitPickerState.Progress(orderedFiles.filterNotNull(), pickerResults.size))
                }
            }
        }.joinAll()

    send(FileKitPickerState.Completed(orderedFiles.filterNotNull()))
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
        ?: throw IllegalStateException("Failed to get temporary directory")

    // Create a file URL
    val fileUrl = NSURL.fileURLWithPathComponents(fileComponents)
        ?: throw IllegalStateException("Failed to create file URL")

    // Write the data to the file URL
    fileManager.copyItemAtURL(
        srcURL = url,
        toURL = fileUrl,
        error = null,
    )

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
