@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.mimeType.MimeType
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class PlatformFileLinuxTest {
    private val resourceDirectory = FileKit.projectDir / "src/nonWebTest/resources"
    private val textFile = resourceDirectory / "hello.txt"
    private val notExistingFile = resourceDirectory / "not-existing-file.pdf"

    @Test
    fun PlatformFile_absolutePath_absolutePath_isReturnedUnchanged() {
        assertEquals(
            expected = "/tmp/filekit/hello.txt",
            actual = PlatformFile("/tmp/filekit/hello.txt").absolutePath(),
        )
    }

    @Test
    fun PlatformFile_absolutePath_relativePathOfMissingFile_resolvesWithoutThrowing() {
        val absolutePath = PlatformFile("does-not-exist/hello.txt").absolutePath()

        assertTrue(absolutePath.startsWith("/"), "Expected an absolute path but got $absolutePath")
        assertTrue(absolutePath.endsWith("does-not-exist/hello.txt"), "Unexpected path $absolutePath")
    }

    @Test
    fun PlatformFile_absolutePath_missingSibling_resolvesToSameParentAsExistingFile() {
        val existingParent = textFile.absolutePath().substringBeforeLast('/')
        val missingParent = notExistingFile.absolutePath().substringBeforeLast('/')

        assertEquals(expected = existingParent, actual = missingParent)
    }

    @Test
    fun PlatformFile_absolutePath_emptyPath_resolvesToWorkingDirectory() {
        val absolutePath = PlatformFile("").absolutePath()

        assertTrue(absolutePath.startsWith("/"), "Expected an absolute path but got $absolutePath")
        assertTrue(
            absolutePath == "/" || !absolutePath.endsWith("/"),
            "Working directory should not have a trailing slash: $absolutePath",
        )
    }

    @Test
    fun PlatformFile_write_relativeDestination_doesNotThrow() = runTest {
        // Regression: absolutePath() used to require the file to exist, so isSameLogicalFileAs()
        // threw FileNotFoundException for any relative destination that had not been created yet.
        val destination = resourceDirectory / "linux-relative-destination.txt"

        try {
            destination write textFile
            assertTrue(destination.exists())
            assertEquals(expected = textFile.readString(), actual = destination.readString())
        } finally {
            if (destination.exists()) {
                destination.delete(mustExist = false)
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun PlatformFile_createdAt_returnsNull() {
        // Linux stat() has no birth time, and st_ctim is the status change time, not creation time
        assertNull(textFile.createdAt())
        assertNull(notExistingFile.createdAt())
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun PlatformFile_lastModified_existingFile_returnsNonEpochTime() {
        val lastModified = textFile.lastModified()

        assertTrue(
            lastModified > Instant.fromEpochMilliseconds(0L),
            "Expected a real modification time but got $lastModified",
        )
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun PlatformFile_lastModified_missingFile_returnsEpoch() {
        assertEquals(
            expected = Instant.fromEpochMilliseconds(0L),
            actual = notExistingFile.lastModified(),
        )
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun PlatformFile_lastModified_afterWrite_advances() = runTest {
        val file = resourceDirectory / "linux-last-modified.txt"

        try {
            file.writeString("first")
            val before = file.lastModified()

            file.writeString("second")
            val after = file.lastModified()

            assertTrue(after >= before, "Expected $after to be at or after $before")
        } finally {
            if (file.exists()) {
                file.delete(mustExist = false)
            }
        }
    }

    @Test
    fun PlatformFile_bookmarkData_roundTripsToAbsolutePath() = runTest {
        val bookmark = textFile.bookmarkData()
        val restored = PlatformFile.fromBookmarkData(bookmark)

        assertEquals(expected = textFile.absolutePath(), actual = restored.path)
    }

    @Test
    fun PlatformFile_resolveBookmarkData_reportsFreshBookmark() = runTest {
        val resolution = PlatformFile.resolveBookmarkData(textFile.bookmarkData())

        assertEquals(expected = textFile.absolutePath(), actual = resolution.file.path)
        assertFalse(resolution.isStale)
        assertFalse(resolution.shouldRefresh)
    }

    @Test
    fun PlatformFile_mimeType_blankExtension_returnsNull() {
        assertNull((resourceDirectory / "empty-file").mimeType())
    }

    @Test
    fun PlatformFile_startAccessingSecurityScopedResource_isAlwaysGranted() {
        assertTrue(textFile.startAccessingSecurityScopedResource())
        textFile.stopAccessingSecurityScopedResource()
    }

    @Test
    fun SystemMimeTypes_find_caseSensitiveEntry_prefersExactMatch() {
        // `*.C:cs` is C++ source, `*.c` without the flag is C source
        val mimeTypes = parseSharedMimeInfoGlobs(
            """
            50:text/x-c++src:*.C:cs
            50:text/x-csrc:*.c
            """.trimIndent(),
        )

        assertEquals(expected = MimeType.parse("text/x-c++src"), actual = mimeTypes.find("C"))
        assertEquals(expected = MimeType.parse("text/x-csrc"), actual = mimeTypes.find("c"))
    }

    @Test
    fun SystemMimeTypes_find_caseInsensitiveEntry_matchesAnyCase() {
        val mimeTypes = parseSharedMimeInfoGlobs("50:text/plain:*.txt")

        assertEquals(expected = MimeType.parse("text/plain"), actual = mimeTypes.find("txt"))
        assertEquals(expected = MimeType.parse("text/plain"), actual = mimeTypes.find("TXT"))
        assertEquals(expected = MimeType.parse("text/plain"), actual = mimeTypes.find("Txt"))
    }

    @Test
    fun SystemMimeTypes_find_higherWeightEntry_winsOverLaterDuplicate() {
        // globs2 is ordered by descending weight, so the first entry for an extension wins
        val mimeTypes = parseSharedMimeInfoGlobs(
            """
            60:application/gzip:*.gz
            40:application/x-gzip:*.gz
            """.trimIndent(),
        )

        assertEquals(expected = MimeType.parse("application/gzip"), actual = mimeTypes.find("gz"))
    }

    @Test
    fun SystemMimeTypes_find_compoundGlob_doesNotShadowSingleSuffix() {
        // `*.tar.gz` must not be registered under "gz", since `extension` is only the last segment
        val mimeTypes = parseSharedMimeInfoGlobs(
            """
            50:application/x-compressed-tar:*.tar.gz
            50:application/gzip:*.gz
            """.trimIndent(),
        )

        assertEquals(expected = MimeType.parse("application/gzip"), actual = mimeTypes.find("gz"))
        assertNull(mimeTypes.find("tar.gz"))
    }

    @Test
    fun SystemMimeTypes_find_malformedLines_areIgnored() {
        val mimeTypes = parseSharedMimeInfoGlobs(
            """
            # a comment
            not-a-valid-line
            50:missing-glob

            50:text/plain:*.txt
            50::*.bad
            """.trimIndent(),
        )

        assertEquals(expected = MimeType.parse("text/plain"), actual = mimeTypes.find("txt"))
        assertNull(mimeTypes.find("bad"))
    }

    @Test
    fun SystemMimeTypes_find_blankExtension_returnsNull() {
        val mimeTypes = parseSharedMimeInfoGlobs("50:text/plain:*.txt")

        assertNull(mimeTypes.find(""))
        assertNull(mimeTypes.find("   "))
    }

    @Test
    fun SystemMimeTypes_parseMimeTypesDatabase_mapsEveryExtensionOnTheLine() {
        val mimeTypes = parseMimeTypesDatabase(
            """
            # comment
            text/plain		txt text
            image/png		png
            broken-line-without-extension
            """.trimIndent(),
        )

        assertEquals(expected = MimeType.parse("text/plain"), actual = mimeTypes.find("txt"))
        assertEquals(expected = MimeType.parse("text/plain"), actual = mimeTypes.find("text"))
        assertEquals(expected = MimeType.parse("image/png"), actual = mimeTypes.find("PNG"))
        assertNull(mimeTypes.find("unknown"))
    }
}
