package io.github.vinceglb.filekit

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import platform.Foundation.NSInputStream
import platform.posix.uint8_tVar

public actual class PlatformInputStream(private val nsInputStream: NSInputStream) : AutoCloseable {
    init { 
        nsInputStream.open() 
    }

    public actual fun hasBytesAvailable(): Boolean {
        return nsInputStream.hasBytesAvailable
    }

    @OptIn(ExperimentalForeignApi::class)
    public actual suspend fun readInto(buffer: ByteArray, maxBytes: Int): Int {
        return memScoped {
            val pointerBuffer: CPointer<uint8_tVar> = allocArray(maxBytes)
            val numRead = nsInputStream.read(pointerBuffer, maxBytes.toULong()).toInt()
            for (i in 0 until numRead) {
                buffer[i] = pointerBuffer[i].toByte()
            }
            numRead
        }
    }

    actual override fun close() {
        nsInputStream.close()
    }
}