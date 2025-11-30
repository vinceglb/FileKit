package io.github.vinceglb.filekit

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

public expect object PlatformFileSerializer : KSerializer<PlatformFile> {
    override val descriptor: SerialDescriptor

    override fun serialize(encoder: Encoder, value: PlatformFile)

    override fun deserialize(decoder: Decoder): PlatformFile
}
