@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")
@file:OptIn(ExperimentalFileKitApi::class)

package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.exceptions.FileKitException
import io.github.vinceglb.filekit.utils.createTestFile
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class OriginPrivateFileSystemTest {
    @Test
    fun appendOriginPrivateFileSystemPath_fromRoot_returnsChildName() {
        assertEquals("hello.txt", "".appendOriginPrivateFileSystemPath("hello.txt"))
    }

    @Test
    fun appendOriginPrivateFileSystemPath_fromDirectory_joinsWithSlash() {
        assertEquals("folder/hello.txt", "folder".appendOriginPrivateFileSystemPath("hello.txt"))
    }

    @Test
    fun FileKit_originPrivateDir_describesRootWithoutBrowserAccess() {
        val root = FileKit.originPrivateDir

        assertTrue(root.isDirectory())
        assertFalse(root.isRegularFile())
        assertNull(root.parent())
        assertEquals("", root.path)
        assertEquals("", root.name)
        assertEquals(root, FileKit.originPrivateDir)
    }

    @Test
    fun PlatformFile_writeString_updatesContentAndMetadata() = runTest {
        val directory = createTestDirectory("write")
        try {
            val file = directory.resolveFile("hello.txt", create = true)

            file.writeString("Hello")

            assertEquals("Hello", file.readString())
            assertEquals(5L, file.size())
            assertTrue(file.lastModified() > Instant.fromEpochMilliseconds(0))
            assertEquals("write/hello.txt", file.path)
        } finally {
            directory.delete(recursively = true)
        }
    }

    @Test
    fun PlatformFile_resolveFile_nestedPathWithCreate_createsParentDirectories() = runTest {
        val directory = createTestDirectory("nested")
        try {
            val file = directory.resolveFile("a/b/c.txt", create = true)

            assertEquals("nested/a/b/c.txt", file.path)
            assertEquals(directory.resolveDirectory("a/b"), file.parent())
            assertEquals(file, directory.resolveDirectory("a").resolveFile("b/c.txt"))
        } finally {
            directory.delete(recursively = true)
        }
    }

    @Test
    fun PlatformFile_resolveFile_emptySegments_areIgnored() = runTest {
        val directory = createTestDirectory("empty-segments")
        try {
            val file = directory.resolveFile("/a//b.txt", create = true)

            assertEquals("empty-segments/a/b.txt", file.path)
            assertEquals(file, directory.resolveFile("a/b.txt"))
            assertEquals(directory.resolveDirectory("a"), directory.resolveDirectory("a/"))
        } finally {
            directory.delete(recursively = true)
        }
    }

    @Test
    fun PlatformFile_resolve_missingWithoutCreate_fails() = runTest {
        val directory = createTestDirectory("missing")
        try {
            assertFails { directory.resolveFile("missing.txt") }
            assertFails { directory.resolveDirectory("missing") }
            assertFails { directory.resolveFile("missing/a.txt") }
        } finally {
            directory.delete(recursively = true)
        }
    }

    @Test
    fun PlatformFile_resolve_invalidRelativePath_fails() = runTest {
        val root = FileKit.originPrivateDir

        assertFailsWith<IllegalArgumentException> { root.resolveFile("") }
        assertFailsWith<IllegalArgumentException> { root.resolveDirectory("/") }
        assertFailsWith<IllegalArgumentException> { root.resolveFile("a/../b.txt") }
    }

    @Test
    fun PlatformFile_resolve_fromNonOriginPrivateFile_fails() = runTest {
        val file = createTestFile(name = "hello.txt", content = "Hello")

        assertFailsWith<FileKitException> { file.resolveFile("a.txt") }
    }

    @Test
    fun PlatformFile_equals_sameOriginPrivatePath_isEqual() = runTest {
        val directory = createTestDirectory("equality")
        try {
            val file = directory.resolveFile("a.txt", create = true)
            val listed = directory.listEntries().single()

            assertEquals(file, listed)
            assertEquals(file.hashCode(), listed.hashCode())
            assertNotEquals(file, directory)
        } finally {
            directory.delete(recursively = true)
        }
    }

    @Test
    fun PlatformFile_listEntries_returnsFilesAndDirectories() = runTest {
        val directory = createTestDirectory("list")
        try {
            directory.resolveFile("a.txt", create = true)
            directory.resolveDirectory("nested", create = true)

            val entries = directory.listEntries().associateBy { it.name }

            assertEquals(setOf("a.txt", "nested"), entries.keys)
            assertTrue(entries.getValue("a.txt").isRegularFile())
            assertTrue(entries.getValue("nested").isDirectory())
        } finally {
            directory.delete(recursively = true)
        }
    }

    @Test
    fun PlatformFile_list_originPrivateDirectory_fails() {
        assertFailsWith<FileKitException> { FileKit.originPrivateDir.list() }
    }

    @Test
    fun PlatformFile_deleteRecursively_removesNonEmptyDirectory() = runTest {
        val directory = createTestDirectory("recursive")
        directory.resolveFile("nested/a.txt", create = true).writeString("x")

        directory.delete(recursively = true)

        assertFalse(FileKit.originPrivateDir.listEntries().any { it.name == "recursive" })
    }

    @Test
    fun PlatformFile_delete_nonEmptyDirectoryWithoutRecursively_fails() = runTest {
        val directory = createTestDirectory("non-recursive")
        try {
            directory.resolveFile("a.txt", create = true)

            assertFails { directory.delete() }
        } finally {
            directory.delete(recursively = true)
        }
    }

    @Test
    fun PlatformFile_delete_missingEntry_failsOnlyWhenItMustExist() = runTest {
        val directory = createTestDirectory("missing-delete")
        try {
            val file = directory.resolveFile("a.txt", create = true)
            file.delete()

            file.delete(mustExist = false)
            assertFails { file.delete() }
        } finally {
            directory.delete(recursively = true)
        }
    }

    @Test
    fun PlatformFile_delete_root_fails() = runTest {
        assertFailsWith<FileKitException> { FileKit.originPrivateDir.delete(recursively = true) }
    }

    @Test
    fun PlatformFile_write_pickerFile_fails() = runTest {
        val file = createTestFile(name = "hello.txt", content = "Hello")

        assertFailsWith<FileKitException> { file.writeString("Bye") }
        assertFailsWith<FileKitException> { file.delete() }
    }

    private suspend fun createTestDirectory(name: String): PlatformFile {
        val root = FileKit.originPrivateDir
        root.resolveDirectory(name, create = true).delete(recursively = true)
        return root.resolveDirectory(name, create = true)
    }
}
