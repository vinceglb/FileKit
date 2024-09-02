package io.github.vinceglb.filekit.core

public expect class PlatformInputStream : AutoCloseable {
    public fun hasBytesAvailable(): Boolean
    public suspend fun readInto(buffer: ByteArray, maxBytes: Int): Int
    public override fun close()
}