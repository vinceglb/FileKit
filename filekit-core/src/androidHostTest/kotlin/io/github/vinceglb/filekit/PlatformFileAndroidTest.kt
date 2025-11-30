package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.mimeType.MimeType
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlatformFileAndroidTest {
    private val resourceDirectory = FileKit.projectDir / "src/nonWebTest/resources"
    private val textFile = resourceDirectory / "hello.txt"
    private val imageFile = resourceDirectory / "compose-logo.png"
    private val emptyFile = resourceDirectory / "empty-file"
    private val notExistingFile = resourceDirectory / "not-existing-file.pdf"

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
}
