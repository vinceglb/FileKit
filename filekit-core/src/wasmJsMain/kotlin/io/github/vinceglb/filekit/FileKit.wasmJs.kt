package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.utils.toJsArray
import kotlinx.browser.document
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.url.URL
import org.w3c.files.File

public actual object FileKit

/**
 * Downloads a file in the browser.
 *
 * @param bytes The content of the file to download.
 * @param fileName The name of the downloaded file.
 */
@OptIn(ExperimentalWasmJsInterop::class)
public actual suspend fun FileKit.download(
    bytes: ByteArray,
    fileName: String,
): Unit = withContext(Dispatchers.Default) {
    // Create a blob
    val file = File(
        fileBits = bytes.toJsArray(),
        fileName = fileName,
    )

    // Create a element
    val a = document.createElement("a") as HTMLAnchorElement
    a.href = URL.createObjectURL(file)
    a.download = fileName

    // Trigger the download
    a.click()
}
