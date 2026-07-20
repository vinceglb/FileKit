package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.exceptions.BookmarkResolutionException
import io.github.vinceglb.filekit.exceptions.BookmarkResolutionFailure

internal enum class MacOsBookmarkKind(
    val encodedValue: Byte,
) {
    Regular(1),
    SecurityScoped(2),
    ;

    companion object {
        fun fromEncodedValue(value: Byte): MacOsBookmarkKind? = entries.firstOrNull { it.encodedValue == value }
    }
}

internal data class MacOsBookmarkEnvelope(
    val kind: MacOsBookmarkKind,
    val payload: ByteArray,
) {
    fun encode(): ByteArray = MAGIC + byteArrayOf(VERSION, kind.encodedValue) + payload

    companion object {
        private val MAGIC = byteArrayOf(0) + "FileKitBookmark".encodeToByteArray()
        private const val VERSION: Byte = 1
        private val HEADER_SIZE = MAGIC.size + 2

        fun decodeOrNull(bytes: ByteArray): MacOsBookmarkEnvelope? {
            if (bytes.size < MAGIC.size || !bytes.copyOfRange(0, MAGIC.size).contentEquals(MAGIC)) {
                return null
            }
            if (bytes.size < HEADER_SIZE) {
                throw BookmarkResolutionException(
                    reason = BookmarkResolutionFailure.INVALID_DATA,
                    message = "The FileKit bookmark envelope is incomplete",
                )
            }
            if (bytes[MAGIC.size] != VERSION) {
                throw BookmarkResolutionException(
                    reason = BookmarkResolutionFailure.UNSUPPORTED_VERSION,
                    message = "Unsupported FileKit bookmark version: ${bytes[MAGIC.size]}",
                )
            }
            val kind = MacOsBookmarkKind.fromEncodedValue(bytes[MAGIC.size + 1])
                ?: throw BookmarkResolutionException(
                    reason = BookmarkResolutionFailure.INCOMPATIBLE_PLATFORM,
                    message = "Unsupported FileKit bookmark payload: ${bytes[MAGIC.size + 1]}",
                )
            if (bytes.size == HEADER_SIZE) {
                throw BookmarkResolutionException(
                    reason = BookmarkResolutionFailure.INVALID_DATA,
                    message = "The FileKit bookmark payload is empty",
                )
            }
            return MacOsBookmarkEnvelope(
                kind = kind,
                payload = bytes.copyOfRange(HEADER_SIZE, bytes.size),
            )
        }
    }
}
