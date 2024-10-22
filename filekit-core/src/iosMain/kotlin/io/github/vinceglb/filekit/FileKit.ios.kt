package io.github.vinceglb.filekit

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIImage
import platform.UIKit.UIImageWriteToSavedPhotosAlbum

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
public actual suspend fun FileKit.saveImageToGallery(
    bytes: ByteArray,
    baseName: String,
    extension: String
): Boolean = withContext(Dispatchers.IO) {
    val nsData = bytes.usePinned {
        NSData.create(
            bytes = it.addressOf(0),
            length = bytes.size.toULong()
        )
    }
    val uiImage = UIImage(nsData)
    UIImageWriteToSavedPhotosAlbum(uiImage, null, null, null)
    true
}
