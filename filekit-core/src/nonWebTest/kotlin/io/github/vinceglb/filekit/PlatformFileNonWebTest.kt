package io.github.vinceglb.filekit

import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlinx.io.files.FileNotFoundException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlatformFileNonWebTest {
    private val resourceDirectory = moduleRoot / "src/nonWebTest/resources"
    private val textFile = resourceDirectory / "hello.txt"
    private val imageFile = resourceDirectory / "compose-logo.png"
    private val emptyFile = resourceDirectory / "empty-file"
    private val notExistingFile = resourceDirectory / "not-existing-file.pdf"

    @Test
    fun testPlatformFileName() {
        assertEquals(expected = "hello.txt", actual = textFile.name)
        assertEquals(expected = "compose-logo.png", actual = imageFile.name)
        assertEquals(expected = "empty-file", actual = emptyFile.name)
        assertEquals(expected = "not-existing-file.pdf", actual = notExistingFile.name)
        assertEquals(expected = "resources", actual = resourceDirectory.name)
    }

    @Test
    fun testPlatformFileExtension() {
        assertEquals(expected = "txt", actual = textFile.extension)
        assertEquals(expected = "png", actual = imageFile.extension)
        assertEquals(expected = "", actual = emptyFile.extension)
        assertEquals(expected = "pdf", actual = notExistingFile.extension)
        assertEquals(expected = "", actual = resourceDirectory.extension)
    }

    @Test
    fun testPlatformFileNameWithoutExtension() {
        assertEquals(expected = "hello", actual = textFile.nameWithoutExtension)
        assertEquals(expected = "compose-logo", actual = imageFile.nameWithoutExtension)
        assertEquals(expected = "empty-file", actual = emptyFile.nameWithoutExtension)
        assertEquals(expected = "not-existing-file", actual = notExistingFile.nameWithoutExtension)
        assertEquals(expected = "resources", actual = resourceDirectory.nameWithoutExtension)
    }

    @Test
    fun testPlatformFileSize() {
        assertEquals(expected = 13L, actual = textFile.size())
        assertEquals(expected = 22_486L, actual = imageFile.size())
        assertEquals(expected = 0L, actual = emptyFile.size())
        assertEquals(expected = -1L, actual = notExistingFile.size())
        assertEquals(expected = -1L, actual = resourceDirectory.size())
    }

    @Test
    fun testPlatformFileParent() {
        assertEquals(expected = resourceDirectory.toPath(), actual = textFile.parent()?.toPath())
        assertEquals(expected = resourceDirectory.toPath(), actual = imageFile.parent()?.toPath())
        assertEquals(expected = resourceDirectory.toPath(), actual = emptyFile.parent()?.toPath())
        assertEquals(expected = resourceDirectory.toPath(), actual = notExistingFile.parent()?.toPath())
        assertEquals(
            expected = (moduleRoot / "src/nonWebTest").toPath(),
            actual = resourceDirectory.parent()?.toPath()
        )
    }

    @Test
    fun testPlatformFileIsRegularFile() {
        assertTrue { textFile.isRegularFile() }
        assertTrue { imageFile.isRegularFile() }
        assertTrue { emptyFile.isRegularFile() }
        assertFalse { notExistingFile.isRegularFile() }
        assertFalse { resourceDirectory.isRegularFile() }
    }

    @Test
    fun testPlatformFileIsDirectory() {
        assertFalse { textFile.isDirectory() }
        assertFalse { imageFile.isDirectory() }
        assertFalse { emptyFile.isDirectory() }
        assertFalse { notExistingFile.isDirectory() }
        assertTrue { resourceDirectory.isDirectory() }
    }

    @Test
    fun testPlatformFileExists() {
        assertTrue { textFile.exists() }
        assertTrue { imageFile.exists() }
        assertTrue { emptyFile.exists() }
        assertFalse { notExistingFile.exists() }
        assertTrue { resourceDirectory.exists() }
    }

    @Test
    fun testPlatformFileReadBytes() = runTest {
        val textFileContent = textFile.readBytes().decodeToString()
        assertEquals(expected = "Hello, World!", actual = textFileContent)

        val emptyFileContent = emptyFile.readBytes()
        assertTrue { emptyFileContent.isEmpty() }
    }

    @Test
    fun testPlatformFileReadBytesInvalidPath() = runTest {
        assertFailsWith<FileNotFoundException> { notExistingFile.readBytes() }
        assertFailsWith<IOException> { resourceDirectory.readBytes() }
    }

    @Test
    fun testPlatformFileWriteAndDelete() = runTest {
        val newFile = resourceDirectory / "new-file.txt"
        val content = "Hello Test!"

        // Write
        newFile.write(content.encodeToByteArray())
        assertEquals(expected = content, actual = newFile.readBytes().decodeToString())

        // Delete
        newFile.delete()
        assertFalse { newFile.exists() }
    }
}

expect val moduleRoot: PlatformFile
