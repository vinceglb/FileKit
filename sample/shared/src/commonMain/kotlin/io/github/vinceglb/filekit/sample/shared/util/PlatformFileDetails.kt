package io.github.vinceglb.filekit.sample.shared.util

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.mimeType
import io.github.vinceglb.filekit.mimeType.MimeType
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.nameWithoutExtension
import io.github.vinceglb.filekit.size

internal data class FileDetailItem(
    val label: String,
    val value: String,
)

internal data class FileDetailSection(
    val title: String,
    val items: List<FileDetailItem>,
)

internal data class PlatformFileDetails(
    val title: String,
    val subtitle: String?,
    val typeLabel: String,
    val isImage: Boolean,
    val sections: List<FileDetailSection>,
)

internal fun PlatformFile.toDetails(): PlatformFileDetails {
    val mimeType = mimeType()
    val baseSections = buildList {
        add(
            FileDetailSection(
                title = "Basics",
                items = listOf(
                    FileDetailItem(label = "Name", value = name),
                    FileDetailItem(label = "Name without extension", value = nameWithoutExtension),
                    FileDetailItem(label = "Extension", value = extension.ifBlank { "None" }),
                ),
            ),
        )

        val metadataItems = buildList {
            val size = size()
            val sizeValue = if (size >= 0) {
                "${size.formatBytes()} ($size B)"
            } else {
                "Unknown"
            }
            add(FileDetailItem(label = "Size", value = sizeValue))
            mimeType?.toString()?.let { add(FileDetailItem(label = "MIME type", value = it)) }
        }

        if (metadataItems.isNotEmpty()) {
            add(FileDetailSection(title = "Metadata", items = metadataItems))
        }
    }

    val extraSections = platformDetailSections()

    return PlatformFileDetails(
        title = name,
        subtitle = mimeType?.toString()?.takeIf { it.isNotBlank() },
        typeLabel = if (isDirectory()) "Directory" else "File",
        isImage = isImageFile(mimeType = mimeType, extension = extension),
        sections = baseSections + extraSections,
    )
}

internal expect fun PlatformFile.platformDetailSections(): List<FileDetailSection>

private fun isImageFile(
    mimeType: MimeType?,
    extension: String,
): Boolean {
    if (mimeType?.primaryType?.lowercase() == "image") {
        return true
    }

    val ext = extension.lowercase()
    val imageExtensions = setOf(
        "apng",
        "avif",
        "bmp",
        "gif",
        "heic",
        "heif",
        "jpg",
        "jpeg",
        "png",
        "svg",
        "tif",
        "tiff",
        "webp",
    )

    return ext in imageExtensions
}
