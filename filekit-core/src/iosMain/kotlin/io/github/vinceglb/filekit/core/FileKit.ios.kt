package io.github.vinceglb.filekit.core

import io.github.vinceglb.filekit.core.util.DocumentPickerDelegate
import io.github.vinceglb.filekit.core.util.PhPickerDelegate
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSDataReadingUncached
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.fileURLWithPathComponents
import platform.Foundation.lastPathComponent
import platform.Foundation.pathComponents
import platform.Foundation.temporaryDirectory
import platform.Foundation.writeToURL
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
import platform.UniformTypeIdentifiers.UTTypeMovie
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

public actual object FileKit {
    // Create a reference to the picker delegate to prevent it from being garbage collected
    private lateinit var documentPickerDelegate: DocumentPickerDelegate
    private lateinit var phPickerDelegate: PhPickerDelegate

    public actual suspend fun <Out> pickFile(
        type: PickerType,
        mode: PickerMode<Out>,
        title: String?,
        initialDirectory: String?,
        platformSettings: FileKitPlatformSettings?,
    ): Out? = when (type) {
        // Use PHPickerViewController for images and videos
        is PickerType.Image,
        is PickerType.Video -> callPhPicker(
            isMultipleMode = mode is PickerMode.Multiple,
            type = type
        )?.map { PlatformFile(it) }?.let { mode.parseResult(it) }

        // Use UIDocumentPickerViewController for other types
        else -> callPicker(
            mode = when (mode) {
                is PickerMode.Single -> Mode.Single
                is PickerMode.Multiple -> Mode.Multiple
            },
            contentTypes = type.contentTypes,
            initialDirectory = initialDirectory
        )?.map { PlatformFile(it) }?.let { mode.parseResult(it) }
    }

    public actual suspend fun pickDirectory(
        title: String?,
        initialDirectory: String?,
        platformSettings: FileKitPlatformSettings?,
    ): PlatformDirectory? = callPicker(
        mode = Mode.Directory,
        contentTypes = listOf(UTTypeFolder),
        initialDirectory = initialDirectory
    )?.firstOrNull()?.let { PlatformDirectory(it) }

    public actual fun isDirectoryPickerSupported(): Boolean = true

    public actual suspend fun saveFile(
        bytes: ByteArray?,
        baseName: String,
        extension: String,
        initialDirectory: String?,
        platformSettings: FileKitPlatformSettings?,
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

    public actual suspend fun isSaveFileWithoutBytesSupported(): Boolean = true

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

    @OptIn(ExperimentalForeignApi::class)
    private suspend fun callPhPicker(
        isMultipleMode: Boolean,
        type: PickerType,
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
                is PickerType.Image -> PHPickerFilter.imagesFilter
                is PickerType.Video -> PHPickerFilter.videosFilter
                is PickerType.ImageAndVideo -> PHPickerFilter.anyFilterMatchingSubfilters(
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

        return withContext(Dispatchers.IO) {
            val fileManager = NSFileManager.defaultManager

            pickerResults.mapNotNull { result ->
                suspendCoroutine<NSURL?> { continuation ->
                    result.itemProvider.loadFileRepresentationForTypeIdentifier(
                        typeIdentifier = when (type) {
                            is PickerType.Image -> UTTypeImage.identifier
                            is PickerType.Video -> UTTypeMovie.identifier
                            is PickerType.ImageAndVideo -> UTTypeContent.identifier
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

    private val PickerType.contentTypes: List<UTType>
        get() = when (this) {
            is PickerType.Image -> listOf(UTTypeImage)
            is PickerType.Video -> listOf(UTTypeMovie)
            is PickerType.ImageAndVideo -> listOf(UTTypeImage, UTTypeMovie)
            is PickerType.File -> extensions
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
