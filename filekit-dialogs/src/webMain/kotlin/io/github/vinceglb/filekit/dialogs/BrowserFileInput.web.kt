package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.BrowserFile
import io.github.vinceglb.filekit.WebFile
import kotlinx.browser.document
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
import org.w3c.files.FileList
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.js.JsException

@OptIn(ExperimentalWasmJsInterop::class)
internal suspend fun openBrowserFileInput(
    type: FileKitType,
    multipleMode: Boolean,
    directoryMode: Boolean,
    failure: (Throwable) -> FileKitDialogException,
): List<WebFile.FileWrapper>? = withContext(Dispatchers.Default) {
    suspendCancellableCoroutine { continuation ->
        val inputElement = document.createElement("input")
        val input = inputElement as BrowserFileInputElement

        fun cleanup() {
            input.onchange = null
            input.oncancel = null
            inputElement.parentNode?.removeChild(inputElement)
        }

        input.onchange = {
            try {
                val result = input.files
                    ?.asList()
                    ?.map { WebFile.FileWrapper(it.unsafeCast<BrowserFile>()) }

                if (continuation.isActive) continuation.resume(result)
            } catch (cause: JsException) {
                if (continuation.isActive) continuation.resumeWithException(failure(cause))
            } catch (cause: Throwable) {
                if (continuation.isActive) continuation.resumeWithException(cause)
            } finally {
                cleanup()
            }
        }

        input.oncancel = {
            if (continuation.isActive) continuation.resume(null)
            cleanup()
        }

        continuation.invokeOnCancellation { cleanup() }

        try {
            (inputElement as HTMLElement).style.display = "none"
            document.body?.appendChild(inputElement)
            input.apply {
                this.type = "file"
                accept = type.acceptAttribute
                multiple = multipleMode
                webkitdirectory = directoryMode
            }
            input.click()
        } catch (cause: JsException) {
            cleanup()
            if (continuation.isActive) continuation.resumeWithException(failure(cause))
        }
    }
}

private val FileKitType.acceptAttribute: String
    get() = when (this) {
        is FileKitType.Image -> {
            "image/*"
        }

        is FileKitType.Video -> {
            "video/*"
        }

        is FileKitType.ImageAndVideo -> {
            "image/*,video/*"
        }

        is FileKitType.File -> {
            extensions
                ?.joinToString(",") { ".$it" }
                .orEmpty()
        }
    }

@JsName("HTMLInputElement")
internal external interface BrowserFileInputElement {
    var accept: String
    val files: FileList?
    var multiple: Boolean
    var webkitdirectory: Boolean
    var type: String
    var value: String
    var onchange: (() -> Unit)?
    var oncancel: (() -> Unit)?

    fun click()
}
