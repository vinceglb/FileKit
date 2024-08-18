package io.github.vinceglb.filekit.core

import kotlinx.browser.document
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.set
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.asList
import org.w3c.dom.url.URL
import org.w3c.files.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

public actual object FileKit {
    public actual suspend fun <Out> pickFile(
        type: PickerType,
        mode: PickerMode<Out>,
        title: String?,
        initialDirectory: String?,
        platformSettings: FileKitPlatformSettings?,
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

    public actual suspend fun pickDirectory(
        title: String?,
        initialDirectory: String?,
        platformSettings: FileKitPlatformSettings?,
    ): IPlatformFile? = withContext(Dispatchers.Default) {
        throw NotImplementedError("Directory selection is not supported on the web")
    }

    public actual fun isDirectoryPickerSupported(): Boolean = false

    public actual suspend fun saveFile(
        bytes: ByteArray?,
        baseName: String,
        extension: String,
        initialDirectory: String?,
        platformSettings: FileKitPlatformSettings?,
    ): IPlatformFile? = withContext(Dispatchers.Default) {
        if (bytes == null) {
            throw FileKitFileSaverWithoutBytesException()
        }

        // Create a byte array
        val array = Uint8Array(bytes.size)
        for (i in bytes.indices) {
            array[i] = bytes[i]
        }

        // Create a dynamic array
        val dynamicArray: Array<dynamic> = emptyArray()
        dynamicArray[0] = array

        // Create a blob
        val file = File(
            fileBits = dynamicArray,
            fileName = "$baseName.$extension",
        )

        // Create a element
        val a = document.createElement("a") as HTMLAnchorElement
        a.href = URL.createObjectURL(file)
        a.download = "$baseName.$extension"

        // Trigger the download
        a.click()

        // Return the file
        PlatformFile(file)
    }

    public actual suspend fun isSaveFileWithoutBytesSupported(): Boolean = false
}
