package io.github.vinceglb.filekit

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

private const val UNSUPPORTED_MESSAGE =
    "PlatformFile serialization is not supported on web targets (JS and Wasm)"

/**
 * Web (JS and Wasm) implementation of [PlatformFileSerializer].
 *
 * A web [PlatformFile] wraps a browser `File` object that has no path to persist, so
 * [serialize] and [deserialize] always throw an [IllegalStateException]. The serializer
 * exists so that `@Serializable` classes holding a [PlatformFile] still compile in shared code.
 */
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
