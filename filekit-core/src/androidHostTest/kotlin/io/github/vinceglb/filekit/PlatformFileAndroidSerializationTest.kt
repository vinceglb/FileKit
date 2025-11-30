package io.github.vinceglb.filekit

import android.net.Uri
import kotlinx.serialization.json.Json
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlatformFileAndroidSerializationTest {
    private val json = Json { encodeDefaults = true }

    @Test
    fun serializeAndDeserializeUriBackedPlatformFile() {
        val uri = Uri.parse("content://example.provider/document/12345")
        val platformFile = PlatformFile(uri)

        val encoded = json.encodeToString(platformFile)
        val decoded = json.decodeFromString<PlatformFile>(encoded)

        assertEquals(platformFile.path, decoded.path)
        assertEquals(uri.toString(), decoded.toString())
        assertIs<AndroidFile.UriWrapper>(value = decoded.androidFile)
    }
}
