@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit

import kotlinx.coroutines.test.runTest
import kotlinx.io.files.SystemTemporaryDirectory
import platform.posix.symlink
import platform.posix.unlink
import kotlin.random.Random
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

    private suspend fun withSymbolicLink(
        target: String,
        block: suspend (PlatformFile, PlatformFile) -> Unit,
    ) {
        val root = PlatformFile(SystemTemporaryDirectory) / "filekit-traversal-link-${Random.nextLong()}"
        val nested = root / "nested"
        val link = nested / "link"
        try {
            nested.createDirectories()
            (root / "top.txt").writeString("é")
            (nested / "leaf.txt").writeString("abc")
            assertEquals(0, symlink(target, link.path))
            block(root, link)
        } finally {
            // Unlink before cleaning up so even a failed assertion cannot traverse the cycle.
            unlink(link.path)
            root.delete(mustExist = false, recursively = true)
        }
    }
}
