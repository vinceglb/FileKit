@file:Suppress("ktlint:standard:function-naming", "FunctionName")

package io.github.vinceglb.filekit.dialogs.compose.util

import androidx.compose.ui.graphics.ImageBitmap
import io.github.vinceglb.filekit.ImageFormat
import kotlinx.coroutines.test.runTest
import org.jetbrains.skia.Image
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ImageBitmapEncodingTest {
    @Test
    fun ImageBitmap_encodePng_returnsDecodableImage() = runTest {
        val bytes = ImageBitmap(2, 3).encodeToByteArray(ImageFormat.PNG)

        assertTrue(bytes.take(8) == listOf(137, 80, 78, 71, 13, 10, 26, 10).map { it.toByte() })
        val decoded = Image.makeFromEncoded(bytes)
        assertEquals(2, decoded.width)
        assertEquals(3, decoded.height)
        decoded.close()
    }

    @Test
    fun ImageBitmap_encodeJpeg_returnsDecodableImage() = runTest {
        val bytes = ImageBitmap(2, 3).encodeToByteArray(ImageFormat.JPEG, quality = 80)

        assertTrue(bytes.take(2) == listOf(255, 216).map { it.toByte() })
        val decoded = Image.makeFromEncoded(bytes)
        assertEquals(2, decoded.width)
        assertEquals(3, decoded.height)
        decoded.close()
    }
}
