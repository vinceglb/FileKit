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

class PlatformFileCopyRecursivelyAppleTest {
    @Test
    fun PlatformFile_copyRecursivelySymbolicLinkSource_skipsFileDirectoryDanglingAndCyclicLinks() = runTest {
        withTemporaryDirectory { root ->
            val source = root / "source"
            val target = root / "target"
            val link = root / "link"
            source.createDirectories()
            (source / "file.txt").writeString("must survive")

            for (destination in listOf("source/file.txt", "source", "missing", "link")) {
                try {
                    assertEquals(0, symlink(destination, link.path))

                    link.copyRecursively(target, overwrite = true)

                    assertFalse(target.exists())
                    assertTrue(link.isSymbolicLink())
                    assertEquals("must survive", (source / "file.txt").readString())
                } finally {
                    unlink(link.path)
                }
            }
        }
    }

    @Test
    fun PlatformFile_copyRecursivelyTreeWithSymbolicLinks_copiesRegularFilesWithoutFollowingLinks() = runTest {
        withTemporaryDirectory { root ->
            val source = root / "source"
            val target = root / "target"
            val outside = root / "outside"
            source.createDirectories()
            target.createDirectories()
            outside.createDirectories()
            (source / "regular.txt").writeString("copied content")
            (outside / "treasure.txt").writeString("must survive")
            val links = mutableListOf<PlatformFile>()
            try {
                val destinations = mapOf(
                    "file-link" to "../outside/treasure.txt",
                    "directory-link" to "../outside",
                    "dangling-link" to "missing",
                    "ancestor-link" to ".",
                )
                for ((name, destination) in destinations) {
                    val link = source / name
                    assertEquals(0, symlink(destination, link.path))
                    links += link
                }

                source.copyRecursively(target)

                assertEquals(setOf("regular.txt"), target.list().map { it.name }.toSet())
                assertEquals("copied content", (target / "regular.txt").readString())
                assertEquals("copied content", (source / "regular.txt").readString())
                assertEquals("must survive", (outside / "treasure.txt").readString())
                for (link in links) {
                    assertTrue(link.isSymbolicLink())
                }
            } finally {
                for (link in links) {
                    unlink(link.path)
                }
            }
        }
    }

    @Test
    fun PlatformFile_copyRecursivelySymbolicLinkSource_callsOnFileSkipped() = runTest {
        withTemporaryDirectory { root ->
            val source = root / "source.txt"
            val target = root / "target.txt"
            val link = root / "link"
            var skippedCalled = false
            source.writeString("must survive")
            try {
                assertEquals(0, symlink("source.txt", link.path))

                link.copyRecursively(
                    target = target,
                    onFileSkipped = { skippedCalled = true },
                )

                assertFalse(target.exists())
                assertEquals("must survive", source.readString())
                assertTrue(skippedCalled, "onFileSkipped must be called when the source is a symbolic link")
            } finally {
                unlink(link.path)
            }
        }
    }

    @Test
    fun PlatformFile_copyRecursivelyTreeWithSymbolicLinks_callsOnFileSkipped() = runTest {
        withTemporaryDirectory { root ->
            val source = root / "source"
            val target = root / "target"
            var skippedCalled = false
            source.createDirectories()
            target.createDirectories()
            (source / "regular.txt").writeString("copied content")
            val links = mutableListOf<PlatformFile>()
            try {
                for ((name, destination) in mapOf("file-link" to "regular.txt", "dangling-link" to "missing")) {
                    val link = source / name
                    assertEquals(0, symlink(destination, link.path))
                    links.add(link)
                }

                source.copyRecursively(
                    target = target,
                    onFileSkipped = { skippedCalled = true },
                )

                assertEquals(setOf("regular.txt"), target.list().map { it.name }.toSet())
                assertEquals("copied content", (target / "regular.txt").readString())
                assertTrue(skippedCalled, "onFileSkipped must be called when tree entries are symbolic links")
            } finally {
                for (link in links) {
                    unlink(link.path)
                }
            }
        }
    }

    private suspend fun withTemporaryDirectory(block: suspend (PlatformFile) -> Unit) {
        val root = PlatformFile(SystemTemporaryDirectory) / "filekit-copy-links-${Random.nextLong()}"
        try {
            root.createDirectories()
            block(root)
        } finally {
            root.delete(mustExist = false, recursively = true)
        }
    }
}
