package io.github.vinceglb.picker.core

import io.github.vinceglb.picker.core.util.PickerDelegate
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.fileURLWithPathComponents
import platform.Foundation.pathComponents
import platform.Foundation.temporaryDirectory
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UISceneActivationStateForegroundActive
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UniformTypeIdentifiers.UTType
import platform.UniformTypeIdentifiers.UTTypeContent
import platform.UniformTypeIdentifiers.UTTypeFolder
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

public actual object Picker {
    // Create a reference to the picker delegate to prevent it from being garbage collected
    private lateinit var pickerDelegate: PickerDelegate

    public actual suspend fun <Out> pick(
        mode: PickerSelectionMode<Out>,
        title: String?,
        initialDirectory: String?
    ): Out? = suspendCoroutine { continuation ->
        // Create a picker delegate
        pickerDelegate = PickerDelegate(
            onFilesPicked = { urls ->
                val selection = PickerSelectionMode.SelectionResult(urls)
                continuation.resume(mode.result(selection))
            },
            onPickerCancelled = {
                continuation.resume(null)
            }
        )

        // Create a picker controller
        val pickerController = UIDocumentPickerViewController(
            forOpeningContentTypes = mode.contentTypes
        )

        // Set the initial directory
        initialDirectory?.let { pickerController.directoryURL = NSURL.fileURLWithPath(it) }

        // Setup the picker mode
        if (mode is PickerSelectionMode.MultipleFiles) {
            pickerController.allowsMultipleSelection = true
        }

        // Assign the delegate to the picker controller
        pickerController.delegate = pickerDelegate

        // Present the picker controller
        UIApplication.sharedApplication.firstKeyWindow?.rootViewController?.presentViewController(
            pickerController,
            animated = true,
            completion = null
        )
    }

    public actual suspend fun save(
        bytes: ByteArray,
        fileName: String,
        initialDirectory: String?
    ): PlatformFile? = suspendCoroutine { continuation ->
        // Create a picker delegate
        pickerDelegate = PickerDelegate(
            onFilesPicked = { urls ->
                val file = urls.firstOrNull()?.let { PlatformFile(it) }
                continuation.resume(file)
            },
            onPickerCancelled = {
                continuation.resume(null)
            }
        )

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
        pickerController.delegate = pickerDelegate

        // Present the picker controller
        UIApplication.sharedApplication.firstKeyWindow?.rootViewController?.presentViewController(
            pickerController,
            animated = true,
            completion = null
        )
    }

    // How to get Root view controller in Swift
    // https://sarunw.com/posts/how-to-get-root-view-controller/
    private val UIApplication.firstKeyWindow: UIWindow?
        get() = this.connectedScenes
            .filterIsInstance<UIWindowScene>()
            .firstOrNull { it.activationState == UISceneActivationStateForegroundActive }
            ?.keyWindow

    private val PickerSelectionMode<*>.contentTypes: List<UTType>
        get() = when (this) {
            is PickerSelectionMode.Directory -> listOf(UTTypeFolder)

            is PickerSelectionMode.SingleFile -> this.extensions
                ?.mapNotNull { UTType.typeWithFilenameExtension(it) }
                .ifNullOrEmpty { listOf(UTTypeContent) }

            is PickerSelectionMode.MultipleFiles -> this.extensions
                ?.mapNotNull { UTType.typeWithFilenameExtension(it) }
                .ifNullOrEmpty { listOf(UTTypeContent) }

            else -> throw IllegalArgumentException("Unsupported mode: $this")
        }

    private fun <R> List<R>?.ifNullOrEmpty(block: () -> List<R>): List<R> =
        if (this.isNullOrEmpty()) block() else this
}
