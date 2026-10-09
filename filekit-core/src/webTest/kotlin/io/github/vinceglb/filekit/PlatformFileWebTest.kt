package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.exceptions.FileKitException
import io.github.vinceglb.filekit.mimeType.MimeType
import io.github.vinceglb.filekit.utils.createTestFile
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class PlatformFileWebTest {
    private val platformFile = createTestFile(
        name = "hello.txt",
        content = "Hello, World!",
    )

    @Test
    fun testPlatformFileName() {
        assertEquals(
            expected = "hello.txt",
            actual = platformFile.name,
        )
    }

    @Test
    fun testPlatformFileExtension() {
        assertEquals(
            expected = "txt",
            actual = platformFile.extension,
        )
    }

    @Test
    fun testPlatformFileExtensionWithNoExtension() {
        val platformFile = createTestFile(
            name = "hello",
            content = "Hello, World!",
        )

        assertEquals(
            expected = "",
            actual = platformFile.extension,
        )
    }

    @Test
    fun testPlatformFileNameWithoutExtension() {
        assertEquals(
            expected = "hello",
            actual = platformFile.nameWithoutExtension,
        )
    }

    @Test
    fun testPlatformFileSize() {
        assertEquals(
            expected = 13L,
            actual = platformFile.size(),
        )
    }

    @Test
    fun testRegularFilePathFallsBackToName() {
        assertEquals(
            expected = "hello.txt",
            actual = platformFile.path,
        )
    }

    @Test
    fun testPlatformFileReadBytes() = runTest {
        val bytes = platformFile.readBytes()
        assertEquals(
            expected = "Hello, World!",
            actual = bytes.decodeToString(),
        )
    }

    @Test
    fun testPlatformMimeType() {
        assertEquals(
            expected = MimeType.parse("text/plain"),
            actual = platformFile.mimeType(),
        )
    }

    @Test
    fun testPlatformFileTypeChecks() {
        assertTrue(platformFile.isRegularFile())
        assertFalse(platformFile.isDirectory())
    }

    @Test
    fun testRegularFileListFails() {
        assertFailsWith<FileKitException> {
            platformFile.list()
        }
    }

    @Test
    fun testWebDirectoryTree() {
        val rootFile = createTestFile(
            name = "root.txt",
            content = "Root",
            relativePath = "picked/root.txt",
        )
        val nestedFile = createTestFile(
            name = "nested.txt",
            content = "Nested",
            relativePath = "picked/folder/nested.txt",
        )

        val directory = PlatformFile.fromWebDirectoryFiles(
            listOf(rootFile.webFileWrapper(), nestedFile.webFileWrapper()),
        )!!

        assertEquals("picked", directory.name)
        assertEquals("picked", directory.path)
        assertTrue(directory.isDirectory())
        assertFalse(directory.isRegularFile())
        assertNull(directory.parent())

        val rootChildren = directory.list()
        assertEquals(listOf("root.txt", "folder"), rootChildren.map { it.name })

        val nestedDirectory = rootChildren.first { it.name == "folder" }
        assertEquals("picked/folder", nestedDirectory.path)
        assertEquals("picked", nestedDirectory.parent()?.name)
        assertTrue(nestedDirectory.isDirectory())

        val nestedChildren = nestedDirectory.list()
        assertEquals(listOf("nested.txt"), nestedChildren.map { it.name })
        assertEquals("folder", nestedChildren.single().parent()?.name)
        assertTrue(nestedChildren.single().isRegularFile())
    }

    @Test
    fun testWebDirectoryEmptyFileListReturnsNull() {
        assertNull(PlatformFile.fromWebDirectoryFiles(emptyList()))
    }

    @Test
    fun testWebDirectoryReadBytesFails() = runTest {
        val file = createTestFile(
            name = "root.txt",
            content = "Root",
            relativePath = "picked/root.txt",
        )
        val directory = PlatformFile.fromWebDirectoryFiles(listOf(file.webFileWrapper()))!!

        assertFailsWith<FileKitException> {
            directory.readBytes()
        }
    }

    @Test
    fun testWebDirectoryLastModifiedIsSynthetic() {
        val file = createTestFile(
            name = "root.txt",
            content = "Root",
            relativePath = "picked/root.txt",
        )
        val directory = PlatformFile.fromWebDirectoryFiles(listOf(file.webFileWrapper()))!!

        assertEquals(
            expected = Instant.fromEpochMilliseconds(0),
            actual = directory.lastModified(),
        )
    }

    @Test
    fun testOriginPrivateFileSystemPathFromRoot() {
        assertEquals(
            expected = "hello.txt",
            actual = "".appendOriginPrivateFileSystemPath("hello.txt"),
        )
    }

    @Test
    fun testOriginPrivateFileSystemPathFromDirectory() {
        assertEquals(
            expected = "folder/hello.txt",
            actual = "folder".appendOriginPrivateFileSystemPath("hello.txt"),
        )
    }

    @Test
    fun testOriginPrivateFileSystemRoot() = runTest {
        val root = PlatformFile.fromOriginPrivateFileSystem()

        assertTrue(root.isDirectory())
        assertFalse(root.isRegularFile())
        assertNull(root.parent())
    }

    @Test
    fun testOriginPrivateFileSystemStorageDirectories() = runTest {
        val filesDir = FileKit.filesDirectory()

        assertTrue(filesDir.isDirectory())
        assertEquals("", filesDir.path)
    }

    @Test
    fun testOriginPrivateFileUpdateRefreshesSnapshotAttributes() = runTest {
        val root = FileKit.filesDirectory()
        val name = "filekit-attributes-test.txt"
        val file = root.file(name, create = true)

        try {
            file.writeString("Hello")

            assertEquals(0L, file.size())

            file.update()

            assertEquals(5L, file.size())
            assertTrue(file.lastModified() > Instant.fromEpochMilliseconds(0))
        } finally {
            file.delete(mustExist = false)
        }
    }

    private fun PlatformFile.webFileWrapper(): WebFile.FileWrapper =
        assertIs<WebFile.FileWrapper>(webFile)
}
