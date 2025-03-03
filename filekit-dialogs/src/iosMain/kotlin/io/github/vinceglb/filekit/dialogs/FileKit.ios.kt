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
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSDataReadingUncached
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.temporaryDirectory
import platform.Foundation.writeToURL
import platform.Photos.PHPhotoLibrary.Companion.sharedPhotoLibrary
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UISceneActivationStateForegroundActive
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UIKit.presentationController
import platform.UniformTypeIdentifiers.UTType
import platform.UniformTypeIdentifiers.UTTypeContent
import platform.UniformTypeIdentifiers.UTTypeFolder
import platform.UniformTypeIdentifiers.UTTypeImage
import platform.UniformTypeIdentifiers.UTTypeMovie
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private object FileKitDialog {
    // Create a reference to the picker delegate to prevent it from being garbage collected
    lateinit var documentPickerDelegate: DocumentPickerDelegate
    lateinit var phPickerDelegate: PhPickerDelegate
    lateinit var phPickerDismissDelegate: PhPickerDismissDelegate
    lateinit var cameraControllerDelegate: CameraControllerDelegate
}

public actual suspend fun <Out> FileKit.openFilePicker(
    type: FileKitType,
    mode: FileKitMode<Out>,
    title: String?,
    directory: PlatformFile?,
    dialogSettings: FileKitDialogSettings,
): Out? = when (type) {
    // Use PHPickerViewController for images and videos
    is FileKitType.Image,
    is FileKitType.Video,
    is FileKitType.ImageAndVideo -> callPhPicker(
        mode = mode,
        type = type
    )?.map { PlatformFile(it) }?.let { mode.parseResult(it) }

    // Use UIDocumentPickerViewController for other types
    else -> callPicker(
        mode = when (mode) {
            is FileKitMode.Single -> Mode.Single
            is FileKitMode.Multiple -> Mode.Multiple
        },
        contentTypes = type.contentTypes,
        directory = directory
    )?.map { PlatformFile(it) }?.let { mode.parseResult(it) }
}

public actual suspend fun FileKit.openDirectoryPicker(
    title: String?,
    directory: PlatformFile?,
    dialogSettings: FileKitDialogSettings,
): PlatformFile? = callPicker(
    mode = Mode.Directory,
    contentTypes = listOf(UTTypeFolder),
    directory = directory
)?.firstOrNull()?.let { PlatformFile(it) }

public actual suspend fun FileKit.openFileSaver(
    suggestedName: String,
    extension: String,
    directory: PlatformFile?,
    dialogSettings: FileKitDialogSettings,
): PlatformFile? = withContext(Dispatchers.Main) {
    suspendCoroutine { continuation ->
        // Create a picker delegate
        documentPickerDelegate = DocumentPickerDelegate(
            onFilesPicked = { urls ->
                val file = urls.firstOrNull()?.let { PlatformFile(it) }
                continuation.resume(file)
            },
            onPickerCancelled = {
                continuation.resume(null)
            }
        )

        val fileName = "$suggestedName.$extension"

        // Get the fileManager
        val fileManager = NSFileManager.defaultManager

        // Get the temporary directory
        val fileComponents = fileManager.temporaryDirectory.pathComponents?.plus(fileName)
            ?: throw IllegalStateException("Failed to get temporary directory")

        // Create a file URL
        val fileUrl = NSURL.fileURLWithPathComponents(fileComponents)
            ?: throw IllegalStateException("Failed to create file URL")

        // Write the bytes to the temp file
        // writeBytesArrayToNsUrl(bytes, fileUrl)

        // Create a picker controller
        val pickerController = UIDocumentPickerViewController(
            forExportingURLs = listOf(fileUrl)
        )

        // Set the initial directory
        directory?.let { pickerController.directoryURL = NSURL.fileURLWithPath(it.path) }

        // Assign the delegate to the picker controller
        pickerController.delegate = documentPickerDelegate

        // Present the picker controller
        UIApplication.sharedApplication.firstKeyWindow?.rootViewController?.presentViewController(
            pickerController,
            animated = true,
            completion = null
        )
    }
}

public actual suspend fun FileKit.openCameraPicker(
    type: FileKitCameraType
): PlatformFile? = withContext(Dispatchers.Main) {
    suspendCoroutine { continuation ->
        cameraControllerDelegate = CameraControllerDelegate(
            onImagePicked = { image ->
                if (image != null) {
                    // Convert UIImage to NSData (JPEG format with compression quality 1.0)
                    val imageData = UIImageJPEGRepresentation(image, 1.0)

                    // Get the path for the temporary directory
                    val tempDir = NSTemporaryDirectory()
                    val fileName = "image_${NSUUID().UUIDString}.jpg"
                    val filePath = tempDir + fileName

                    // Create an NSURL for the file path
                    val fileUrl = NSURL.fileURLWithPath(filePath)

                    // Write the NSData to the file
                    if (imageData?.writeToURL(fileUrl, true) == true) {
                        // Return the NSURL of the saved image file
                        continuation.resume(PlatformFile(fileUrl))
                    } else {
                        // If saving fails, return null
                        continuation.resume(null)
                    }
                } else {
                    continuation.resume(null)
                }
            }
        )

        val pickerController = UIImagePickerController()
        pickerController.sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
        pickerController.delegate = cameraControllerDelegate

        UIApplication.sharedApplication.firstKeyWindow?.rootViewController?.presentViewController(
            pickerController,
            animated = true,
            completion = null
        )
    }
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
            onPickerCancelled = { continuation.resume(null) }
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
        UIApplication.sharedApplication.firstKeyWindow?.rootViewController?.presentViewController(
            pickerController,
            animated = true,
            completion = null
        )
    }
}

@OptIn(ExperimentalForeignApi::class)
private suspend fun <Out> callPhPicker(
    mode: FileKitMode<Out>,
    type: FileKitType,
): List<NSURL>? = withContext(Dispatchers.Main) {
    val pickerResults: List<PHPickerResult> = suspendCoroutine { continuation ->
        // Create a picker delegate
        phPickerDelegate = PhPickerDelegate(
            onFilesPicked = continuation::resume
        )
        phPickerDismissDelegate = PhPickerDismissDelegate(
            onFilesPicked = continuation::resume
        )

        // Define configuration
        val configuration = PHPickerConfiguration(sharedPhotoLibrary())

        // Number of medias to select
        configuration.selectionLimit = when (mode) {
            is FileKitMode.Multiple -> mode.maxItems?.toLong() ?: 0
            FileKitMode.Single -> 1
        }

        // Filter configuration
        configuration.filter = when (type) {
            is FileKitType.Image -> PHPickerFilter.imagesFilter
            is FileKitType.Video -> PHPickerFilter.videosFilter
            is FileKitType.ImageAndVideo -> PHPickerFilter.anyFilterMatchingSubfilters(
                listOf(
                    PHPickerFilter.imagesFilter,
                    PHPickerFilter.videosFilter,
                )
            )

            else -> throw IllegalArgumentException("Unsupported type: $type")
        }

        // Create a picker controller
        val controller = PHPickerViewController(configuration = configuration)
        controller.delegate = phPickerDelegate
        controller.presentationController?.delegate = phPickerDismissDelegate

        // Present the picker controller
        UIApplication.sharedApplication.firstKeyWindow?.rootViewController?.presentViewController(
            controller,
            animated = true,
            completion = null
        )
    }

    return@withContext withContext(Dispatchers.IO) {
        val fileManager = NSFileManager.defaultManager

        pickerResults.mapNotNull { result ->
            suspendCoroutine<NSURL?> { continuation ->
                result.itemProvider.loadFileRepresentationForTypeIdentifier(
                    typeIdentifier = when (type) {
                        is FileKitType.Image -> UTTypeImage.identifier
                        is FileKitType.Video -> UTTypeMovie.identifier
                        is FileKitType.ImageAndVideo -> UTTypeContent.identifier
                        else -> throw IllegalArgumentException("Unsupported type: $type")
                    }
                ) { url, _ ->
                    val tmpUrl = url?.let {
                        // Get the temporary directory
                        val fileComponents =
                            fileManager.temporaryDirectory.pathComponents?.plus(it.lastPathComponent)
                                ?: throw IllegalStateException("Failed to get temporary directory")

                        // Create a file URL
                        val fileUrl = NSURL.fileURLWithPathComponents(fileComponents)
                            ?: throw IllegalStateException("Failed to create file URL")

                        // Read the data from the URL
                        val data = NSData.dataWithContentsOfURL(it, NSDataReadingUncached, null)
                            ?: throw IllegalStateException("Failed to read data from $it")

                        // Write the data to the temp file
                        data.writeToURL(fileUrl, true)

                        // Return the temporary
                        fileUrl
                    }

                    continuation.resume(tmpUrl)
                }
            }
        }.takeIf { it.isNotEmpty() }
    }
}

// How to get Root view controller in Swift
// https://sarunw.com/posts/how-to-get-root-view-controller/
private val UIApplication.firstKeyWindow: UIWindow?
    get() = this.connectedScenes
        .filterIsInstance<UIWindowScene>()
        .firstOrNull { it.activationState == UISceneActivationStateForegroundActive }
        ?.keyWindow

private val FileKitType.contentTypes: List<UTType>
    get() = when (this) {
        is FileKitType.Image -> listOf(UTTypeImage)
        is FileKitType.Video -> listOf(UTTypeMovie)
        is FileKitType.ImageAndVideo -> listOf(UTTypeImage, UTTypeMovie)
        is FileKitType.File -> extensions
            ?.mapNotNull { UTType.typeWithFilenameExtension(it) }
            .ifNullOrEmpty { listOf(UTTypeContent) }
    }

private fun <R> List<R>?.ifNullOrEmpty(block: () -> List<R>): List<R> =
    if (this.isNullOrEmpty()) block() else this

private enum class Mode {
    Single,
    Multiple,
    Directory
}
