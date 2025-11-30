package io.github.vinceglb.filekit

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class PlatformFileSerializationTest {
    private val resourceDirectory = FileKit.projectDir / "src/nonWebTest/resources"
    private val textFile = resourceDirectory / "hello.txt"

    private val json = Json { encodeDefaults = true }

    @Test
    fun serializeAndDeserializePlatformFile() {
        val encoded = json.encodeToString(textFile)
        val decodedPath = json.decodeFromString<String>(encoded)
        assertEquals(textFile.path, decodedPath)

        val decoded = json.decodeFromString<PlatformFile>(encoded)
        assertEquals(textFile.path, decoded.path)
    }

    @Test
    fun serializeWrappedPlatformFile() {
        val encoded = json.encodeToString(Wrapper.serializer(), Wrapper(textFile))
        val decoded = json.decodeFromString(Wrapper.serializer(), encoded)
        assertEquals(textFile.path, decoded.file.path)
    }

    @Serializable
    private data class Wrapper(
        val file: PlatformFile,
    )
}
