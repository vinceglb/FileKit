@file:OptIn(ExperimentalTime::class)

package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.exceptions.FileKitException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.io.Sink
import kotlinx.io.buffered
import kotlinx.io.readByteArray
import kotlinx.io.writeIntLe
import kotlinx.io.writeShortLe
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Writes this file, or this whole directory, into a zip archive at [destination].
 *
 * A directory is stored with its own name as the top-level entry, so unzipping recreates the folder
 * rather than spilling its contents into the current directory.
 *
 * @param destination The archive to create. An existing file is overwritten.
 */
public suspend infix fun PlatformFile.zipTo(destination: PlatformFile): Unit =
    listOf(this).zipTo(destination)

/**
 * Writes all of these files and directories into a single zip archive at [destination].
 *
 * Entries are named after each item, so zipping `report.pdf` and a `photos/` directory produces an
 * archive holding `report.pdf` and `photos/...`. Two items sharing a name would collide, so that is
 * rejected rather than silently written.
 *
 * Symbolic links are followed, as `zip -r` does, but a link that leads back into a directory
 * already being written is skipped so a cycle cannot run forever.
 *
 * @param destination The archive to create. An existing file is overwritten.
 */
public suspend infix fun List<PlatformFile>.zipTo(destination: PlatformFile) {
    if (isEmpty()) {
        throw FileKitException("Nothing to zip.")
    }

    val duplicate = map { it.name }.groupingBy { it }.eachCount().entries.firstOrNull { it.value > 1 }
    if (duplicate != null) {
        throw FileKitException("Cannot zip two entries both named \"${duplicate.key}\".")
    }

    withContext(Dispatchers.IO) {
        destination.sink().buffered().use { sink ->
            val writer = ZipWriter(sink)
            forEach { source ->
                if (!source.exists()) {
                    throw FileKitException("Cannot zip \"${source.name}\": it does not exist.")
                }
                writer.add(source, source.name, mutableSetOf())
            }
            writer.finish()
        }
    }
}

private class ZipWriter(private val sink: Sink) {
    private var offset = 0L
    private val entries = mutableListOf<CentralDirectoryEntry>()

    suspend fun add(source: PlatformFile, entryName: String, visited: MutableSet<String>) {
        if (source.isDirectory()) {
            // Absolute paths, not names: a link pointing back at an ancestor is the shape that
            // would otherwise recurse forever.
            if (!visited.add(source.absolutePath())) return
            writeDirectoryEntry("$entryName/", source.lastModified())
            source.list().forEach { child -> add(child, "$entryName/${child.name}", visited) }
            visited.remove(source.absolutePath())
        } else {
            writeFileEntry(source, entryName)
        }
    }

    private fun writeDirectoryEntry(entryName: String, modifiedAt: Instant) {
        val nameBytes = entryName.encodeToByteArray()
        val localHeaderOffset = offset
        val (time, date) = modifiedAt.toDosDateTime()

        writeLocalHeader(nameBytes, METHOD_STORED, time, date, crc = 0, size = 0)
        entries += CentralDirectoryEntry(nameBytes, METHOD_STORED, time, date, 0, 0, 0, localHeaderOffset, isDirectory = true)
    }

    private fun writeFileEntry(source: PlatformFile, entryName: String) {
        val nameBytes = entryName.encodeToByteArray()
        val localHeaderOffset = offset
        val (time, date) = source.lastModified().toDosDateTime()

        // Sizes and CRC are only known once the whole file has gone through the deflater, and the
        // sink cannot seek back to patch the header. Bit 3 says so and moves them after the data.
        writeLocalHeader(nameBytes, METHOD_DEFLATED, time, date, crc = 0, size = 0, useDataDescriptor = true)

        val crc = Crc32()
        val deflater = RawDeflater()
        var uncompressed = 0L
        var compressed = 0L
        try {
            source.source().buffered().use { input ->
                val chunk = ByteArray(COPY_CHUNK_BYTES)
                while (true) {
                    val read = input.readAtMostTo(chunk, 0, chunk.size)
                    if (read <= 0) break
                    crc.update(chunk, read)
                    uncompressed += read
                    compressed += writeRaw(deflater.deflate(chunk, read))
                }
                compressed += writeRaw(deflater.finish())
            }
        } finally {
            deflater.close()
        }

        writeRaw(
            buildBytes {
                writeIntLe(SIGNATURE_DATA_DESCRIPTOR)
                writeIntLe(crc.value.toInt())
                writeIntLe(compressed.toInt())
                writeIntLe(uncompressed.toInt())
            },
        )

        entries += CentralDirectoryEntry(
            nameBytes = nameBytes,
            method = METHOD_DEFLATED,
            time = time,
            date = date,
            crc = crc.value,
            compressedSize = compressed,
            uncompressedSize = uncompressed,
            localHeaderOffset = localHeaderOffset,
            isDirectory = false,
        )
    }

    private fun writeLocalHeader(
        nameBytes: ByteArray,
        method: Int,
        time: Int,
        date: Int,
        crc: Long,
        size: Long,
        useDataDescriptor: Boolean = false,
    ) {
        writeRaw(
            buildBytes {
                writeIntLe(SIGNATURE_LOCAL_HEADER)
                writeShortLe(VERSION_NEEDED.toShort())
                writeShortLe((if (useDataDescriptor) FLAG_UTF8 or FLAG_DATA_DESCRIPTOR else FLAG_UTF8).toShort())
                writeShortLe(method.toShort())
                writeShortLe(time.toShort())
                writeShortLe(date.toShort())
                writeIntLe(crc.toInt())
                writeIntLe(size.toInt())
                writeIntLe(size.toInt())
                writeShortLe(nameBytes.size.toShort())
                writeShortLe(0)
                write(nameBytes)
            },
        )
    }

    fun finish() {
        val centralDirectoryOffset = offset
        entries.forEach { entry ->
            writeRaw(
                buildBytes {
                    writeIntLe(SIGNATURE_CENTRAL_HEADER)
                    writeShortLe(VERSION_NEEDED.toShort())
                    writeShortLe(VERSION_NEEDED.toShort())
                    writeShortLe(
                        (if (entry.isDirectory) FLAG_UTF8 else FLAG_UTF8 or FLAG_DATA_DESCRIPTOR).toShort(),
                    )
                    writeShortLe(entry.method.toShort())
                    writeShortLe(entry.time.toShort())
                    writeShortLe(entry.date.toShort())
                    writeIntLe(entry.crc.toInt())
                    writeIntLe(entry.compressedSize.toInt())
                    writeIntLe(entry.uncompressedSize.toInt())
                    writeShortLe(entry.nameBytes.size.toShort())
                    writeShortLe(0)
                    writeShortLe(0)
                    writeShortLe(0)
                    writeShortLe(0)
                    writeIntLe(if (entry.isDirectory) EXTERNAL_ATTRIBUTES_DIRECTORY else 0)
                    writeIntLe(entry.localHeaderOffset.toInt())
                    write(entry.nameBytes)
                },
            )
        }
        val centralDirectorySize = offset - centralDirectoryOffset

        writeRaw(
            buildBytes {
                writeIntLe(SIGNATURE_END_OF_CENTRAL_DIRECTORY)
                writeShortLe(0)
                writeShortLe(0)
                writeShortLe(entries.size.toShort())
                writeShortLe(entries.size.toShort())
                writeIntLe(centralDirectorySize.toInt())
                writeIntLe(centralDirectoryOffset.toInt())
                writeShortLe(0)
            },
        )
    }

    private fun writeRaw(bytes: ByteArray): Int {
        if (bytes.isNotEmpty()) {
            sink.write(bytes)
            offset += bytes.size
        }
        return bytes.size
    }
}

private class CentralDirectoryEntry(
    val nameBytes: ByteArray,
    val method: Int,
    val time: Int,
    val date: Int,
    val crc: Long,
    val compressedSize: Long,
    val uncompressedSize: Long,
    val localHeaderOffset: Long,
    val isDirectory: Boolean,
)

private inline fun buildBytes(block: Sink.() -> Unit): ByteArray =
    kotlinx.io.Buffer().apply(block).readByteArray()

/**
 * Zip stores the modification time as MS-DOS date and time words: a two second resolution, and no
 * timezone, counted from 1980. Anything older is clamped to that floor, which is what the format
 * can represent.
 */
private fun Instant.toDosDateTime(): Pair<Int, Int> {
    val epochSeconds = epochSeconds
    val days = epochSeconds.floorDiv(SECONDS_PER_DAY)
    val secondOfDay = epochSeconds.mod(SECONDS_PER_DAY).toInt()

    // civil-from-days: shifts the epoch to March 1st so leap days land at the end of the cycle.
    val shifted = days + DAYS_FROM_0000_03_01_TO_EPOCH
    val era = (if (shifted >= 0) shifted else shifted - 146_096L) / 146_097L
    val dayOfEra = shifted - era * 146_097L
    val yearOfEra = (dayOfEra - dayOfEra / 1_460L + dayOfEra / 36_524L - dayOfEra / 146_096L) / 365L
    val dayOfYear = dayOfEra - (365L * yearOfEra + yearOfEra / 4L - yearOfEra / 100L)
    val monthIndex = (5L * dayOfYear + 2L) / 153L
    val day = (dayOfYear - (153L * monthIndex + 2L) / 5L + 1L).toInt()
    val month = (if (monthIndex < 10L) monthIndex + 3L else monthIndex - 9L).toInt()
    val year = (yearOfEra + era * 400L + if (month <= 2) 1L else 0L).toInt()

    if (year < DOS_EPOCH_YEAR) return 0 to (1 shl 5 or 1)

    val time = (secondOfDay / 3600 shl 11) or (secondOfDay % 3600 / 60 shl 5) or (secondOfDay % 60 / 2)
    val date = (year - DOS_EPOCH_YEAR shl 9) or (month shl 5) or day
    return time to date
}

/** Feeds bytes through raw deflate, the compression zip entries use. */
internal expect class RawDeflater() {
    /** Compresses the first [length] bytes of [input], returning whatever output is ready. */
    fun deflate(input: ByteArray, length: Int): ByteArray

    /** Ends the stream and returns the remaining output. */
    fun finish(): ByteArray

    fun close()
}

/**
 * CRC-32 as zip defines it. Written here rather than taken from each platform because the algorithm
 * is fixed and tiny, and one implementation means one thing to be wrong.
 */
internal class Crc32 {
    private var crc = 0xFFFFFFFFuL.toLong()

    fun update(input: ByteArray, length: Int) {
        for (index in 0 until length) {
            val position = (crc xor (input[index].toLong() and 0xFF)).toInt() and 0xFF
            crc = TABLE[position] xor (crc ushr 8)
        }
    }

    val value: Long
        get() = crc xor 0xFFFFFFFFuL.toLong()

    private companion object {
        val TABLE = LongArray(256) { index ->
            var value = index.toLong()
            repeat(8) {
                value = if (value and 1L != 0L) 0xEDB88320L xor (value ushr 1) else value ushr 1
            }
            value
        }
    }
}

private const val SIGNATURE_LOCAL_HEADER = 0x04034b50
private const val SIGNATURE_DATA_DESCRIPTOR = 0x08074b50
private const val SIGNATURE_CENTRAL_HEADER = 0x02014b50
private const val SIGNATURE_END_OF_CENTRAL_DIRECTORY = 0x06054b50
private const val VERSION_NEEDED = 20
private const val FLAG_DATA_DESCRIPTOR = 1 shl 3
private const val FLAG_UTF8 = 1 shl 11
private const val METHOD_STORED = 0
private const val METHOD_DEFLATED = 8
private const val EXTERNAL_ATTRIBUTES_DIRECTORY = 0x10
private const val COPY_CHUNK_BYTES = 64 * 1024
private const val SECONDS_PER_DAY = 86_400L
private const val DAYS_FROM_0000_03_01_TO_EPOCH = 719_468L
private const val DOS_EPOCH_YEAR = 1980
