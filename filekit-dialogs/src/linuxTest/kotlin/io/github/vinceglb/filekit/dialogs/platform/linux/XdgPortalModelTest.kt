@file:Suppress("ktlint:standard:function-naming")

package io.github.vinceglb.filekit.dialogs.platform.linux

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class XdgPortalModelTest {
    @Test
    fun buildPortalFileFilters_multipleExtensions_createsSupportedFilterPlusPerExtensionFilters() {
        val filters = buildPortalFileFilters(setOf("png", "pdf"))

        assertEquals(
            listOf(
                PortalFileFilter("Supported files", listOf("*.png", "*.pdf")),
                PortalFileFilter("png", listOf("*.png")),
                PortalFileFilter("pdf", listOf("*.pdf")),
            ),
            filters,
        )
    }

    @Test
    fun buildPortalFileFilters_noExtensions_createsOnlySupportedFilterWithoutPatterns() {
        val filters = buildPortalFileFilters(emptySet())

        assertEquals(listOf(PortalFileFilter("Supported files", emptyList())), filters)
    }

    @Test
    fun generatePortalHandleToken_returns32HexCharsWithoutDashes() {
        val token = generatePortalHandleToken()

        assertEquals(32, token.length)
        assertTrue(token.all { it in "0123456789abcdef" })
        assertTrue('-' !in token)
    }

    @Test
    fun resolvePortalResponse_success_returnsDecodedPaths() {
        val paths = resolvePortalResponse(
            response = 0,
            uris = listOf("file:///home/user/report%20draft.txt", "file:///tmp/photo.png"),
        )

        assertEquals(listOf("/home/user/report draft.txt", "/tmp/photo.png"), paths)
    }

    @Test
    fun resolvePortalResponse_successWithoutUris_returnsEmptyList() {
        assertEquals(emptyList(), resolvePortalResponse(response = 0, uris = null))
    }

    @Test
    fun resolvePortalResponse_cancelled_returnsNull() {
        assertNull(resolvePortalResponse(response = 1, uris = null))
    }

    @Test
    fun resolvePortalResponse_failure_throwsOperationalFailure() {
        val failure = assertFailsWith<LinuxXdgPortalException> {
            resolvePortalResponse(response = 2, uris = null)
        }

        assertEquals("The XDG portal ended the request with response code 2.", failure.message)
    }

    @Test
    fun resolvePortalResponse_unexpectedCode_throws() {
        assertFailsWith<IllegalStateException> {
            resolvePortalResponse(response = 42, uris = null)
        }
    }

    @Test
    fun resolvePortalResponse_nonFileUri_throwsOperationalFailure() {
        val failure = assertFailsWith<LinuxXdgPortalException> {
            resolvePortalResponse(
                response = 0,
                uris = listOf("http://example.com/file.txt"),
            )
        }

        assertTrue(failure.message.orEmpty().contains("http://example.com/file.txt"))
    }

    @Test
    fun portalUriToFilePath_fileUri_returnsDecodedPath() {
        assertEquals(
            "/home/user/my file.txt",
            portalUriToFilePath("file:///home/user/my%20file.txt"),
        )
    }

    @Test
    fun portalUriToFilePath_utf8EncodedPath_returnsDecodedPath() {
        assertEquals(
            "/home/user/café.png",
            portalUriToFilePath("file:///home/user/caf%C3%A9.png"),
        )
    }

    @Test
    fun portalUriToFilePath_localhostUri_returnsDecodedPath() {
        assertEquals(
            "/tmp/file.txt",
            portalUriToFilePath("file://localhost/tmp/file.txt"),
        )
    }

    @Test
    fun portalUriToFilePath_nonFileUri_returnsNull() {
        assertNull(portalUriToFilePath("http://example.com/file.txt"))
    }

    @Test
    fun percentDecode_encodedCharacters_returnsDecoded() {
        assertEquals(
            "/home/user/a b[1].txt",
            percentDecode("/home/user/a%20b%5B1%5D.txt"),
        )
    }

    @Test
    fun percentDecode_utf8EncodedCharacters_returnsDecoded() {
        assertEquals("/home/user/café.png", percentDecode("/home/user/caf%C3%A9.png"))
    }

    @Test
    fun percentDecode_literalNonAsciiCharacters_returnsDecoded() {
        assertEquals("/home/user/café.png", percentDecode("/home/user/café.png"))
    }

    @Test
    fun percentDecode_noEncoding_returnsInput() {
        assertEquals("/plain/path.txt", percentDecode("/plain/path.txt"))
    }

    @Test
    fun percentDecode_malformedEncoding_leavesItUntouched() {
        assertEquals("/a%2z.txt", percentDecode("/a%2z.txt"))
    }
}
