package io.github.vinceglb.filekit.sample.shared.ui.screens.filedetails.components

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.createdAt
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.isAbsolute
import io.github.vinceglb.filekit.isRegularFile
import io.github.vinceglb.filekit.lastModified
import io.github.vinceglb.filekit.mimeType
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.nameWithoutExtension
import io.github.vinceglb.filekit.parent
import io.github.vinceglb.filekit.path
import io.github.vinceglb.filekit.sample.shared.util.formatBytes
import io.github.vinceglb.filekit.sample.shared.util.isDirectory
import io.github.vinceglb.filekit.size

internal actual fun PlatformFile.toMetadataItems(): List<FileMetadataItem> = listOf(
    FileMetadataItem(
        label = "Name",
        value = this.name,
    ),
    FileMetadataItem(
        label = "Name without Extension",
        value = this.nameWithoutExtension,
        hidden = true,
    ),
    FileMetadataItem(
        label = "Extension",
        value = this.extension,
        hidden = true,
    ),
    FileMetadataItem(
        label = "Size",
        value = "${this.size().formatBytes()} - (${this.size()} bytes)",
    ),
    FileMetadataItem(
        label = "Mime Type",
        value = this.mimeType().toString(),
    ),
    FileMetadataItem(
        label = "Parent",
        value = this.parent()?.name ?: "-",
    ),
    FileMetadataItem(
        label = "Created At",
        value = this.createdAt().toString(),
        hidden = true,
    ),
    FileMetadataItem(
        label = "Updated At",
        value = this.lastModified().toString(),
    ),
    FileMetadataItem(
        label = "Path",
        value = this.path,
    ),
    FileMetadataItem(
        label = "Absolute Path",
        value = this.absolutePath(),
        hidden = true,
    ),
    FileMetadataItem(
        label = "Is a Directory",
        value = this.isDirectory().toString(),
        hidden = true,
    ),
    FileMetadataItem(
        label = "Is absolute",
        value = this.isAbsolute().toString(),
        hidden = true,
    ),
    FileMetadataItem(
        label = "Is a Regular File",
        value = this.isRegularFile().toString(),
        hidden = true,
    ),
    FileMetadataItem(
        label = "Exists",
        value = this.exists().toString(),
        hidden = true,
    ),
)
