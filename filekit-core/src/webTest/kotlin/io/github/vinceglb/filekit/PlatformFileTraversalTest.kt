@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.utils.createTestFile
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PlatformFileTraversalTest {
    @Test
    fun PlatformFile_walkNull_returnsEmptySequence() {
        val file: PlatformFile? = null

        assertTrue(file.walk().toList().isEmpty())
    }

    @Test
    fun PlatformFile_walkRegularFile_returnsOnlyThatFile() {
        val file = createTestFile(name = "file.txt", content = "content")

        assertEquals(listOf(file.path), file.walk().map { it.path }.toList())
    }

    @Test
    fun PlatformFile_walkEmptyDirectory_returnsEmptySequence() {
        assertTrue(createEmptyDirectory().walk().toList().isEmpty())
    }

    @Test
    fun PlatformFile_walkNestedTree_returnsEveryEntryOnceInDepthFirstOrder() {
        val directory = createDirectoryTree()
        val expected = setOf(
            "picked/top.txt",
            "picked/empty.txt",
            "picked/nested",
            "picked/nested/middle.txt",
            "picked/nested/deep",
            "picked/nested/deep/leaf.txt",
        )

        val paths = directory.walk().map { it.path }.toList()

        assertEquals(expected, paths.toSet())
        assertEquals(expected.size, paths.size)
        assertEquals(
            setOf("picked/nested/middle.txt", "picked/nested/deep", "picked/nested/deep/leaf.txt"),
            paths.drop(paths.indexOf("picked/nested") + 1).take(3).toSet(),
        )
        assertEquals("picked/nested/deep/leaf.txt", paths[paths.indexOf("picked/nested/deep") + 1])
    }

    @Test
    fun PlatformFile_walkWithDepthOne_returnsImmediateChildren() {
        val directory = createDirectoryTree()

        val paths = directory.walk(maxDepth = 1).map { it.path }.toList()

        assertEquals(setOf("picked/top.txt", "picked/empty.txt", "picked/nested"), paths.toSet())
        assertEquals(3, paths.size)
    }

    @Test
    fun PlatformFile_walkWithDepthTwo_excludesDeeperEntries() {
        val directory = createDirectoryTree()
        val expected = setOf(
            "picked/top.txt",
            "picked/empty.txt",
            "picked/nested",
            "picked/nested/middle.txt",
            "picked/nested/deep",
        )

        val paths = directory.walk(maxDepth = 2).map { it.path }.toList()

        assertEquals(expected, paths.toSet())
        assertEquals(expected.size, paths.size)
    }

    @Test
    fun PlatformFile_sizeRecursivelyNull_returnsMinusOne() = runTest {
        val file: PlatformFile? = null

        assertEquals(-1L, file.sizeRecursively())
    }

    @Test
    fun PlatformFile_sizeRecursivelyRegularFile_returnsSizeInBytes() = runTest {
        val file = createTestFile(name = "file.txt", content = "é")

        assertEquals(2L, file.sizeRecursively())
    }

    @Test
    fun PlatformFile_sizeRecursivelyEmptyFile_returnsZero() = runTest {
        val file = createTestFile(name = "empty.txt", content = "")

        assertEquals(0L, file.sizeRecursively())
    }

    @Test
    fun PlatformFile_sizeRecursivelyEmptyDirectory_returnsZero() = runTest {
        assertEquals(0L, createEmptyDirectory().sizeRecursively())
    }

    @Test
    fun PlatformFile_sizeRecursivelyNestedTree_sumsFileBytesOnly() = runTest {
        assertEquals(10L, createDirectoryTree().sizeRecursively())
    }

    private fun createDirectoryTree(): PlatformFile {
        val files = listOf(
            createTestFile(name = "top.txt", content = "é", relativePath = "picked/top.txt"),
            createTestFile(name = "empty.txt", content = "", relativePath = "picked/empty.txt"),
            createTestFile(name = "middle.txt", content = "abc", relativePath = "picked/nested/middle.txt"),
            createTestFile(name = "leaf.txt", content = "12345", relativePath = "picked/nested/deep/leaf.txt"),
        ).map { assertIs<WebFile.FileWrapper>(it.webFile) }

        return requireNotNull(PlatformFile.fromWebDirectoryFiles(files))
    }

    private fun createEmptyDirectory(): PlatformFile = PlatformFile(
        WebFile.DirectoryWrapper(name = "picked", path = "picked", parent = null),
    )
}
