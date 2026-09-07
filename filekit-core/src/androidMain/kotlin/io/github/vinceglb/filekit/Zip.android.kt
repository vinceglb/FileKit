package io.github.vinceglb.filekit

import java.io.ByteArrayOutputStream
import java.util.zip.Deflater

internal actual class RawDeflater actual constructor() {
    // nowrap: zip entries carry raw deflate, without the zlib header and trailer.
    private val deflater = Deflater(Deflater.DEFAULT_COMPRESSION, true)
    private val buffer = ByteArray(DEFLATE_BUFFER_BYTES)

    actual fun deflate(input: ByteArray, length: Int): ByteArray {
        if (length <= 0) return ByteArray(0)
        deflater.setInput(input, 0, length)
        return drain { !deflater.needsInput() }
    }

    actual fun finish(): ByteArray {
        deflater.finish()
        return drain { !deflater.finished() }
    }

    actual fun close() {
        deflater.end()
    }

    private inline fun drain(hasMore: () -> Boolean): ByteArray {
        val output = ByteArrayOutputStream()
        while (hasMore()) {
            val produced = deflater.deflate(buffer, 0, buffer.size)
            if (produced <= 0) break
            output.write(buffer, 0, produced)
        }
        return output.toByteArray()
    }
}

private const val DEFLATE_BUFFER_BYTES = 64 * 1024
