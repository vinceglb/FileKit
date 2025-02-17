package io.github.vinceglb.filekit

import kotlinx.browser.document
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.set
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.url.URL
import org.w3c.files.File

public actual object FileKit

public actual suspend fun FileKit.download(
    bytes: ByteArray,
    fileName: String,
): Unit = withContext(Dispatchers.Default) {
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
        fileName = fileName,
    )

    // Create a element
    val a = document.createElement("a") as HTMLAnchorElement
    a.href = URL.createObjectURL(file)
    a.download = fileName

    // Trigger the download
    a.click()
}
