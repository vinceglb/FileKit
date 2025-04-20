package io.github.vinceglb.sample.explorer.util

import io.github.vinceglb.filekit.PlatformFile
import kotlinx.datetime.Instant
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes

actual fun PlatformFile.createdAt(): Instant? {
    val attributes = Files.readAttributes(file.toPath(), BasicFileAttributes::class.java)
    val timestamp = attributes.creationTime().toMillis()
    return Instant.fromEpochMilliseconds(timestamp)
}

actual fun PlatformFile.lastModified(): Instant {
    val timestamp = this.file.lastModified()
    return Instant.fromEpochMilliseconds(timestamp)
}
