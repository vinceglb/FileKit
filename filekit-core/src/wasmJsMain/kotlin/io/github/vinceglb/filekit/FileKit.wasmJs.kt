package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.utils.toJsArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    downloadBlob(blob = file, fileName = fileName)
}
