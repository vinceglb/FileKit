@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.exceptions.BookmarkResolutionException
import io.github.vinceglb.filekit.exceptions.BookmarkResolutionFailure
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MacOsBookmarkEnvelopeTest {
    @Test
    fun MacOsBookmarkEnvelope_encodeAndDecode_roundTripsPayload() {
        val expected = MacOsBookmarkEnvelope(
            kind = MacOsBookmarkKind.SecurityScoped,
            payload = byteArrayOf(1, 2, 3),
        )

        val actual = requireNotNull(MacOsBookmarkEnvelope.decodeOrNull(expected.encode()))

        assertEquals(expected = expected.kind, actual = actual.kind)
        assertTrue(expected.payload.contentEquals(actual.payload))
    }

    @Test
    fun MacOsBookmarkEnvelope_decodeLegacyPathStartingWithOldMagic_returnsNull() {
        val legacyPath = "FKBK-project/file.txt".encodeToByteArray()

        assertNull(MacOsBookmarkEnvelope.decodeOrNull(legacyPath))
    }

    @Test
    fun MacOsBookmarkEnvelope_decodeTruncatedCurrentMagic_throwsInvalidData() {
        val truncatedMagic = byteArrayOf(0) + "FileKit".encodeToByteArray()

        val error = assertFailsWith<BookmarkResolutionException> {
            MacOsBookmarkEnvelope.decodeOrNull(truncatedMagic)
        }

        assertEquals(expected = BookmarkResolutionFailure.INVALID_DATA, actual = error.reason)
    }
}
