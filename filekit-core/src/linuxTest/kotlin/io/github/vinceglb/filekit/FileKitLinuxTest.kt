@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.exceptions.FileKitException
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class FileKitLinuxTest {
    private val sandbox = FileKit.projectDir / "build/linux-test-sandbox"

    @AfterTest
    fun tearDown() {
        // Leave global FileKit state initialized to a known appId for any later test
        FileKit.init(appId = APP_ID)
    }

    @Test
    fun FileKit_projectDir_isTheWorkingDirectory() {
        assertEquals(expected = ".", actual = FileKit.projectDir.path)
    }

    @Test
    fun FileKit_filesDir_withCustomDirectory_usesItAndCreatesIt() {
        val filesDir = sandbox / "files"
        val cacheDir = sandbox / "cache"
        FileKit.init(appId = APP_ID, filesDir = filesDir, cacheDir = cacheDir)

        assertEquals(expected = filesDir.path, actual = FileKit.filesDir.path)
        assertTrue(FileKit.filesDir.exists(), "filesDir should be created on access")
        assertTrue(FileKit.filesDir.isDirectory())
    }

    @Test
    fun FileKit_cacheDir_withCustomDirectory_usesItAndCreatesIt() {
        val filesDir = sandbox / "files"
        val cacheDir = sandbox / "cache"
        FileKit.init(appId = APP_ID, filesDir = filesDir, cacheDir = cacheDir)

        assertEquals(expected = cacheDir.path, actual = FileKit.cacheDir.path)
        assertTrue(FileKit.cacheDir.exists(), "cacheDir should be created on access")
        assertTrue(FileKit.cacheDir.isDirectory())
    }

    @Test
    fun FileKit_databasesDir_isNestedUnderFilesDir() {
        val filesDir = sandbox / "files"
        FileKit.init(appId = APP_ID, filesDir = filesDir, cacheDir = sandbox / "cache")

        assertEquals(
            expected = (filesDir / "databases").path,
            actual = FileKit.databasesDir.path,
        )
    }

    @Test
    fun FileKit_filesDir_withoutCustomDirectory_resolvesUnderXdgDataHome() {
        FileKit.init(appId = APP_ID)

        val filesDir = FileKit.filesDir.path

        assertTrue(filesDir.startsWith("/"), "Expected an absolute path but got $filesDir")
        assertTrue(filesDir.endsWith("/$APP_ID"), "Expected the appId suffix but got $filesDir")
        assertTrue(FileKit.filesDir.isDirectory(), "filesDir should be created on access")
    }

    @Test
    fun FileKit_cacheDir_withoutCustomDirectory_resolvesUnderXdgCacheHome() {
        FileKit.init(appId = APP_ID)

        val cacheDir = FileKit.cacheDir.path

        assertTrue(cacheDir.startsWith("/"), "Expected an absolute path but got $cacheDir")
        assertTrue(cacheDir.endsWith("/$APP_ID"), "Expected the appId suffix but got $cacheDir")
    }

    @Test
    fun FileKit_appId_afterInitWithoutAppId_throws() {
        FileKit.init(filesDir = sandbox / "files", cacheDir = sandbox / "cache")

        // init(filesDir, cacheDir) clears the appId, so reading it must fail loudly
        assertFailsWith<FileKitException> { FileKit.appId }
    }

    @Test
    fun FileKit_userDirectoryOrNull_returnsAbsolutePathOrNull() {
        // The API is explicitly nullable, and resolution creates the directory, which can fail on a
        // locked down machine. Only assert the shape of a result that did resolve.
        FileKitUserDirectory.entries.forEach { type ->
            val directory = runCatching { FileKit.userDirectoryOrNull(type)?.path }
                .getOrNull() ?: return@forEach
            assertTrue(
                directory.startsWith("/"),
                "Expected an absolute path for $type but got $directory",
            )
        }
    }

    @Test
    fun FileKit_compressImage_isNotSupported() = runTest {
        assertFailsWith<FileKitException> {
            FileKit.compressImage(
                bytes = ByteArray(1),
                imageFormat = ImageFormat.JPEG,
                quality = 80,
            )
        }
    }

    private companion object {
        const val APP_ID = "io.github.vinceglb.filekit.linuxtest"
    }
}
