@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit

import com.sun.jna.Platform
import kotlinx.coroutines.test.runTest
import org.junit.Assume.assumeTrue
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createDirectory
import kotlin.io.path.createTempDirectory
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlatformFileSymbolicLinkTest {
    @Test
    fun PlatformFile_isSymbolicLink_existingAndMissingTargets_reportsEntryType() = runTest {
        for (target in listOf("..", "../top.txt", "missing", "link")) {
            withSymbolicLink(target) { root, link ->
                assertTrue(link.isSymbolicLink())
                assertFalse(root.isSymbolicLink())
                assertFalse((root / "top.txt").isSymbolicLink())
                assertFalse((root / "missing").isSymbolicLink())
            }
        }
    }

    @Test
    fun PlatformFile_walkDirectoryLinkToAncestor_yieldsLinkWithoutRevisitingTarget() = runTest {
        withSymbolicLink("..") { root, link ->
            val expected = setOf(
                (root / "top.txt").path,
                (root / "nested").path,
                (root / "nested/leaf.txt").path,
                link.path,
            )

            val paths = root
                .walk()
                .take(expected.size + 1)
                .map { it.path }
                .toList()

            assertEquals(expected, paths.toSet())
            assertEquals(expected.size, paths.size)
        }
    }

    @Test
    fun PlatformFile_walkSymbolicLinkReceiver_returnsEmptySequence() = runTest {
        for (target in listOf("..", "../top.txt", "missing", "link")) {
            withSymbolicLink(target) { _, link ->
                assertTrue(
                    link
                        .walk()
                        .take(1)
                        .toList()
                        .isEmpty(),
                )
            }
        }
    }

    @Test
    fun PlatformFile_sizeRecursivelyDirectoryLinkToAncestor_countsFilesOnce() = runTest {
        withSymbolicLink("..") { root, link ->
            assertEquals(5L, root.sizeRecursively())
            assertEquals(0L, link.sizeRecursively())
        }
    }

    @Test
    fun PlatformFile_sizeRecursivelyFileLink_countsTargetSize() = runTest {
        withSymbolicLink("../top.txt") { root, link ->
            assertEquals(2L, link.sizeRecursively())
            assertEquals(7L, root.sizeRecursively())
        }
    }

    @Test
    fun PlatformFile_walkDanglingLink_yieldsEntryWithoutAddingUnknownSize() = runTest {
        withSymbolicLink("missing") { root, link ->
            val paths = root.walk().map { it.path }.toList()

            assertEquals(1, paths.count { it == link.path })
            assertEquals(4, paths.size)
            assertEquals(5L, root.sizeRecursively())
        }
    }

    @Test
    fun PlatformFile_walkWindowsJunctionToAncestor_yieldsJunctionWithoutRevisitingTarget() = runTest {
        withJunction { root, link ->
            val expected = setOf(
                (root / "top.txt").path,
                (root / "nested").path,
                (root / "nested/leaf.txt").path,
                link.path,
            )

            val paths = root
                .walk()
                .take(expected.size + 1)
                .map { it.path }
                .toList()

            assertTrue(link.isSymbolicLink())
            assertEquals(expected, paths.toSet())
            assertEquals(expected.size, paths.size)
            assertTrue(
                link
                    .walk()
                    .take(1)
                    .toList()
                    .isEmpty(),
            )
        }
    }

    @Test
    fun PlatformFile_sizeRecursivelyWindowsJunctionToAncestor_countsFilesOnce() = runTest {
        withJunction { root, link ->
            assertEquals(5L, root.sizeRecursively())
            assertEquals(0L, link.sizeRecursively())
        }
    }

    private suspend fun withSymbolicLink(
        target: String,
        block: suspend (PlatformFile, PlatformFile) -> Unit,
    ) {
        assumeTrue("Windows junctions are tested without requiring symlink privileges", !Platform.isWindows())
        withDirectoryTree { root, link ->
            Files.createSymbolicLink(link, Paths.get(target))
            block(PlatformFile(root.toFile()), PlatformFile(link.toFile()))
        }
    }

    private suspend fun withJunction(block: suspend (PlatformFile, PlatformFile) -> Unit) {
        assumeTrue("Directory junctions are specific to Windows", Platform.isWindows())
        withDirectoryTree { root, link ->
            val process = ProcessBuilder("cmd", "/c", "mklink", "/J", link.toString(), root.toString())
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader().use { it.readText() }
            assertEquals(0, process.waitFor(), "Could not create a junction: $output")
            block(PlatformFile(root.toFile()), PlatformFile(link.toFile()))
        }
    }

    private suspend fun withDirectoryTree(block: suspend (Path, Path) -> Unit) {
        val root = createTempDirectory("filekit-traversal-link")
        val nested = root.resolve("nested")
        val link = nested.resolve("link")
        try {
            nested.createDirectory()
            root.resolve("top.txt").writeText("é")
            nested.resolve("leaf.txt").writeText("abc")
            block(root, link)
        } finally {
            // Unlink before cleaning up so even a failed assertion cannot traverse the cycle.
            Files.deleteIfExists(link)
            root.toFile().deleteRecursively()
        }
    }
}
