package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.browser.document
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.asList
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

public actual suspend fun <Out> FileKit.openFilePicker(
    type: PickerType,
    mode: PickerMode<Out>,
    title: String?,
    initialDirectory: String?,
    platformSettings: FileKitDialogSettings,
): Out? = withContext(Dispatchers.Default) {
    suspendCoroutine { continuation ->
        // Create input element
        val input = document.createElement("input") as HTMLInputElement

        // Configure the input element
        input.apply {
            this.type = "file"

            // Set the allowed file types
            when (type) {
                is PickerType.Image -> accept = "image/*"
                is PickerType.Video -> accept = "video/*"
                is PickerType.ImageAndVideo -> accept = "image/*,video/*"
                is PickerType.File -> type.extensions?.let {
                    accept = type.extensions.joinToString(",") { ".$it" }
                }
            }

            // Set the multiple attribute
            multiple = mode is PickerMode.Multiple

            // max is not supported for file inputs
        }

        // Setup the change listener
        input.onchange = { event ->
            try {
                // Get the selected files
                val files = event.target
                    ?.unsafeCast<HTMLInputElement>()
                    ?.files
                    ?.asList()

                // Return the result
                val result = files?.map { PlatformFile(it) }
                continuation.resume(mode.parseResult(result))
            } catch (e: Throwable) {
                continuation.resumeWithException(e)
            }
        }

        input.oncancel = {
            continuation.resume(null)
        }

        // Trigger the file picker
        input.click()
    }
}
