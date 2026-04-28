package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.FileExt
import io.github.vinceglb.filekit.FileHandleFile
import kotlinx.browser.document
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
import org.w3c.files.FileList
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalWasmJsInterop::class)
internal actual suspend fun platformOpenFilePickerWeb(
    type: FileKitType,
    multipleMode: Boolean, // select multiple files
    directoryMode: Boolean, // select a directory
): List<FileHandleFile>? {
    val files = withContext(Dispatchers.Default) {
        suspendCoroutine { continuation ->
            // Create input element
            val input = document.createElement("input") as HTMLInputElementExt

            // Visually hide the element
            input.style.display = "none"

            document.body?.appendChild(input)

            // Configure the input element
            input.apply {
                this.type = "file"

                // Set the allowed file types
                when (type) {
                    is FileKitType.Image -> accept = "image/*"

                    is FileKitType.Video -> accept = "video/*"

                    is FileKitType.ImageAndVideo -> accept = "image/*,video/*"

                    is FileKitType.File -> type.extensions?.let {
                        accept = type.extensions.joinToString(",") { ".$it" }
                    }
                }

                // Set the multiple attribute
                multiple = multipleMode
                webkitdirectory = directoryMode

                // max is not supported for file inputs
            }

            // Setup the change listener
            input.onchange = { event ->
                try {
                    // Get the selected files
                    val files = event.target
                        ?.unsafeCast<HTMLInputElementExt>()
                        ?.files
                        ?.asList()
                        ?.map { it.unsafeCast<FileExt>() }

                    // Return the result
                    val result = files?.map { FileHandleFile(it) }
                    continuation.resume(result)
                } catch (e: Throwable) {
                    continuation.resumeWithException(e)
                } finally {
                    document.body?.removeChild(input)
                }
            }

            input.oncancel = {
                continuation.resume(null)
                document.body?.removeChild(input)
            }

            // Trigger the file picker
            input.click()
        }
    }

    return files
}

@JsName("HTMLInputElement")
public abstract external class HTMLInputElementExt : HTMLElement {
    public open var accept: String
    public open val files: FileList?
    public open var multiple: Boolean
    public open var webkitdirectory: Boolean
    public open var type: String
    public open var value: String
}
