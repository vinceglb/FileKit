package io.github.vinceglb.filekit.utils

import io.github.vinceglb.filekit.PlatformFile
import org.w3c.files.File

actual fun createTestFile(
    name: String,
    content: String
): PlatformFile {
    val jsArray = content.encodeToByteArray().toJsArray()
    val file = File(jsArray, name)
    return PlatformFile(file)
}
