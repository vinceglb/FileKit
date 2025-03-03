package io.github.vinceglb.filekit.utils

import io.github.vinceglb.filekit.PlatformFile
import org.w3c.files.File

actual fun createTestFile(
    name: String,
    content: String
): PlatformFile {
    val bytes = content.encodeToByteArray()
    val file = File(
        fileBits = bytes.toBitsArray(),
        fileName = name,
    )
    return PlatformFile(file)
}
