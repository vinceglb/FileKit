package io.github.vinceglb.filekit

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.w3c.files.File
import org.w3c.files.FileReader
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

public actual data class PlatformFile(
    val file: File,
)

public actual val PlatformFile.name: String
    get() = file.name

public actual val PlatformFile.size: Long
    get() = file.size.toDouble().toLong()

public actual suspend fun PlatformFile.readBytes(): ByteArray = withContext(Dispatchers.Main) {
    suspendCoroutine { continuation ->
        val reader = FileReader()
        reader.onload = { event ->
            try {
                // Read the file as an ArrayBuffer
                val arrayBuffer = event
                    .target
                    ?.unsafeCast<FileReader>()
                    ?.result
                    ?.unsafeCast<ArrayBuffer>()
                    ?: throw IllegalStateException("Could not read file")

                // Convert the ArrayBuffer to a ByteArray
                val bytes = Uint8Array(arrayBuffer)

                // Copy the bytes into a ByteArray
                val byteArray = ByteArray(bytes.length)
                for (i in 0 until bytes.length) {
                    byteArray[i] = bytes[i]
                }

                // Return the ByteArray
                continuation.resume(byteArray)
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }

        // Read the file as an ArrayBuffer
        reader.readAsArrayBuffer(file)
    }
}
