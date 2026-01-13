package io.github.vinceglb.filekit.sample.shared.util

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.createdAt
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.isAbsolute
import io.github.vinceglb.filekit.isDirectory
import io.github.vinceglb.filekit.isRegularFile
import io.github.vinceglb.filekit.lastModified
import io.github.vinceglb.filekit.parent
import io.github.vinceglb.filekit.withScopedAccess

internal actual fun PlatformFile.platformDetailSections(): List<FileDetailSection> = withScopedAccess { file ->
    val locationItems = buildList {
        val absolutePath = file.absolutePath().takeIf { it.isNotBlank() }
        val parentPath = file.parent()?.absolutePath()?.takeIf { it.isNotBlank() }

        absolutePath?.let { add(FileDetailItem(label = "Absolute path", value = it)) }
        parentPath?.let { add(FileDetailItem(label = "Parent path", value = it)) }
    }

    val statusItems = listOf(
        FileDetailItem(label = "Exists", value = file.exists().toString()),
        FileDetailItem(label = "Is directory", value = file.isDirectory().toString()),
        FileDetailItem(label = "Is file", value = file.isRegularFile().toString()),
        FileDetailItem(label = "Is absolute", value = file.isAbsolute().toString()),
    )

    val dateItems = buildList {
        file.createdAt()?.let { add(FileDetailItem(label = "Created", value = it.toString())) }
        add(FileDetailItem(label = "Last modified", value = file.lastModified().toString()))
    }

    buildList {
        if (locationItems.isNotEmpty()) {
            add(FileDetailSection(title = "Location", items = locationItems))
        }
        add(FileDetailSection(title = "Status", items = statusItems))
        if (dateItems.isNotEmpty()) {
            add(FileDetailSection(title = "Dates", items = dateItems))
        }
    }
}
