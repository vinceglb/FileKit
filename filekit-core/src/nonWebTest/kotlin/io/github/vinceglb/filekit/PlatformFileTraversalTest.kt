@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit

import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PlatformFileTraversalTest : PlatformFileTestBase() {
    // Canonical root: Robolectric's lstat reports a missing path containing "./" as a symbolic link.
    private val root = FileKit.projectDir.absoluteFile() / "src/nonWebTest/resources/walk-tree"

    @BeforeTest
    fun setUp() = runTest {
        root.delete(mustExist = false, recursively = true)
        (root / "a" / "b").createDirectories()
        (root / "empty").createDirectories()
        (root / "a" / "b" / "deep.txt").writeString("deep")
        (root / "a" / "a.txt").writeString("aa")
        (root / "top.txt").writeString("top")
    }

    @AfterTest
    fun tearDown() = runTest {
        root.delete(mustExist = false, recursively = true)
    }

    @Test
    fun PlatformFile_walk_yieldsDescendantsDepthFirst() {
        val paths = root.walk().map { it.relativePath() }.toList()

        assertEquals(
            expected = setOf("a", "a/b", "a/b/deep.txt", "a/a.txt", "empty", "top.txt"),
            actual = paths.toSet(),
        )
        assertEquals(paths.size, paths.toSet().size)
        // Depth-first: each entry's descendants come right after it.
        paths.forEachIndexed { index, path ->
            val descendantCount = paths.count { it.startsWith("$path/") }
            val following = paths.subList(index + 1, index + 1 + descendantCount)
            assertTrue(following.all { it.startsWith("$path/") }, "Not depth-first: $paths")
        }
    }

    @Test
    fun PlatformFile_walk_maxDepth_limitsDepth() {
        assertEquals(
            expected = setOf("a", "empty", "top.txt"),
            actual = root.walk(maxDepth = 1).map { it.relativePath() }.toSet(),
        )
        assertEquals(
            expected = setOf("a", "a/b", "a/a.txt", "empty", "top.txt"),
            actual = root.walk(maxDepth = 2).map { it.relativePath() }.toSet(),
        )
        assertFailsWith<IllegalArgumentException> { root.walk(maxDepth = 0) }
    }

    @Test
    fun PlatformFile_walk_regularFile_yieldsOnlyItself() = runTest {
        val file = root / "top.txt"

        assertEquals(listOf(file.path), file.walk().map { it.path }.toList())
        assertEquals(3L, file.sizeRecursively())
    }

    @Test
    fun PlatformFile_walk_emptyDirectory_isEmpty() = runTest {
        assertEquals(emptyList(), (root / "empty").walk().toList())
        assertEquals(0L, (root / "empty").sizeRecursively())
    }

    @Test
    fun PlatformFile_walk_missingPath_throws() = runTest {
        assertFailsWith<IOException> { (root / "missing").walk().toList() }
    }

    @Test
    fun PlatformFile_sizeRecursively_directory_sumsRegularFiles() = runTest {
        assertEquals(4L + 2L + 3L, root.sizeRecursively())
    }

    private fun PlatformFile.relativePath(): String =
        path.removePrefix(root.path).drop(1).replace('\\', '/')
}
