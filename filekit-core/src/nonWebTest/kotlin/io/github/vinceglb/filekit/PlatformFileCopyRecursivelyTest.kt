@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.exceptions.FileKitException
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlatformFileCopyRecursivelyTest : PlatformFileTestBase() {
    // Canonical root: Robolectric's lstat reports a missing path containing "./" as a symbolic link.
    private val root = FileKit.projectDir.absoluteFile() / "src/nonWebTest/resources/copy-tree"
    private val source = root / "source"
    private val target = root / "target"

    @BeforeTest
    fun setUp() = runTest {
        root.delete(mustExist = false, recursively = true)
        (source / "a" / "b").createDirectories()
        (source / "empty").createDirectories()
        (source / "a" / "b" / "deep.txt").writeString("deep")
        (source / "a" / "a.txt").writeString("aa")
        (source / "top.txt").writeString("top")
    }

    @AfterTest
    fun tearDown() = runTest {
        root.delete(mustExist = false, recursively = true)
    }

    @Test
    fun PlatformFile_copyRecursively_directoryTree_copiesToTargetRoot() = runTest {
        val nestedTarget = target / "missing" / "parent"

        source.copyRecursively(nestedTarget)

        assertEquals(source.relativePaths(), nestedTarget.relativePaths())
        assertEquals("deep", (nestedTarget / "a" / "b" / "deep.txt").readString())
        assertEquals("top", (nestedTarget / "top.txt").readString())
    }

    @Test
    fun PlatformFile_copyRecursively_emptyDirectory_createsEmptyDirectory() = runTest {
        (source / "empty").copyRecursively(target)

        assertTrue(target.isDirectory())
        assertEquals(emptyList(), target.list())
    }

    @Test
    fun PlatformFile_copyRecursively_regularFile_copiesToTargetFile() = runTest {
        val copy = target / "nested" / "copy.txt"

        (source / "top.txt").copyRecursively(copy)

        assertEquals("top", copy.readString())
    }

    @Test
    fun PlatformFile_copyRecursively_existingTarget_mergesDirectories() = runTest {
        (target / "a").createDirectories()
        (target / "a" / "extra.txt").writeString("extra")

        source.copyRecursively(target)

        assertEquals(source.relativePaths() + "a/extra.txt", target.relativePaths())
        assertEquals("extra", (target / "a" / "extra.txt").readString())
    }

    @Test
    fun PlatformFile_copyRecursively_existingFileWithoutOverwrite_throws() = runTest {
        (target / "a").createDirectories()
        (target / "a" / "a.txt").writeString("old")

        assertFailsWith<FileKitException> { source.copyRecursively(target) }
        assertEquals("old", (target / "a" / "a.txt").readString())
    }

    @Test
    fun PlatformFile_copyRecursively_existingFileWithOverwrite_replacesIt() = runTest {
        (target / "a").createDirectories()
        (target / "a" / "a.txt").writeString("old")

        source.copyRecursively(target, overwrite = true)

        assertEquals(source.relativePaths(), target.relativePaths())
        assertEquals("aa", (target / "a" / "a.txt").readString())
    }

    @Test
    fun PlatformFile_copyRecursively_typeConflict_throws() = runTest {
        target.createDirectories()
        (target / "a").writeString("file where the source has a directory")
        (target / "top.txt").createDirectories()

        for (overwrite in listOf(false, true)) {
            assertFailsWith<FileKitException> { source.copyRecursively(target, overwrite) }
            assertFailsWith<FileKitException> { (source / "a").copyRecursively(target / "a", overwrite) }
            assertFailsWith<FileKitException> { (source / "top.txt").copyRecursively(target / "top.txt", overwrite) }
        }
    }

    @Test
    fun PlatformFile_copyRecursively_intoItself_throws() = runTest {
        assertFailsWith<FileKitException> { source.copyRecursively(source, overwrite = true) }
        assertFailsWith<FileKitException> { source.copyRecursively(source / "a" / "inside") }
        assertFalse((source / "a" / "inside").exists())
    }

    private fun PlatformFile.relativePaths(): Set<String> = walk().map { it.relativeTo(this) }.toSet()

    private fun PlatformFile.relativeTo(root: PlatformFile): String =
        path.removePrefix(root.path).drop(1).replace('\\', '/')
}
