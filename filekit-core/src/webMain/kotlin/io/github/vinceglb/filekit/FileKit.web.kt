package io.github.vinceglb.filekit

public expect suspend fun FileKit.download(
    bytes: ByteArray,
    fileName: String,
)

public suspend fun FileKit.download(
    file: PlatformFile,
    fileName: String = file.name,
) {
    val bytes = file.readBytes()
    download(bytes = bytes, fileName = fileName)
}
