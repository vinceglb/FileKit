@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.exceptions.BookmarkResolutionException
import io.github.vinceglb.filekit.exceptions.BookmarkResolutionFailure
import io.github.vinceglb.filekit.exceptions.FileKitException
import io.github.vinceglb.filekit.utils.toByteArray
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.test.runTest
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import platform.Foundation.NSURL
import platform.posix.symlink
import platform.posix.unlink
import kotlin.random.Random
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
    fun PlatformFile_nonExistingPath_cannotEscapeScopeThroughParentSegments() {
        val root = PlatformFile.withMacOsBookmarkLease(NSURL.fileURLWithPath("/tmp/filekit-bookmark-root"))

        val escaped = root.copy(
            NSURL.fileURLWithPath("/tmp/filekit-bookmark-root/missing/../../outside"),
        )

        assertNull(actual = escaped.macOsBookmarkLease)
    }

    @OptIn(ExperimentalForeignApi::class)
    @Test
    fun PlatformFile_parentSegment_afterSymlink_cannotEscapeScope() {
        val testDirectory = Path("/tmp/filekit-bookmark-symlink-${Random.nextInt(0, Int.MAX_VALUE)}")
        val rootPath = Path(testDirectory, "root")
        val outsidePath = Path(testDirectory, "outside")
        val linkPath = Path(rootPath, "link")
        SystemFileSystem.createDirectories(rootPath)
        SystemFileSystem.createDirectories(outsidePath)
        assertEquals(expected = 0, actual = symlink(outsidePath.toString(), linkPath.toString()))
        try {
            val root = PlatformFile.withMacOsBookmarkLease(NSURL.fileURLWithPath(rootPath.toString()))
            val escaped = root.copy(
                NSURL.fileURLWithPath("$linkPath/../secret"),
            )

            assertNull(actual = escaped.macOsBookmarkLease)
        } finally {
            unlink(linkPath.toString())
            SystemFileSystem.delete(rootPath)
            SystemFileSystem.delete(outsidePath)
            SystemFileSystem.delete(testDirectory)
        }
    }

    @Test
    fun PlatformFile_releaseBookmark_rejectsNewScopedAccessForDerivedFiles() {
        val root = PlatformFile.withMacOsBookmarkLease(FileKit.projectDir.absoluteFile().nsUrl)
        val child = root / "child"

        root.releaseBookmark()

        assertFailsWith<FileKitException> {
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
        assertFailsWith<FileKitException> {
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
