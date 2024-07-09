package io.github.vinceglb.filekit.core

public actual class PlatformInputStream {
    public actual fun hasBytesAvailable(): Boolean {
        throw IllegalStateException("JS does not support InputStreams")
    }

    public actual suspend fun readInto(buffer: ByteArray, maxBytes: Int): Int {
        throw IllegalStateException("JS does not support InputStreams")
    }
}