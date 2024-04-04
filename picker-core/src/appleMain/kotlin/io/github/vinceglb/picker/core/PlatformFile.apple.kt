package io.github.vinceglb.picker.core

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.lastPathComponent
import platform.posix.memcpy

public actual data class PlatformFile(
	val nsUrl: NSURL,
) {
	public actual val name: String =
		nsUrl.lastPathComponent ?: ""

	public actual val path: String? =
		nsUrl.absoluteString

	@OptIn(ExperimentalForeignApi::class)
	public actual suspend fun readBytes(): ByteArray = withContext(Dispatchers.IO) {
		// Get the NSData from the NSURL
		val nsData = NSData.dataWithContentsOfURL(nsUrl)
			?: throw IllegalStateException("Failed to read data from $nsUrl")

		ByteArray(nsData.length.toInt()).apply {
			usePinned {
				memcpy(it.addressOf(0), nsData.bytes, nsData.length)
			}
		}
	}
}

public actual data class PlatformDirectory(
	val nsUrl: NSURL,
) {
	public actual val path: String? =
		nsUrl.absoluteString
}
