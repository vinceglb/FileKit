package io.github.vinceglb.filekit.core

public actual class PlatformInputStream : AutoCloseable {
    public actual fun hasBytesAvailable(): Boolean {
        throw IllegalStateException("Wasm does not support InputStreams")
    }

    public actual suspend fun readInto(buffer: ByteArray, maxBytes: Int): Int {
        throw IllegalStateException("Wasm does not support InputStreams")
    }

    override fun close() {
        throw IllegalStateException("Wasm does not support InputStreams")
    }
}