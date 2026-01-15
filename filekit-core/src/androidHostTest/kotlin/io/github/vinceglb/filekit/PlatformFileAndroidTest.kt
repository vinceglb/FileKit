package io.github.vinceglb.filekit

import android.net.Uri
import io.github.vinceglb.filekit.exceptions.FileKitException
import io.github.vinceglb.filekit.exceptions.FileKitUriPathNotSupportedException
import io.github.vinceglb.filekit.mimeType.MimeType
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertIsNot
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlatformFileAndroidTest {
    @Before
    fun setup() {
        // Initialize FileKit with Robolectric's application context
        FileKit.manualFileKitCoreInitialization(RuntimeEnvironment.getApplication())
    }

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

    // Issue #415: Test `/` operator on FileWrapper (should work as before)
    @Test
    fun testDivOperatorOnFileWrapper() {
        val base = PlatformFile("/tmp/test")
        val child = base / "child.txt"

        assertIs<AndroidFile.FileWrapper>(child.androidFile)
        assertEquals("/tmp/test/child.txt", child.path)
    }

    // Issue #415: Test `/` operator on UriWrapper does NOT throw FileKitUriPathNotSupportedException
    // Note: In Robolectric, DocumentFile.fromTreeUri() returns null (no real SAF support),
    // so this test verifies that the ORIGINAL bug (FileKitUriPathNotSupportedException) is fixed.
    // Full integration testing requires a real Android device with SAF support.
    @Test
    fun testDivOperatorOnUriWrapper_noLongerThrowsPathNotSupportedException() {
        // Create a Uri-based PlatformFile (tree Uri format used by directory pickers)
        val uri = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3ADocuments")
        val base = PlatformFile(uri)

        // Before the fix, this would throw FileKitUriPathNotSupportedException
        // because PlatformFile(base, child) called base.toKotlinxIoPath() which throws for UriWrapper.
        // After the fix, it uses DocumentFile API instead, which fails in Robolectric
        // with a generic FileKitException (not FileKitUriPathNotSupportedException).
        val exception = assertFailsWith<FileKitException> {
            base / "backup.zip"
        }

        // Verify it's NOT the old exception type (the bug we fixed)
        assertIsNot<FileKitUriPathNotSupportedException>(exception)
        // The error message should be about DocumentFile access, not Path conversion
        assertTrue(
            exception.message?.contains("Could not access Uri as directory") == true ||
                exception.message?.contains("Could not create child file") == true,
        )
    }
}
