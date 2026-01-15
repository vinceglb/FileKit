package io.github.vinceglb.filekit.sample.shared.ui.screens.filedetails.components

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.mimeType
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.nameWithoutExtension
import io.github.vinceglb.filekit.sample.shared.util.formatBytes
import io.github.vinceglb.filekit.size

internal actual fun PlatformFile.toMetadataItems(): List<FileMetadataItem> = listOf(
    FileMetadataItem(
        label = "Name",
        value = this.name,
    ),
    FileMetadataItem(
        label = "Name without Extension",
        value = this.nameWithoutExtension,
    ),
    FileMetadataItem(
        label = "Extension",
        value = this.extension,
    ),
    FileMetadataItem(
        label = "Size",
        value = this.size().formatBytes(),
    ),
    FileMetadataItem(
        label = "Mime Type",
        value = this.mimeType().toString(),
    ),
)
