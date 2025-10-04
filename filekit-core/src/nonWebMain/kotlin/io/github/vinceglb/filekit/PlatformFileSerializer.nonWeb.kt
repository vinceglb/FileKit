package io.github.vinceglb.filekit

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

public actual object PlatformFileSerializer : KSerializer<PlatformFile> {
    actual override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        serialName = "io.github.vinceglb.filekit.PlatformFile",
        kind = PrimitiveKind.STRING
    )

    actual override fun deserialize(decoder: Decoder): PlatformFile {
        val path = decoder.decodeString()
        return PlatformFile(path)
    }

    actual override fun serialize(encoder: Encoder, value: PlatformFile) {
        encoder.encodeString(value.path)
    }
}
