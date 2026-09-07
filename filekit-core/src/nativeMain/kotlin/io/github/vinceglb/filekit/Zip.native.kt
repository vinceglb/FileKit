@file:OptIn(ExperimentalForeignApi::class)

package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.exceptions.FileKitException
import kotlinx.cinterop.Arena
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.usePinned
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import platform.zlib.ZLIB_VERSION
import platform.zlib.Z_DEFAULT_COMPRESSION
import platform.zlib.Z_DEFAULT_STRATEGY
import platform.zlib.Z_DEFLATED
import platform.zlib.Z_FINISH
import platform.zlib.Z_NO_FLUSH
import platform.zlib.Z_OK
import platform.zlib.Z_STREAM_ERROR
import platform.zlib.deflateEnd
import platform.zlib.deflateInit2_
import platform.zlib.z_stream
import platform.zlib.deflate as zlibDeflate

internal actual class RawDeflater actual constructor() {
    private val arena = Arena()
    private val stream = arena.alloc<z_stream>()
    private val buffer = ByteArray(DEFLATE_BUFFER_BYTES)
    private var closed = false

    init {
        // A negative windowBits asks zlib for raw deflate, without the zlib header and trailer,
        // which is what a zip entry holds. deflateInit2 itself is a macro, so the underlying
        // deflateInit2_ is what cinterop exposes.
        val result = deflateInit2_(
            strm = stream.ptr,
            level = Z_DEFAULT_COMPRESSION,
            method = Z_DEFLATED,
            windowBits = -MAX_WINDOW_BITS,
            memLevel = DEFAULT_MEM_LEVEL,
            strategy = Z_DEFAULT_STRATEGY,
            version = ZLIB_VERSION,
            stream_size = sizeOf<z_stream>().convert(),
        )
        if (result != Z_OK) {
            arena.clear()
            throw FileKitException("Could not start compression: zlib returned $result")
        }
    }

    actual fun deflate(input: ByteArray, length: Int): ByteArray {
        if (length <= 0) return ByteArray(0)
        return input.usePinned { pinnedInput ->
            stream.next_in = pinnedInput.addressOf(0).reinterpret()
            stream.avail_in = length.convert()
            pump(Z_NO_FLUSH)
        }
    }

    actual fun finish(): ByteArray {
        stream.next_in = null
        stream.avail_in = 0u
        return pump(Z_FINISH)
    }

    actual fun close() {
        if (closed) return
        closed = true
        deflateEnd(stream.ptr)
        arena.clear()
    }

    /**
     * Runs zlib until it stops filling the output buffer. A full buffer means there is more to
     * come, so the loop only ends once zlib leaves room, which is its signal that it is done for
     * the input it has.
     */
    private fun pump(flush: Int): ByteArray {
        val output = Buffer()
        while (true) {
            val produced = buffer.usePinned { pinnedOutput ->
                stream.next_out = pinnedOutput.addressOf(0).reinterpret()
                stream.avail_out = buffer.size.convert()
                val result = zlibDeflate(stream.ptr, flush)
                if (result == Z_STREAM_ERROR) {
                    throw FileKitException("Compression failed: zlib returned $result")
                }
                buffer.size - stream.avail_out.toInt()
            }
            if (produced > 0) {
                output.write(buffer, 0, produced)
            }
            if (produced < buffer.size) break
        }
        return output.readByteArray()
    }
}

private const val DEFLATE_BUFFER_BYTES = 64 * 1024
private const val DEFAULT_MEM_LEVEL = 8

// zlib's MAX_WBITS is a macro, so cinterop does not surface it. 15 is the largest window zlib
// supports and the value zip entries are written with.
private const val MAX_WINDOW_BITS = 15
