@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit

import com.sun.jna.Platform
import kotlinx.coroutines.test.runTest
import org.junit.Assume.assumeTrue
import java.nio.file.Files
import java.nio.file.LinkOption.NOFOLLOW_LINKS
import kotlin.io.path.createDirectory
import kotlin.io.path.createTempDirectory
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class PlatformFileDeletionTest {
    @Test
    fun PlatformFile_delete_danglingLink_isUnlinked() = runTest {
        val root = createTempDirectory("filekit-delete-link")
        val link = root.resolve("link")
        try {
            for (recursively in listOf(false, true)) {
                for (mustExist in listOf(false, true)) {
                    Files.createSymbolicLink(link, root.resolve("missing"))

                    PlatformFile(link.toFile()).delete(mustExist, recursively)

                    assertFalse(link.exists(NOFOLLOW_LINKS), "The dangling link must be unlinked")
                }
            }
        } finally {
            Files.deleteIfExists(link)
            Files.delete(root)
        }
    }

    @Test
    fun PlatformFile_deleteRecursively_danglingLink_removesDirectory() = runTest {
        val root = createTempDirectory("filekit-delete-dangling")
        val link = Files.createSymbolicLink(root.resolve("link"), root.resolve("missing"))
        try {
            PlatformFile(root.toFile()).delete(recursively = true)

            assertFalse(root.exists(NOFOLLOW_LINKS))
        } finally {
            Files.deleteIfExists(link)
            Files.deleteIfExists(root)
        }
    }

    @Test
    fun PlatformFile_deleteRecursively_windowsJunction_preservesTarget() = runTest {
        assumeTrue("Directory junctions are specific to Windows", Platform.isWindows())
        val root = createTempDirectory("filekit-delete-junction")
        val outside = root.resolve("outside").createDirectory()
        val treasure = outside.resolve("treasure.txt")
        treasure.writeText("must survive")
        val doomed = root.resolve("doomed").createDirectory()
        val junction = doomed.resolve("junction")
        try {
            val process = ProcessBuilder("cmd", "/c", "mklink", "/J", junction.toString(), outside.toString())
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader().use { it.readText() }
            assertEquals(0, process.waitFor(), "Could not create a junction: $output")

            PlatformFile(doomed.toFile()).delete(recursively = true)

            assertFalse(doomed.exists(NOFOLLOW_LINKS))
            assertEquals("must survive", treasure.readText())
        } finally {
            // Remove the junction itself before cleaning up; never traverse it, even on failure.
            Files.deleteIfExists(junction)
            root.toFile().deleteRecursively()
        }
    }
}
