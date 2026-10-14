@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit

import kotlinx.coroutines.test.runTest
import kotlinx.io.files.SystemTemporaryDirectory
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlatformFileCopyRecursivelyTest : PlatformFileTestBase() {
    @Test
    fun PlatformFile_copyRecursivelyFileToNewPath_createsRenamedCopyAndPreservesSource() = runTest {
        withTemporaryDirectory { root ->
            val source = root / "source.txt"
            val target = root / "renamed.txt"
            source.writeString("source content")

            source.copyRecursively(target)

            assertEquals("source content", target.readString())
            assertEquals("source content", source.readString())
        }
    }

    @Test
    fun PlatformFile_copyRecursivelyFileToMissingParents_createsParentsAndPreservesBytes() = runTest {
        withTemporaryDirectory { root ->
            val source = root / "source.bin"
            val target = root / "missing" / "nested" / "renamed.bin"
            val content = byteArrayOf(0, 1, -1, 127, -128)
            source.write(content)

            source.copyRecursively(target)

            assertContentEquals(content, target.readBytes())
            assertContentEquals(content, source.readBytes())
        }
    }

    @Test
    fun PlatformFile_copyRecursivelyDirectoryToNewRoot_preservesTreeWithoutAddingSourceFolder() = runTest {
        withTemporaryDirectory { root ->
            val source = root / "source"
            val target = root / "missing" / "target"
            createTree(source)

            source.copyRecursively(target)

            assertCopiedTree(target)
            assertCopiedTree(source)
            assertFalse((target / source.name).exists())
        }
    }

    @Test
    fun PlatformFile_copyRecursivelyEmptyDirectoryToNewRoot_createsEmptyDestination() = runTest {
        withTemporaryDirectory { root ->
            val source = root / "source"
            val target = root / "missing" / "target"
            source.createDirectories()

            source.copyRecursively(target)

            assertTrue(target.isDirectory())
            assertTrue(target.list().isEmpty())
            assertTrue(source.isDirectory())
        }
    }

    @Test
    fun PlatformFile_copyRecursivelyDirectoryToExistingRoot_preservesTree() = runTest {
        withTemporaryDirectory { root ->
            val source = root / "source"
            val target = root / "target"
            createTree(source)
            target.createDirectories()

            source.copyRecursively(target)

            assertCopiedTree(target)
            assertCopiedTree(source)
            assertFalse((target / source.name).exists())
        }
    }

    @Test
    fun PlatformFile_copyRecursivelyFileWithoutOverwrite_skipsAndPreservesExistingDestination() = runTest {
        withTemporaryDirectory { root ->
            val source = root / "source.txt"
            val target = root / "target.txt"
            source.writeString("new content")
            target.writeString("original content")

            source.copyRecursively(target)

            assertEquals("original content", target.readString())
            assertEquals("new content", source.readString())
        }
    }

    @Test
    fun PlatformFile_copyRecursivelyFileWithOverwrite_replacesDestinationAndPreservesSource() = runTest {
        withTemporaryDirectory { root ->
            val source = root / "source.txt"
            val target = root / "target.txt"
            source.writeString("new")
            target.writeString("long original content")

            source.copyRecursively(target, overwrite = true)

            assertEquals("new", target.readString())
            assertEquals("new", source.readString())
        }
    }

    @Test
    fun PlatformFile_copyRecursivelyDirectoryWithoutOverwrite_preservesAllConflictingFiles() = runTest {
        withTemporaryDirectory { root ->
            val source = root / "source"
            val target = root / "target"
            source.createDirectories()
            target.createDirectories()
            for (name in listOf("a.txt", "b.txt")) {
                (source / name).writeString("new $name")
                (target / name).writeString("original $name")
            }

            source.copyRecursively(target)

            for (name in listOf("a.txt", "b.txt")) {
                assertEquals("original $name", (target / name).readString())
                assertEquals("new $name", (source / name).readString())
            }
        }
    }

    @Test
    fun PlatformFile_copyRecursivelyDirectoryWithOverwrite_mergesAndPreservesUnrelatedFiles() = runTest {
        withTemporaryDirectory { root ->
            val source = root / "source"
            val target = root / "target"
            createTree(source)
            (target / "nested").createDirectories()
            (target / "top.txt").writeString("old top")
            (target / "nested" / "leaf.txt").writeString("old leaf")
            (target / "keep.txt").writeString("keep root")
            (target / "nested" / "keep.txt").writeString("keep nested")

            source.copyRecursively(target, overwrite = true)

            assertCopiedTree(target, includesUnrelatedFiles = true)
            assertEquals("keep root", (target / "keep.txt").readString())
            assertEquals("keep nested", (target / "nested" / "keep.txt").readString())
            assertCopiedTree(source)
        }
    }

    @Test
    fun PlatformFile_copyRecursivelyMixedTreeWithoutOverwrite_skipsConflictAndCopiesRemainingFiles() = runTest {
        withTemporaryDirectory { root ->
            val source = root / "source"
            val target = root / "target"
            source.createDirectories()
            target.createDirectories()
            for (name in listOf("a.txt", "b.txt", "c.txt")) {
                (source / name).writeString("content $name")
            }
            val files = source.walk().toList()
            val conflict = target / files[1].name
            conflict.writeString("original conflict")

            source.copyRecursively(target)

            assertEquals(files[0].readString(), (target / files[0].name).readString())
            assertEquals("original conflict", conflict.readString())
            assertEquals(files[2].readString(), (target / files[2].name).readString())
            for (file in files) {
                assertEquals("content ${file.name}", file.readString())
            }
        }
    }

    @Test
    fun PlatformFile_copyRecursivelyFileOntoItself_throwsAndPreservesContent() = runTest {
        withTemporaryDirectory { root ->
            val source = root / "source.txt"
            source.writeString("must survive")

            assertFailsWith<IllegalArgumentException> {
                source.copyRecursively(source, overwrite = true)
            }

            assertEquals("must survive", source.readString())
        }
    }

    @Test
    fun PlatformFile_copyRecursivelyDirectoryOntoItself_throwsAndPreservesTree() = runTest {
        withTemporaryDirectory { root ->
            createTree(root)

            assertFailsWith<IllegalArgumentException> {
                root.copyRecursively(root, overwrite = true)
            }

            assertCopiedTree(root)
        }
    }

    @Test
    fun PlatformFile_copyRecursivelyDirectoryIntoDescendant_throwsBeforeWriting() = runTest {
        withTemporaryDirectory { root ->
            val source = root / "source"
            val target = source / "backup"
            target.createDirectories()
            (source / "original.txt").writeString("source content")
            (target / "backup").writeString("recursion barrier")

            assertFailsWith<IllegalArgumentException> {
                source.copyRecursively(target, overwrite = true)
            }

            assertEquals(setOf("backup"), target.list().map { it.name }.toSet())
            assertEquals("recursion barrier", (target / "backup").readString())
            assertEquals("source content", (source / "original.txt").readString())
        }
    }

    @Test
    fun PlatformFile_copyRecursivelyDirectoryToSiblingWithSamePrefix_copiesNormally() = runTest {
        withTemporaryDirectory { root ->
            val source = root / "source"
            val target = root / "source-backup"
            source.createDirectories()
            target.createDirectories()
            (source / "file.txt").writeString("content")

            source.copyRecursively(target)

            assertEquals("content", (target / "file.txt").readString())
            assertEquals("content", (source / "file.txt").readString())
        }
    }

    @Test
    fun PlatformFile_copyRecursivelyFileToDirectory_throwsWithoutCopyingIntoIt() = runTest {
        withTemporaryDirectory { root ->
            val source = root / "source.txt"
            val target = root / "target"
            source.writeString("content")
            target.createDirectories()

            assertFails { source.copyRecursively(target, overwrite = true) }

            assertTrue(target.list().isEmpty())
            assertEquals("content", source.readString())
        }
    }

    @Test
    fun PlatformFile_copyRecursivelyDirectoryToFile_throwsAndPreservesDestination() = runTest {
        withTemporaryDirectory { root ->
            val source = root / "source"
            val target = root / "target.txt"
            source.createDirectories()
            (source / "file.txt").writeString("source content")
            target.writeString("must survive")

            assertFails { source.copyRecursively(target, overwrite = true) }

            assertEquals("must survive", target.readString())
            assertEquals("source content", (source / "file.txt").readString())
        }
    }

    @Test
    fun PlatformFile_copyRecursivelyMissingSource_throwsAndPreservesDestination() = runTest {
        withTemporaryDirectory { root ->
            val source = root / "missing"
            val target = root / "target.txt"
            target.writeString("must survive")

            assertFails { source.copyRecursively(target, overwrite = true) }

            assertFalse(source.exists())
            assertEquals("must survive", target.readString())
        }
    }

    @Test
    fun PlatformFile_copyRecursivelyNestedFileConflictingWithDirectory_throwsWithoutCopyingInsideIt() = runTest {
        withTemporaryDirectory { root ->
            val source = root / "source"
            val target = root / "target"
            source.createDirectories()
            (source / "entry.txt").writeString("new content")
            (target / "entry.txt").createDirectories()
            (target / "entry.txt" / "keep.txt").writeString("must survive")

            assertFails { source.copyRecursively(target) }

            assertEquals(setOf("keep.txt"), (target / "entry.txt").list().map { it.name }.toSet())
            assertEquals("must survive", (target / "entry.txt" / "keep.txt").readString())
            assertEquals("new content", (source / "entry.txt").readString())
        }
    }

    private suspend fun createTree(root: PlatformFile) {
        (root / "nested").createDirectories()
        (root / "empty-folder").createDirectories()
        (root / "top.txt").writeString("é top")
        (root / "empty.txt").writeString("")
        (root / "nested" / "leaf.txt").writeString("leaf content")
        (root / "nested" / "binary.bin").write(byteArrayOf(0, 1, -1, 127, -128))
    }

    private suspend fun assertCopiedTree(root: PlatformFile, includesUnrelatedFiles: Boolean = false) {
        val rootEntries = setOf("nested", "empty-folder", "top.txt", "empty.txt") +
            if (includesUnrelatedFiles) setOf("keep.txt") else emptySet()
        val nestedEntries = setOf("leaf.txt", "binary.bin") +
            if (includesUnrelatedFiles) setOf("keep.txt") else emptySet()
        assertEquals(rootEntries, root.list().map { it.name }.toSet())
        assertEquals(nestedEntries, (root / "nested").list().map { it.name }.toSet())
        assertTrue((root / "empty-folder").isDirectory())
        assertTrue((root / "empty-folder").list().isEmpty())
        assertEquals("é top", (root / "top.txt").readString())
        assertEquals("", (root / "empty.txt").readString())
        assertEquals("leaf content", (root / "nested" / "leaf.txt").readString())
        assertContentEquals(byteArrayOf(0, 1, -1, 127, -128), (root / "nested" / "binary.bin").readBytes())
    }

    private suspend fun withTemporaryDirectory(block: suspend (PlatformFile) -> Unit) {
        val root = PlatformFile(SystemTemporaryDirectory) / "filekit-copy-recursively-${Random.nextLong()}"
        try {
            root.createDirectories()
            block(root)
        } finally {
            root.delete(mustExist = false, recursively = true)
        }
    }
}
