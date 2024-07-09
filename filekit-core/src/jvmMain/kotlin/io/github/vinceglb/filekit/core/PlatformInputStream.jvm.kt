package io.github.vinceglb.filekit.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

public actual class PlatformInputStream(private val inputStream: InputStream) {

    public actual fun hasBytesAvailable(): Boolean {
        return inputStream.available() > 0
    }

    public actual suspend fun readInto(buffer: ByteArray, maxBytes: Int): Int =
        withContext(Dispatchers.IO) {
            inputStream.read(buffer, 0, maxBytes)
        }
}