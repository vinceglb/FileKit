@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit

import kotlinx.coroutines.test.runTest
import kotlinx.io.files.SystemTemporaryDirectory
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PlatformFileTraversalTest : PlatformFileTestBase() {
    @Test
    fun PlatformFile_walkNull_returnsEmptySequence() {
        val file: PlatformFile? = null

        assertTrue(file.walk().toList().isEmpty())
    }

    @Test
    fun PlatformFile_walkWithNonPositiveDepth_returnsEmptySequence() = runTest {
        withTemporaryDirectory { root ->
            val file = root / "file.txt"
            file.writeString("content")

            for (maxDepth in listOf(0, -1)) {
                assertTrue(root.walk(maxDepth).toList().isEmpty())
                assertTrue(file.walk(maxDepth).toList().isEmpty())
            }
        }
    }

    @Test
    fun PlatformFile_walkRegularFile_returnsOnlyThatFile() = runTest {
        withTemporaryDirectory { root ->
            val file = root / "file.txt"
            file.writeString("content")

            assertEquals(listOf(file.path), file.walk().map { it.path }.toList())
        }
    }

    @Test
    fun PlatformFile_walkNestedTree_returnsEveryEntryOnceInDepthFirstOrder() = runTest {
        withTemporaryDirectory { root ->
            createTree(root)
            val nested = root / "nested"
            val deep = nested / "deep"
            val leaf = deep / "leaf.txt"
            val nestedEntries = setOf((nested / "middle.txt").path, deep.path, leaf.path)
            val expected = setOf(
                (root / "top.txt").path,
                (root / "empty.txt").path,
                (root / "empty-folder").path,
                nested.path,
            ) + nestedEntries

            val paths = root.walk().map { it.path }.toList()

            assertEquals(expected, paths.toSet())
            assertEquals(expected.size, paths.size)
            assertEquals(
                nestedEntries,
                paths.drop(paths.indexOf(nested.path) + 1).take(3).toSet(),
            )
            assertEquals(leaf.path, paths[paths.indexOf(deep.path) + 1])
        }
    }

    @Test
    fun PlatformFile_walkWithDepthOne_returnsImmediateChildren() = runTest {
        withTemporaryDirectory { root ->
            createTree(root)
            val expected = setOf(
                (root / "top.txt").path,
                (root / "empty.txt").path,
                (root / "nested").path,
                (root / "empty-folder").path,
            )

            val paths = root.walk(maxDepth = 1).map { it.path }.toList()

            assertEquals(expected, paths.toSet())
            assertEquals(expected.size, paths.size)
        }
    }

    @Test
    fun PlatformFile_walkWithDepthTwo_excludesDeeperEntries() = runTest {
        withTemporaryDirectory { root ->
            createTree(root)
            val nested = root / "nested"
            val expected = setOf(
                (root / "top.txt").path,
                (root / "empty.txt").path,
                (root / "empty-folder").path,
                nested.path,
                (nested / "middle.txt").path,
                (nested / "deep").path,
            )

            val paths = root.walk(maxDepth = 2).map { it.path }.toList()

            assertEquals(expected, paths.toSet())
            assertEquals(expected.size, paths.size)
        }
    }

    @Test
    fun PlatformFile_walkBeforeCreatingFile_listsAtConsumptionTime() = runTest {
        withTemporaryDirectory { root ->
            val files = root.walk()
            val lateFile = root / "late.txt"
            lateFile.writeString("late")

            assertEquals(listOf(lateFile.path), files.map { it.path }.toList())
        }
    }

    @Test
    fun PlatformFile_walkMissingPath_throwsIllegalStateException() = runTest {
        withTemporaryDirectory { root ->
            assertFailsWith<IllegalStateException> {
                (root / "missing").walk()
            }
        }
    }

    @Test
    fun PlatformFile_sizeRecursivelyNull_returnsMinusOne() = runTest {
        val file: PlatformFile? = null

        assertEquals(-1L, file.sizeRecursively())
    }

    @Test
    fun PlatformFile_sizeRecursivelyRegularFile_returnsSizeInBytes() = runTest {
        withTemporaryDirectory { root ->
            val file = root / "file.txt"
            file.writeString("é")

            assertEquals(2L, file.sizeRecursively())
        }
    }

    @Test
    fun PlatformFile_sizeRecursivelyEmptyFile_returnsZero() = runTest {
        withTemporaryDirectory { root ->
            val file = root / "empty.txt"
            file.writeString("")

            assertEquals(0L, file.sizeRecursively())
        }
    }

    @Test
    fun PlatformFile_sizeRecursivelyEmptyDirectory_returnsZero() = runTest {
        withTemporaryDirectory { root ->
            assertEquals(0L, root.sizeRecursively())
        }
    }

    @Test
    fun PlatformFile_sizeRecursivelyNestedTree_sumsFileBytesOnly() = runTest {
        withTemporaryDirectory { root ->
            createTree(root)

            assertEquals(10L, root.sizeRecursively())
        }
    }

    @Test
    fun PlatformFile_sizeRecursivelyMissingPath_throwsIllegalStateException() = runTest {
        withTemporaryDirectory { root ->
            assertFailsWith<IllegalStateException> {
                (root / "missing").sizeRecursively()
            }
        }
    }

    private suspend fun withTemporaryDirectory(block: suspend (PlatformFile) -> Unit) {
        val root = PlatformFile(SystemTemporaryDirectory) / "filekit-traversal-${Random.nextLong()}"
        try {
            root.createDirectories()
            block(root)
        } finally {
            root.delete(mustExist = false, recursively = true)
        }
    }

    private suspend fun createTree(root: PlatformFile) {
        val nested = root / "nested"
        val deep = nested / "deep"
        deep.createDirectories()
        (root / "empty-folder").createDirectories()
        (root / "top.txt").writeString("é")
        (root / "empty.txt").writeString("")
        (nested / "middle.txt").writeString("abc")
        (deep / "leaf.txt").writeString("12345")
    }
}
