package io.github.vinceglb.picker.core

import io.github.vinceglb.picker.core.util.DocumentPickerDelegate
import io.github.vinceglb.picker.core.util.PhPickerDelegate
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.fileURLWithPathComponents
import platform.Foundation.pathComponents
import platform.Foundation.temporaryDirectory
import platform.Photos.PHPhotoLibrary.Companion.sharedPhotoLibrary
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UISceneActivationStateForegroundActive
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UniformTypeIdentifiers.UTType
import platform.UniformTypeIdentifiers.UTTypeContent
import platform.UniformTypeIdentifiers.UTTypeFolder
import platform.UniformTypeIdentifiers.UTTypeImage
import platform.UniformTypeIdentifiers.UTTypeVideo
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

public actual object Picker {
    // Create a reference to the picker delegate to prevent it from being garbage collected
    private lateinit var documentPickerDelegate: DocumentPickerDelegate
    private lateinit var phPickerDelegate: PhPickerDelegate

    public actual suspend fun <Out> pickFile(
        type: PickerSelectionType,
        mode: PickerSelectionMode<Out>,
        title: String?,
        initialDirectory: String?
    ): Out? = when (type) {
        // Use PHPickerViewController for images and videos
        is PickerSelectionType.Image,
        is PickerSelectionType.Video -> callPhPicker(
            isMultipleMode = mode is PickerSelectionMode.Multiple,
            type = type
        )?.map { PlatformFile(it) }?.let { mode.parseResult(it) }

        // Use UIDocumentPickerViewController for other types
        else -> callPicker(
            mode = when (mode) {
                is PickerSelectionMode.Single -> Mode.Single
                is PickerSelectionMode.Multiple -> Mode.Multiple
            },
            contentTypes = type.contentTypes,
            initialDirectory = initialDirectory
        )?.map { PlatformFile(it) }?.let { mode.parseResult(it) }
    }

    public actual suspend fun pickDirectory(
        title: String?,
        initialDirectory: String?
    ): PlatformDirectory? = callPicker(
        mode = Mode.Directory,
        contentTypes = listOf(UTTypeFolder),
        initialDirectory = initialDirectory
    )?.firstOrNull()?.let { PlatformDirectory(it) }

    public actual fun isPickDirectorySupported(): Boolean = true

    public actual suspend fun saveFile(
        bytes: ByteArray,
        baseName: String,
        extension: String,
        initialDirectory: String?,
    ): PlatformFile? = suspendCoroutine { continuation ->
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

        val fileName = "$baseName.$extension"

        // Get the fileManager
        val fileManager = NSFileManager.defaultManager

        // Get the temporary directory
        val fileComponents = fileManager.temporaryDirectory.pathComponents?.plus(fileName)
            ?: throw IllegalStateException("Failed to get temporary directory")

        // Create a file URL
        val fileUrl = NSURL.fileURLWithPathComponents(fileComponents)
            ?: throw IllegalStateException("Failed to create file URL")

        // Write the bytes to the temp file
        writeBytesArrayToNsUrl(bytes, fileUrl)

        // Create a picker controller
        val pickerController = UIDocumentPickerViewController(
            forExportingURLs = listOf(fileUrl)
        )

        // Set the initial directory
        initialDirectory?.let { pickerController.directoryURL = NSURL.fileURLWithPath(it) }

        // Assign the delegate to the picker controller
        pickerController.delegate = documentPickerDelegate

        // Present the picker controller
        UIApplication.sharedApplication.firstKeyWindow?.rootViewController?.presentViewController(
            pickerController,
            animated = true,
            completion = null
        )
    }

    private suspend fun callPicker(
        mode: Mode,
        contentTypes: List<UTType>,
        initialDirectory: String?,
    ): List<NSURL>? = suspendCoroutine { continuation ->
        // Create a picker delegate
        documentPickerDelegate = DocumentPickerDelegate(
            onFilesPicked = { urls -> continuation.resume(urls) },
            onPickerCancelled = { continuation.resume(null) }
        )

        // Create a picker controller
        val pickerController = UIDocumentPickerViewController(forOpeningContentTypes = contentTypes)

        // Set the initial directory
        initialDirectory?.let { pickerController.directoryURL = NSURL.fileURLWithPath(it) }

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

    private suspend fun callPhPicker(
        isMultipleMode: Boolean,
        type: PickerSelectionType,
    ): List<NSURL>? {
        val pickerResults: List<PHPickerResult> = suspendCoroutine { continuation ->
            // Create a picker delegate
            phPickerDelegate = PhPickerDelegate(
                onFilesPicked = continuation::resume
            )

            // Define configuration
            val configuration = PHPickerConfiguration(sharedPhotoLibrary())

            // Number of medias to select
            configuration.selectionLimit = if (isMultipleMode) 0 else 1

            // Filter configuration
            configuration.filter = when (type) {
                is PickerSelectionType.Image -> PHPickerFilter.imagesFilter
                is PickerSelectionType.Video -> PHPickerFilter.videosFilter
                is PickerSelectionType.ImageAndVideo -> PHPickerFilter.anyFilterMatchingSubfilters(
                    listOf(
                        PHPickerFilter.imagesFilter,
                        PHPickerFilter.videosFilter
                    )
                )

                else -> throw IllegalArgumentException("Unsupported type: $type")
            }

            // Create a picker controller
            val controller = PHPickerViewController(configuration = configuration)
            controller.delegate = phPickerDelegate

            // Present the picker controller
            UIApplication.sharedApplication.firstKeyWindow?.rootViewController?.presentViewController(
                controller,
                animated = true,
                completion = null
            )
        }

        return pickerResults.mapNotNull { result ->
            suspendCoroutine<NSURL?> { continuation ->
                result.itemProvider.loadFileRepresentationForTypeIdentifier(
                    typeIdentifier = when (type) {
                        is PickerSelectionType.Image -> UTTypeImage.identifier
                        is PickerSelectionType.Video -> UTTypeVideo.identifier
                        is PickerSelectionType.ImageAndVideo -> UTTypeContent.identifier
                        else -> throw IllegalArgumentException("Unsupported type: $type")
                    }
                ) { url, _ -> continuation.resume(url) }
            }
        }.takeIf { it.isNotEmpty() }
    }

    // How to get Root view controller in Swift
    // https://sarunw.com/posts/how-to-get-root-view-controller/
    private val UIApplication.firstKeyWindow: UIWindow?
        get() = this.connectedScenes
            .filterIsInstance<UIWindowScene>()
            .firstOrNull { it.activationState == UISceneActivationStateForegroundActive }
            ?.keyWindow

    private val PickerSelectionType.contentTypes: List<UTType>
        get() = when (this) {
            is PickerSelectionType.Image -> listOf(UTTypeImage)
            is PickerSelectionType.Video -> listOf(UTTypeVideo)
            is PickerSelectionType.ImageAndVideo -> listOf(UTTypeImage, UTTypeVideo)
            is PickerSelectionType.File -> extensions
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
}
