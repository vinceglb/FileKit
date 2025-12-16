package io.github.vinceglb.filekit.sample.shared.ui.components

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.mimeType
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.nameWithoutExtension
import io.github.vinceglb.filekit.size

internal actual suspend fun getPlatformFileInfo(file: PlatformFile): List<InfoRow> = listOf(
    InfoRow("name", file.name),
    InfoRow("extension", file.extension),
    InfoRow("nameWithoutExtension", file.nameWithoutExtension),
    InfoRow("sizeBytes", file.size().toString()),
    InfoRow("mimeType", file.mimeType()?.toString() ?: "unknown"),
)
