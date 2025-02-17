package io.github.vinceglb.filekit

public expect suspend fun FileKit.download(
    bytes: ByteArray,
    fileName: String,
)

public suspend fun FileKit.download(
    file: PlatformFile,
    fileName: String = file.name ?: "file",
) {
    file.readBytes()?.let {
        download(bytes = it, fileName = fileName)
    }
}
