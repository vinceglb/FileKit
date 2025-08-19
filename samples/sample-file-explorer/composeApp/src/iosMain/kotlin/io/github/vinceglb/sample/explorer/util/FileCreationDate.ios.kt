package io.github.vinceglb.sample.explorer.util

import io.github.vinceglb.filekit.PlatformFile
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDate
import platform.Foundation.NSURLContentModificationDateKey
import platform.Foundation.NSURLCreationDateKey
import platform.Foundation.timeIntervalSince1970
import kotlin.time.Instant

@OptIn(ExperimentalForeignApi::class)
actual fun PlatformFile.createdAt(): Instant? {
    val values = this.nsUrl.resourceValuesForKeys(listOf(NSURLCreationDateKey), null)
    val date = values?.get(NSURLCreationDateKey) as? NSDate
    return Instant.fromEpochSeconds(date?.timeIntervalSince1970?.toLong() ?: 0L)
}

@OptIn(ExperimentalForeignApi::class)
actual fun PlatformFile.lastModified(): Instant {
    val values = this.nsUrl.resourceValuesForKeys(listOf(NSURLContentModificationDateKey), null)
    val date = values?.get(NSURLContentModificationDateKey) as? NSDate
    return Instant.fromEpochSeconds(date?.timeIntervalSince1970?.toLong() ?: 0L)
}
