package io.github.vinceglb.picker.core

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.refTo
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithBytes
import platform.Foundation.writeToURL

@OptIn(ExperimentalForeignApi::class)
internal fun writeBytesArrayToNsUrl(bytes: ByteArray, nsUrl: NSURL) {
    // Get the NSData from the ByteArray
    val nsData = memScoped {
        NSData.dataWithBytes(
            bytes = bytes.refTo(0).getPointer(this),
            length = bytes.size.toULong()
        )
    }

    // Write the NSData to the NSURL
    nsData.writeToURL(nsUrl, true)
}
