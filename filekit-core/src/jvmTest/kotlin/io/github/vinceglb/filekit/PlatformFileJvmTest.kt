@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit

import com.sun.jna.Platform
import io.github.vinceglb.filekit.exceptions.BookmarkResolutionException
import io.github.vinceglb.filekit.exceptions.BookmarkResolutionFailure
import io.github.vinceglb.filekit.mimeType.MimeType
import kotlinx.coroutines.test.runTest
import kotlinx.io.files.Path
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlatformFileJvmTest {
    private val resourceDirectory = PlatformFile(Path("src/nonWebTest/resources"))
    private val textFile = PlatformFile(resourceDirectory, "hello.txt")
    private val imageFile = PlatformFile(resourceDirectory, "compose-logo.png")
    private val emptyFile = PlatformFile(resourceDirectory, "empty-file")
    private val notExistingFile = PlatformFile(resourceDirectory, "not-existing-file.pdf")

    @Test
    fun testPlatformMimeType() {
        assertEquals(
            expected = MimeType.parse("text/plain"),
            actual = textFile.mimeType(),
        )
        assertEquals(
            expected = MimeType.parse("image/png"),
            actual = imageFile.mimeType(),
        )
        assertEquals(
            expected = null,
            actual = emptyFile.mimeType(),
        )
        assertEquals(
            expected = MimeType.parse("application/pdf"),
            actual = notExistingFile.mimeType(),
        )
        assertEquals(
            expected = null,
            actual = resourceDirectory.mimeType(),
        )
    }

    @Test
    fun PlatformFile_resolveLegacyBookmarkData_recommendsRefresh() {
        val legacyPath = "src/nonWebTest/resources/hello.txt"
        val bookmarkData = BookmarkData(legacyPath.encodeToByteArray())

        val resolution = PlatformFile.resolveBookmarkData(bookmarkData)

        assertEquals(expected = legacyPath, actual = resolution.file.path)
        assertFalse(resolution.isStale)
        assertTrue(resolution.shouldRefresh)
        assertEquals(expected = resolution.file, actual = PlatformFile.fromBookmarkData(bookmarkData))
    }

    @Test
    fun PlatformFile_bookmarkDataOnMacos_roundTripsCurrentBookmark() = runTest {
        if (!Platform.isMac()) return@runTest

        val bookmarkData = textFile.bookmarkData()
        val resolution = PlatformFile.resolveBookmarkData(bookmarkData)

        assertEquals(expected = textFile.file.canonicalPath, actual = resolution.file.file.canonicalPath)
        assertFalse(resolution.isStale)
        assertFalse(resolution.shouldRefresh)
        assertFalse(bookmarkData.bytes.contentEquals(textFile.path.encodeToByteArray()))
    }

    @Test
    fun PlatformFile_resolveUnknownBookmarkVersion_throwsTypedFailure() {
        val unknownVersion = byteArrayOf('F'.code.toByte(), 'K'.code.toByte(), 'B'.code.toByte(), 'K'.code.toByte(), 99, 1)

        val error = assertFailsWith<BookmarkResolutionException> {
            PlatformFile.resolveBookmarkData(unknownVersion)
        }

        assertEquals(expected = BookmarkResolutionFailure.UNSUPPORTED_VERSION, actual = error.reason)
    }

    @Test
    fun PlatformFile_resolveUnknownBookmarkKind_throwsTypedFailure() {
        val unknownKind = byteArrayOf('F'.code.toByte(), 'K'.code.toByte(), 'B'.code.toByte(), 'K'.code.toByte(), 1, 99, 1)

        val error = assertFailsWith<BookmarkResolutionException> {
            PlatformFile.resolveBookmarkData(unknownKind)
        }

        assertEquals(expected = BookmarkResolutionFailure.INCOMPATIBLE_PLATFORM, actual = error.reason)
    }

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
    fun PlatformFile_resolveEmptyBookmarkEnvelope_throwsTypedFailure() {
        val emptyEnvelope = byteArrayOf('F'.code.toByte(), 'K'.code.toByte(), 'B'.code.toByte(), 'K'.code.toByte(), 1, 1)

        val error = assertFailsWith<BookmarkResolutionException> {
            PlatformFile.resolveBookmarkData(emptyEnvelope)
        }

        assertEquals(expected = BookmarkResolutionFailure.INVALID_DATA, actual = error.reason)
    }

    @Test
    fun PlatformFile_resolveInvalidLegacyPath_throwsTypedFailure() {
        val error = assertFailsWith<BookmarkResolutionException> {
            PlatformFile.resolveBookmarkData(byteArrayOf(0xC3.toByte()))
        }

        assertEquals(expected = BookmarkResolutionFailure.INVALID_DATA, actual = error.reason)
    }

    @Test
    fun PlatformFile_copy_preservesJvmDataClassSurface() {
        val original = PlatformFile(textFile.file)

        val copied = original.copy()

        assertEquals(expected = original, actual = copied)
        assertEquals(expected = original.file, actual = copied.component1())
    }

    @Test
    fun PlatformFile_copy_preservesCapabilityOnlyWithinRoot() {
        val root = createTempDirectory("filekit-bookmark-root").toFile()
        try {
            val access = RecordingBookmarkAccess(root)
            val original = PlatformFile.withMacOsBookmarkAccess(root, access)

            val child = original.copy(File(root, "child"))
            val outside = original.copy(File(root.parentFile, "outside"))

            child.startAccessingSecurityScopedResource()
            child.stopAccessingSecurityScopedResource()
            assertEquals(expected = 1, actual = access.startCount)
            assertEquals(expected = 1, actual = access.stopCount)

            outside.startAccessingSecurityScopedResource()
            outside.stopAccessingSecurityScopedResource()
            assertEquals(expected = 1, actual = access.startCount)
            assertEquals(expected = 1, actual = access.stopCount)
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun PlatformFile_nestedScopedAccess_balancesEverySuccessfulStart() {
        val file = createTempDirectory("filekit-bookmark-access").toFile()
        try {
            val access = RecordingBookmarkAccess(file)
            val platformFile = PlatformFile.withMacOsBookmarkAccess(file, access)

            platformFile.withScopedAccess {
                platformFile.withScopedAccess {}
            }

            assertEquals(expected = 2, actual = access.startCount)
            assertEquals(expected = 2, actual = access.stopCount)
        } finally {
            file.deleteRecursively()
        }
    }

    @Test
    fun PlatformFile_sourceAndSink_keepAccessUntilClose() {
        val directory = createTempDirectory("filekit-bookmark-stream").toFile()
        try {
            val nativeFile = File(directory, "content.txt").apply { writeText("hello") }
            val access = RecordingBookmarkAccess(directory)
            val platformFile = PlatformFile.withMacOsBookmarkAccess(nativeFile, access)

            val source = platformFile.source()
            assertEquals(expected = 1, actual = access.startCount)
            assertEquals(expected = 0, actual = access.stopCount)
            source.close()
            source.close()
            assertEquals(expected = 1, actual = access.stopCount)

            val sink = platformFile.sink()
            assertEquals(expected = 2, actual = access.startCount)
            assertEquals(expected = 1, actual = access.stopCount)
            sink.close()
            sink.close()
            assertEquals(expected = 2, actual = access.stopCount)
        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun MacOsBookmarkKind_usesInjectedSandboxEntitlement() {
        val originalReader = MacOsSandbox.entitlementReader
        try {
            MacOsSandbox.entitlementReader = { true }
            assertEquals(expected = MacOsBookmarkKind.SecurityScoped, actual = macOsBookmarkKindForCurrentProcess())

            MacOsSandbox.entitlementReader = { false }
            assertEquals(expected = MacOsBookmarkKind.Regular, actual = macOsBookmarkKindForCurrentProcess())
        } finally {
            MacOsSandbox.entitlementReader = originalReader
        }
    }

    private class RecordingBookmarkAccess(
        root: File,
    ) : MacOsBookmarkAccess {
        private val rootPath = root.canonicalFile.toPath()

        var startCount: Int = 0
            private set

        var stopCount: Int = 0
            private set

        override fun covers(file: File): Boolean = file.canonicalFile.toPath().startsWith(rootPath)

        override fun start(): Boolean {
            startCount += 1
            return true
        }

        override fun stop() {
            stopCount += 1
        }
    }
}
