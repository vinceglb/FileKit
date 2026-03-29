package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.FileExt
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.utils.toJsArray
import org.w3c.files.File
import org.w3c.files.FilePropertyBag

@OptIn(ExperimentalWasmJsInterop::class)
internal actual fun createTestPlatformFile(name: String): PlatformFile {
    val jsArray = name.encodeToByteArray().toJsArray()
    val file = File(jsArray, name, FilePropertyBag(type = "text/plain"))
    return PlatformFile(file.unsafeCast<FileExt>())
}
