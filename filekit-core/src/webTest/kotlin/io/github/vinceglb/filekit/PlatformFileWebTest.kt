package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.mimeType.MimeType
import io.github.vinceglb.filekit.utils.createTestFile
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

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
}
