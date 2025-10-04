package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.mimeType.MimeType
import kotlinx.io.files.Path
import kotlin.test.Test
import kotlin.test.assertEquals

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
            actual = textFile.mimeType()
        )
        assertEquals(
            expected = MimeType.parse("image/png"),
            actual = imageFile.mimeType()
        )
        assertEquals(
            expected = null,
            actual = emptyFile.mimeType()
        )
        assertEquals(
            expected = MimeType.parse("application/pdf"),
            actual = notExistingFile.mimeType()
        )
        assertEquals(
            expected = null,
            actual = resourceDirectory.mimeType()
        )
    }
}
