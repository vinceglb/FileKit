@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.exceptions.BookmarkResolutionException
import io.github.vinceglb.filekit.exceptions.BookmarkResolutionFailure
import io.github.vinceglb.filekit.utils.toByteArray
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.test.runTest
import platform.Foundation.NSURL
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PlatformFileMacOsBookmarkTest {
    @Test
    fun AppleBookmarkConfiguration_selectsUsingSandboxEntitlement() {
        assertEquals(
            expected = MacOsBookmarkKind.SecurityScoped,
            actual = appleBookmarkCreationConfiguration(isAppSandboxEnabled = true).kind,
        )
        assertEquals(
            expected = MacOsBookmarkKind.Regular,
            actual = appleBookmarkCreationConfiguration(isAppSandboxEnabled = false).kind,
        )
    }

    @Test
    fun PlatformFile_equality_usesUrlPath() {
        val first = PlatformFile(requireNotNull(NSURL(string = "file:///tmp/filekit-equality?version=1")))
        val second = PlatformFile(requireNotNull(NSURL(string = "file:///tmp/filekit-equality?version=2")))

        assertEquals(expected = first.nsUrl.path, actual = second.nsUrl.path)
        assertEquals(expected = first, actual = second)
        assertEquals(expected = first.hashCode(), actual = second.hashCode())
    }

    @Test
    fun PlatformFile_derivedPaths_inheritScopeOnlyWithinRoot() {
        val root = PlatformFile.withMacOsBookmarkLease(FileKit.projectDir.absoluteFile().nsUrl)

        val child = root / "child"
        val escaped = root / "../outside"

        assertEquals(expected = root.macOsBookmarkLease, actual = child.macOsBookmarkLease)
        assertNull(actual = escaped.macOsBookmarkLease)
    }

    @Test
    fun PlatformFile_nonExistingDescendant_underSymlinkedPrefix_inheritsScope() {
        val root = PlatformFile.withMacOsBookmarkLease(NSURL.fileURLWithPath("/tmp"))

        val child = root / "filekit-non-existing-descendant"

        assertEquals(expected = root.macOsBookmarkLease, actual = child.macOsBookmarkLease)
    }

    @Test
    fun PlatformFile_releaseBookmark_rejectsNewScopedAccessForDerivedFiles() {
        val root = PlatformFile.withMacOsBookmarkLease(FileKit.projectDir.absoluteFile().nsUrl)
        val child = root / "child"

        root.releaseBookmark()

        assertFailsWith<IllegalStateException> {
            child.startAccessingSecurityScopedResource()
        }
    }

    @Test
    fun PlatformFile_copy_preservesCapabilityOnlyWithinRoot() {
        val original = PlatformFile.withMacOsBookmarkLease(FileKit.projectDir.absoluteFile().nsUrl)
        val child = original.copy((original / "child").nsUrl)
        val escaped = original.copy((original / "../outside").nsUrl)

        original.releaseBookmark()
        val copied = original.copy()

        assertEquals(expected = original.nsUrl, actual = copied.component1())
        assertEquals(expected = original.macOsBookmarkLease, actual = child.macOsBookmarkLease)
        assertNull(actual = escaped.macOsBookmarkLease)
        assertFailsWith<IllegalStateException> {
            copied.startAccessingSecurityScopedResource()
        }
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
        assertTrue(resolution.shouldRefresh)
    }

    @Test
    fun PlatformFile_resolveStaleBookmarkData_recommendsRefresh() {
        val file = FileKit.projectDir / "src/nonWebTest/resources/hello.txt"
        val resolution = appleBookmarkResolution(
            payload = AppleBookmarkPayload(
                bytes = byteArrayOf(1, 2, 3),
                resolutionOptions = 0u,
                isLegacy = false,
            ),
            nativeResolution = AppleBookmarkNativeResolution(
                url = file.nsUrl,
                isStale = true,
            ),
        )

        assertEquals(expected = file.path, actual = resolution.file.path)
        assertTrue(resolution.isStale)
        assertTrue(resolution.shouldRefresh)
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
