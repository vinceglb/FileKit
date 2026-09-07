@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")
@file:OptIn(ExperimentalForeignApi::class)

package io.github.vinceglb.filekit

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.test.runTest
import platform.posix.system
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The zip container is shared code, already round-tripped through java.util.zip on the JVM. What
 * only runs here is the zlib deflater behind [RawDeflater], so this hands the archive to the
 * system's own `unzip`, which checks every entry's CRC against the compressed bytes.
 */
class ZipMacosTest {
    @Test
    fun PlatformFile_zipTo_writesArchiveThatSystemUnzipVerifies() = runTest {
        val root = FileKit.projectDir / "build/zip-native-test"
        val tree = root / "photos"
        val nested = tree / "nested"
        val archive = root / "out.zip"
        try {
            nested.createDirectories()
            (tree / "a.txt").writeString("first")
            // Repetitive enough that the deflater has to emit back-references rather than literals.
            (nested / "b.txt").writeString("compress me ".repeat(2_000))

            tree zipTo archive

            val status = system("unzip -t '${archive.absolutePath()}' > /dev/null 2>&1")
            assertEquals(expected = 0, actual = status, "system unzip rejected the archive")
        } finally {
            listOf(nested / "b.txt", tree / "a.txt", nested, tree, archive, root)
                .forEach { it.delete(mustExist = false) }
        }
    }
}
