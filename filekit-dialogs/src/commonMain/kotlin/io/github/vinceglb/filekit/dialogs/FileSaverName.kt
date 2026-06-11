package io.github.vinceglb.filekit.dialogs

internal fun normalizeFileSaverExtension(extension: String?): String? = extension
    ?.trim()
    ?.trimStart('.')
    ?.takeIf { it.isNotBlank() }

internal fun normalizeFileSaverExtensions(extensions: Set<String>?): Set<String>? = extensions
    ?.mapNotNull(::normalizeFileSaverExtension)
    ?.toSet()
    ?.takeIf { it.isNotEmpty() }

internal fun buildFileSaverSuggestedName(
    suggestedName: String,
    extension: String?,
): String {
    val normalizedExtension = normalizeFileSaverExtension(extension)
    return when (normalizedExtension) {
        null -> suggestedName
        else -> "$suggestedName.$normalizedExtension"
    }
}

internal fun buildFileSaverAllowedFileTypes(
    defaultExtension: String?,
    allowedExtensions: Set<String>?,
): List<String>? {
    val normalizedDefault = normalizeFileSaverExtension(defaultExtension)
    val normalizedAllowed = normalizeFileSaverExtensions(allowedExtensions).orEmpty()
    return buildList {
        normalizedDefault?.let { add(it) }
        normalizedAllowed.forEach { if (it != normalizedDefault) add(it) }
    }.takeIf { it.isNotEmpty() }
}
