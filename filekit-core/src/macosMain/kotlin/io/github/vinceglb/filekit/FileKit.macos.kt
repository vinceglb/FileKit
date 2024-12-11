package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.utils.calculateNewDimensions
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.AppKit.NSBitmapImageFileType
import platform.AppKit.NSBitmapImageRep
import platform.AppKit.NSImage
import platform.AppKit.NSImageCompressionFactor
import platform.AppKit.representationUsingType
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSMakeRect
import platform.Foundation.NSMakeSize
import platform.Foundation.NSPicturesDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

public actual suspend fun FileKit.saveImageToGallery(
    bytes: ByteArray,
    filename: String
): Boolean = FileKit.pictureDir / filename write bytes

@Suppress("UnusedReceiverParameter")
public val FileKit.pictureDir: PlatformFile
    get() = NSFileManager
        .defaultManager
        .URLsForDirectory(NSPicturesDirectory, NSUserDomainMask)
        .firstOrNull()
        ?.let { it as NSURL? }
        ?.let(::PlatformFile)
        ?: throw IllegalStateException("Could not find pictures directory")

@OptIn(ExperimentalForeignApi::class)
internal actual fun compress(
    nsData: NSData,
    quality: Int,
    maxWidth: Int?,
    maxHeight: Int?,
    compressFormat: CompressFormat,
): NSData? {
    val originalImage = NSImage(data = nsData)
    val (newWidth, newHeight) = calculateNewDimensions(
        originalImage.size.useContents { width }.toInt(),
        originalImage.size.useContents { height }.toInt(),
        maxWidth,
        maxHeight
    )

    val resizedImage = originalImage.resizeTo(newWidth / 2, newHeight / 2)

    val imageRep = NSBitmapImageRep.imageRepWithData(resizedImage.TIFFRepresentation!!)

    val storageType = when (compressFormat) {
        CompressFormat.JPEG -> NSBitmapImageFileType.NSBitmapImageFileTypeJPEG
        CompressFormat.PNG -> NSBitmapImageFileType.NSBitmapImageFileTypePNG
    }

    return imageRep?.representationUsingType(
        storageType = storageType,
        properties = mapOf(NSImageCompressionFactor to (quality / 100.0))
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun NSImage.resizeTo(newWidth: Int, newHeight: Int): NSImage {
    val newSize = NSMakeSize(newWidth.toDouble(), newHeight.toDouble())

    val newImage = NSImage(newSize)
    newImage.lockFocus()
    this.drawInRect(
        NSMakeRect(
            x = 0.0,
            y = 0.0,
            w = newSize.useContents { width },
            h = newSize.useContents { height }
        )
    )
    newImage.unlockFocus()

    return newImage
}
