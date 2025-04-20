package io.github.vinceglb.sample.explorer.util

import android.os.Build
import androidx.documentfile.provider.DocumentFile
import io.github.vinceglb.filekit.AndroidFile
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.datetime.Instant
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes

actual fun PlatformFile.createdAt(): Instant? {
    return this.androidFile.let { androidFile ->
        when (androidFile) {
            is AndroidFile.FileWrapper -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val attributes = Files.readAttributes(
                        androidFile.file.toPath(),
                        BasicFileAttributes::class.java
                    )
                    val timestamp = attributes.creationTime().toMillis()
                    Instant.fromEpochMilliseconds(timestamp)
                } else {
                    // Fallback for older Android versions
                    null
                }
            }

            is AndroidFile.UriWrapper -> null
        }
    }
}

actual fun PlatformFile.lastModified(): Instant {
    val timestamp = this.androidFile.let { androidFile ->
        when (androidFile) {
            is AndroidFile.FileWrapper -> androidFile.file.lastModified()
            is AndroidFile.UriWrapper -> DocumentFile
                .fromSingleUri(FileKit.context, androidFile.uri)
                ?.lastModified()
                ?: throw IllegalStateException("Unable to get last modified date for URI")
        }
    }

    return Instant.fromEpochMilliseconds(timestamp)
}
