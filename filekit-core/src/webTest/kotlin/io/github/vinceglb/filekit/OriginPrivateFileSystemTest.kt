@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.exceptions.FileKitException
import io.github.vinceglb.filekit.utils.createTestFile
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
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
    fun FileKit_originPrivateDirectory_returnsRootDirectory() = runTest {
        val root = FileKit.originPrivateDirectory()

        assertTrue(root.isDirectory())
        assertFalse(root.isRegularFile())
        assertNull(root.parent())
        assertEquals("", root.path)
    }

    @Test
    fun PlatformFile_writeString_updatesContentAndMetadata() = runTest {
        val directory = createTestDirectory("write")
        try {
            val file = directory.file("hello.txt", create = true)

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
    fun PlatformFile_file_missingWithoutCreate_fails() = runTest {
        val directory = createTestDirectory("missing")
        try {
            assertFails { directory.file("missing.txt") }
            assertFails { directory.directory("missing") }
        } finally {
            directory.delete(recursively = true)
        }
    }

    @Test
    fun PlatformFile_listEntries_returnsFilesAndDirectories() = runTest {
        val directory = createTestDirectory("list")
        try {
            directory.file("a.txt", create = true)
            directory.directory("nested", create = true)

            val entries = directory.listEntries().associateBy { it.name }

            assertEquals(setOf("a.txt", "nested"), entries.keys)
            assertTrue(entries.getValue("a.txt").isRegularFile())
            assertTrue(entries.getValue("nested").isDirectory())
        } finally {
            directory.delete(recursively = true)
        }
    }

    @Test
    fun PlatformFile_list_originPrivateDirectory_fails() = runTest {
        val root = FileKit.originPrivateDirectory()

        assertFailsWith<FileKitException> { root.list() }
    }

    @Test
    fun PlatformFile_deleteRecursively_removesNonEmptyDirectory() = runTest {
        val directory = createTestDirectory("recursive")
        directory.directory("nested", create = true).file("a.txt", create = true).writeString("x")

        directory.delete(recursively = true)

        assertFalse(FileKit.originPrivateDirectory().listEntries().any { it.name == "recursive" })
    }

    @Test
    fun PlatformFile_delete_nonEmptyDirectoryWithoutRecursively_fails() = runTest {
        val directory = createTestDirectory("non-recursive")
        try {
            directory.file("a.txt", create = true)

            assertFails { directory.delete() }
        } finally {
            directory.delete(recursively = true)
        }
    }

    @Test
    fun PlatformFile_delete_missingEntry_failsOnlyWhenItMustExist() = runTest {
        val directory = createTestDirectory("missing-delete")
        try {
            val file = directory.file("a.txt", create = true)
            file.delete()

            file.delete(mustExist = false)
            assertFails { file.delete() }
        } finally {
            directory.delete(recursively = true)
        }
    }

    @Test
    fun PlatformFile_write_pickerFile_fails() = runTest {
        val file = createTestFile(name = "hello.txt", content = "Hello")

        assertFailsWith<FileKitException> { file.writeString("Bye") }
        assertFailsWith<FileKitException> { file.delete() }
    }

    private suspend fun createTestDirectory(name: String): PlatformFile {
        val root = FileKit.originPrivateDirectory()
        root.directory(name, create = true).delete(recursively = true)
        return root.directory(name, create = true)
    }
}
