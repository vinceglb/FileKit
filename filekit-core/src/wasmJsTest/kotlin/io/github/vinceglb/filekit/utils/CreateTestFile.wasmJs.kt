package io.github.vinceglb.filekit.utils

import io.github.vinceglb.filekit.PlatformFile
import org.w3c.files.File
import org.w3c.files.FilePropertyBag

@OptIn(ExperimentalWasmJsInterop::class)
actual fun createTestFile(
    name: String,
    content: String,
): PlatformFile {
    val jsArray = content.encodeToByteArray().toJsArray()
    val file = File(jsArray, name, FilePropertyBag(type = "text/plain"))
    return PlatformFile(file)
}
