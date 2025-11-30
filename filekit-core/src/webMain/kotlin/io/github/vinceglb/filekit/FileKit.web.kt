package io.github.vinceglb.filekit

/**
 * Downloads a file in the browser.
 *
 * @param bytes The content of the file to download.
 * @param fileName The name of the downloaded file.
 */
public expect suspend fun FileKit.download(
    bytes: ByteArray,
    fileName: String,
)

/**
 * Downloads a [PlatformFile] in the browser.
 *
 * @param file The [PlatformFile] to download.
 * @param fileName The name of the downloaded file. Defaults to the file's name.
 */
public suspend fun FileKit.download(
    file: PlatformFile,
    fileName: String = file.name,
) {
    val bytes = file.readBytes()
    download(bytes = bytes, fileName = fileName)
}
