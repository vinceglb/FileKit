package io.github.vinceglb.filekit

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

private const val UNSUPPORTED_MESSAGE =
    "PlatformFile serialization is not supported on Wasm JS targets"

public actual object PlatformFileSerializer : KSerializer<PlatformFile> {
    actual override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        serialName = "io.github.vinceglb.filekit.PlatformFile",
        kind = PrimitiveKind.STRING,
    )

    actual override fun deserialize(decoder: Decoder): PlatformFile {
        error(UNSUPPORTED_MESSAGE)
    }

    actual override fun serialize(encoder: Encoder, value: PlatformFile) {
        error(UNSUPPORTED_MESSAGE)
    }
}
