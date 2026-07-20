@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.exceptions.BookmarkResolutionException
import io.github.vinceglb.filekit.exceptions.BookmarkResolutionFailure
import io.github.vinceglb.filekit.utils.toByteArray
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class PlatformFileMacOsBookmarkTest {
    @Test
    fun AppleBookmarkConfiguration_usesInjectedSandboxEntitlement() {
        val originalReader = AppleBookmarkEnvironment.entitlementReader
        try {
            AppleBookmarkEnvironment.entitlementReader = { true }
            assertEquals(
                expected = MacOsBookmarkKind.SecurityScoped,
                actual = appleBookmarkCreationConfiguration().kind,
            )

            AppleBookmarkEnvironment.entitlementReader = { false }
            assertEquals(
                expected = MacOsBookmarkKind.Regular,
                actual = appleBookmarkCreationConfiguration().kind,
            )
        } finally {
            AppleBookmarkEnvironment.entitlementReader = originalReader
        }
    }

    @Test
    fun PlatformFile_derivedPaths_inheritScopeOnlyWithinRoot() {
        val root = FileKit.projectDir.absoluteFile()

        val child = root / "child"
        val escaped = root / "../outside"

        assertEquals(expected = root.securityScopeUrl, actual = child.securityScopeUrl)
        assertEquals(expected = escaped.nsUrl, actual = escaped.securityScopeUrl)
    }

    @Test
    fun PlatformFile_bookmarkData_roundTripsCurrentBookmark() = runTest {
        val file = FileKit.projectDir / "src/nonWebTest/resources/hello.txt"

        val bookmarkData = file.bookmarkData()
        val resolution = PlatformFile.resolveBookmarkData(bookmarkData)

        assertEquals(expected = file.path, actual = resolution.file.path)
        assertFalse(resolution.isStale)
        assertFalse(resolution.shouldRefresh)
        assertFalse(bookmarkData.bytes.contentEquals(file.path.encodeToByteArray()))
    }

    @OptIn(ExperimentalForeignApi::class)
    @Test
    fun PlatformFile_resolveLegacyBookmarkData_recommendsRefresh() {
        val file = FileKit.projectDir / "src/nonWebTest/resources/hello.txt"
        val legacyData = requireNotNull(
            file.nsUrl.bookmarkDataWithOptions(
                options = 0u,
                includingResourceValuesForKeys = null,
                relativeToURL = null,
                error = null,
            ),
        )

        val resolution = PlatformFile.resolveBookmarkData(BookmarkData(legacyData.toByteArray()))

        assertEquals(expected = file.path, actual = resolution.file.path)
        assertFalse(resolution.isStale)
        kotlin.test.assertTrue(resolution.shouldRefresh)
    }

    @Test
    fun PlatformFile_resolveCorruptNativeBookmark_throwsInvalidDataFailure() {
        val corruptBookmark = MacOsBookmarkEnvelope(
            kind = MacOsBookmarkKind.Regular,
            payload = byteArrayOf(1, 2, 3),
        ).encode()

        val error = assertFailsWith<BookmarkResolutionException> {
            PlatformFile.resolveBookmarkData(corruptBookmark)
        }

        assertEquals(expected = BookmarkResolutionFailure.INVALID_DATA, actual = error.reason)
    }
}
