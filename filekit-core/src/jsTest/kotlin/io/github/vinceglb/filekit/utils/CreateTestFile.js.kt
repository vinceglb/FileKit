package io.github.vinceglb.filekit.utils

import io.github.vinceglb.filekit.PlatformFile
import org.w3c.files.File
import org.w3c.files.FilePropertyBag

actual fun createTestFile(
    name: String,
    content: String
): PlatformFile {
    val bytes = content.encodeToByteArray()
    val file = File(
        fileBits = bytes.toBitsArray(),
        fileName = name,
        options = FilePropertyBag(type = "text/plain"),
    )
    return PlatformFile(file)
}
