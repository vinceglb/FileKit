package io.github.vinceglb.filekit

import androidx.annotation.IntRange
import io.github.vinceglb.filekit.exceptions.FileKitException
import io.github.vinceglb.filekit.exceptions.FileKitNotInitializedException
import io.github.vinceglb.filekit.utils.Platform
import io.github.vinceglb.filekit.utils.PlatformUtil
import io.github.vinceglb.filekit.utils.calculateNewDimensions
import io.github.vinceglb.filekit.utils.div
import io.github.vinceglb.filekit.utils.toFile
import io.github.vinceglb.filekit.utils.toPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import java.awt.Graphics2D
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam

public actual object FileKit {
    private var _appId: String? = null
    public val appId: String
        get() = _appId ?: throw FileKitNotInitializedException()

    public fun init(appId: String) {
        _appId = appId
    }
}

public actual val FileKit.filesDir: PlatformFile
    get() = when (PlatformUtil.current) {
        Platform.Linux -> System.getenv("XDG_DATA_HOME")?.let { it.toPath() / appId }
            ?: (getEnv("HOME").toPath() / ".local" / "share" / appId)

        Platform.MacOS -> getEnv("HOME").toPath() / "Library" / "Application Support" / appId
        Platform.Windows -> getEnv("APPDATA").toPath() / appId
    }.also(Path::assertExists).let(::PlatformFile)

public actual val FileKit.cacheDir: PlatformFile
    get() = when (PlatformUtil.current) {
        Platform.Linux -> System.getenv("XDG_CACHE_HOME")?.let { it.toPath() / appId }
            ?: (getEnv("HOME").toPath() / ".cache" / appId)

        Platform.MacOS -> getEnv("HOME").toPath() / "Library" / "Caches" / appId
        Platform.Windows -> getEnv("LOCALAPPDATA").toPath() / appId / "Cache"
    }.also(Path::assertExists).let(::PlatformFile)

public actual val FileKit.databasesDir: PlatformFile
    get() = FileKit.filesDir / "databases"

public actual val FileKit.projectDir: PlatformFile
    get() = PlatformFile(".")

@Suppress("UnusedReceiverParameter")
public val FileKit.downloadDir: PlatformFile
    get() = when (PlatformUtil.current) {
        Platform.Linux -> System.getenv("XDG_DOWNLOAD_DIR")?.toPath()
            ?: (getEnv("HOME").toPath() / "Downloads")

        Platform.MacOS -> getEnv("HOME").toPath() / "Downloads"
        Platform.Windows -> getEnv("USERPROFILE").toPath() / "Downloads"
    }.also(Path::assertExists).let(::PlatformFile)

@Suppress("UnusedReceiverParameter")
public val FileKit.pictureDir: PlatformFile
    get() = when (PlatformUtil.current) {
        Platform.Linux -> System.getenv("XDG_PICTURES_DIR")?.toPath()
            ?: (getEnv("HOME").toPath() / "Pictures")

        Platform.MacOS -> getEnv("HOME").toPath() / "Pictures"
        Platform.Windows -> getEnv("USERPROFILE").toPath() / "Pictures"
    }.also(Path::assertExists).let(::PlatformFile)

private fun getEnv(key: String): String {
    return System.getenv(key)
        ?: throw IllegalStateException("Environment variable $key not found.")
}

private fun Path.assertExists() {
    if (!SystemFileSystem.exists(this)) {
        this.toFile().mkdirs()
    }
}

public actual suspend fun FileKit.compressImage(
    bytes: ByteArray,
    imageFormat: ImageFormat,
    @IntRange(from = 0, to = 100) quality: Int,
    maxWidth: Int?,
    maxHeight: Int?,
): ByteArray = withContext(Dispatchers.IO) {
    // Step 1: Decode the ByteArray to BufferedImage
    val inputStream = ByteArrayInputStream(bytes)
    val originalImage = ImageIO.read(inputStream)
        ?: throw FileKitException("Failed to read image")

    // Step 2: Calculate the new dimensions while maintaining aspect ratio
    val (newWidth, newHeight) = calculateNewDimensions(
        originalImage.width,
        originalImage.height,
        maxWidth,
        maxHeight
    )

    // Step 3: Resize the BufferedImage
    val resizedImage = BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB)
    val graphics: Graphics2D = resizedImage.createGraphics()
    graphics.drawImage(
        originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH),
        0, 0, newWidth, newHeight, null
    )
    graphics.dispose()

    // Step 4: Compress the BufferedImage
    val outputStream = ByteArrayOutputStream()
    val imageWriter = ImageIO.getImageWritersByFormatName(imageFormat.name.lowercase()).next()
    val imageWriteParam = imageWriter.defaultWriteParam
    imageWriteParam.compressionMode = ImageWriteParam.MODE_EXPLICIT
    imageWriteParam.compressionQuality = quality / 100.0f

    val output = ImageIO.createImageOutputStream(outputStream)
    imageWriter.output = output
    imageWriter.write(null, IIOImage(resizedImage, null, null), imageWriteParam)
    imageWriter.dispose()

    // Step 5: Return the compressed image as ByteArray
    outputStream.toByteArray()
}

public actual suspend fun FileKit.saveImageToGallery(
    bytes: ByteArray,
    filename: String
) {
    FileKit.pictureDir / filename write bytes
}