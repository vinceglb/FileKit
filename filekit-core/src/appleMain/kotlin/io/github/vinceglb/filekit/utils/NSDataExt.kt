package io.github.vinceglb.filekit.utils

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.refTo
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.create
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
public fun NSData.toByteArray(): ByteArray = let { nsData ->
    ByteArray(nsData.length.toInt()).apply {
        memcpy(this.refTo(0), nsData.bytes, nsData.length)
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
public fun ByteArray.toNSData(): NSData = usePinned {
    NSData.create(
        bytes = it.addressOf(0),
        length = this.size.toULong(),
    )
}
