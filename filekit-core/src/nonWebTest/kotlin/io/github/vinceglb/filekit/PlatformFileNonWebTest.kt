package io.github.vinceglb.filekit

import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlinx.io.files.FileNotFoundException
import kotlinx.io.files.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlatformFileNonWebTest {
    private val resourceDirectory = FileKit.projectDir / "src/nonWebTest/resources"
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
    fun testPlatformFileToKotlinxIoPath() {
        val path = Path("my/file.txt")
        val file = PlatformFile(path)
        assertEquals(expected = path, actual = file.toKotlinxIoPath())
    }

    @Test
    fun testPlatformFileParent() {
        assertEquals(expected = resourceDirectory.toKotlinxIoPath(), actual = textFile.parent()?.toKotlinxIoPath())
        assertEquals(expected = resourceDirectory.toKotlinxIoPath(), actual = imageFile.parent()?.toKotlinxIoPath())
        assertEquals(expected = resourceDirectory.toKotlinxIoPath(), actual = emptyFile.parent()?.toKotlinxIoPath())
        assertEquals(expected = resourceDirectory.toKotlinxIoPath(), actual = notExistingFile.parent()?.toKotlinxIoPath())
        assertEquals(
            expected = FileKit.projectDir.resolve("src/nonWebTest").toKotlinxIoPath(),
            actual = resourceDirectory.parent()?.toKotlinxIoPath()
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
        val textFileContent = textFile.readString()
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
        newFile.writeString(content)
        assertEquals(expected = content, actual = newFile.readString())

        // Delete
        newFile.delete()
        assertFalse { newFile.exists() }
    }

    @Test
    fun testPlatformFileWriteTruncation() = runTest {
        val newFile = resourceDirectory / "truncation-test.txt"
        val longContent = "This is a very long content that should be truncated when shorter content is written."
        val shortContent = "Short!"

        try {
            // Write long content first
            newFile.writeString(longContent)
            assertEquals(expected = longContent, actual = newFile.readString())
            assertEquals(expected = longContent.length.toLong(), actual = newFile.size())

            // Write shorter content - should truncate, not append
            newFile.writeString(shortContent)
            val actualContent = newFile.readString()
            assertEquals(expected = shortContent, actual = actualContent)
            assertEquals(expected = shortContent.length.toLong(), actual = newFile.size())

            // Verify no leftover bytes from previous content
            assertTrue("File should contain exactly the short content, no leftover bytes") {
                actualContent == shortContent
            }
        } finally {
            // Clean up
            if (newFile.exists()) {
                newFile.delete()
            }
        }
    }

    @Test
    fun testPlatformFileEquality() {
        val textFile2 = resourceDirectory / "hello.txt"
        val textFile3 = resourceDirectory / "hello.txt"
        val imageFile2 = resourceDirectory / "compose-logo.png"
        val emptyFile2 = resourceDirectory / "empty-file"
        val notExistingFile2 = resourceDirectory / "not-existing-file.pdf"

        assertTrue { textFile == textFile2 }
        assertTrue { textFile == textFile3 }
        assertTrue { textFile2 == textFile3 }
        assertTrue { imageFile == imageFile2 }
        assertTrue { emptyFile == emptyFile2 }
        assertTrue { notExistingFile == notExistingFile2 }
        assertFalse { textFile == imageFile }
    }

    @Test
    fun testPlatformFileToString() {
        assertEquals(expected = textFile.path, actual = textFile.toString())
        assertEquals(expected = imageFile.path, actual = imageFile.toString())
        assertEquals(expected = emptyFile.path, actual = emptyFile.toString())
        assertEquals(expected = notExistingFile.path, actual = notExistingFile.toString())
        assertEquals(expected = resourceDirectory.path, actual = resourceDirectory.toString())
    }
}
