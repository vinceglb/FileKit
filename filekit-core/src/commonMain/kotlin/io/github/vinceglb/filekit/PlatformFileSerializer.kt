package io.github.vinceglb.filekit

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * kotlinx.serialization serializer for [PlatformFile], encoded as its path string.
 *
 * Serialization keeps the path, not access rights: a file picked outside the app (an Android
 * `content://` file, a security-scoped file on Apple platforms) may not be readable once restored.
 * Use bookmark data for persistent access.
 *
 * Not supported on web targets (JS and Wasm): [serialize] and [deserialize] throw an
 * [IllegalStateException] there.
 */
public expect object PlatformFileSerializer : KSerializer<PlatformFile> {
    override val descriptor: SerialDescriptor

    override fun serialize(encoder: Encoder, value: PlatformFile)

    override fun deserialize(decoder: Decoder): PlatformFile
}
