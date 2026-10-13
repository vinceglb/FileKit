@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit

import com.sun.jna.Platform
import kotlinx.coroutines.test.runTest
import org.junit.Assume.assumeTrue
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlatformFileCopyRecursivelyJvmTest {
    @Test
    fun PlatformFile_copyRecursivelySymbolicLinkSource_skipsFileDirectoryDanglingAndCyclicLinks() = runTest {
        withTemporaryDirectory { root ->
            val source = root / "source"
            val target = root / "target"
            source.createDirectories()
            (source / "file.txt").writeString("must survive")
            val link = Paths.get(root.path).resolve("link")

            for (destination in listOf("source/file.txt", "source", "missing", "link")) {
                try {
                    Files.createSymbolicLink(link, Paths.get(destination))

                    PlatformFile(link.toFile()).copyRecursively(target, overwrite = true)

                    assertFalse(target.exists())
                    assertTrue(PlatformFile(link.toFile()).isSymbolicLink())
                    assertEquals("must survive", (source / "file.txt").readString())
                } finally {
                    Files.deleteIfExists(link)
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
            val links = mutableListOf<Path>()
            try {
                val destinations = mapOf(
                    "file-link" to "../outside/treasure.txt",
                    "directory-link" to "../outside",
                    "dangling-link" to "missing",
                    "ancestor-link" to ".",
                )
                for ((name, destination) in destinations) {
                    val link = Paths.get(source.path).resolve(name)
                    Files.createSymbolicLink(link, Paths.get(destination))
                    links.add(link)
                }

                source.copyRecursively(target)

                assertEquals(setOf("regular.txt"), target.list().map { it.name }.toSet())
                assertEquals("copied content", (target / "regular.txt").readString())
                assertEquals("copied content", (source / "regular.txt").readString())
                assertEquals("must survive", (outside / "treasure.txt").readString())
                for (link in links) {
                    assertTrue(PlatformFile(link.toFile()).isSymbolicLink())
                }
            } finally {
                for (link in links) {
                    Files.deleteIfExists(link)
                }
            }
        }
    }

    @Test
    fun PlatformFile_copyRecursivelyFileOntoEquivalentPath_throwsAndPreservesContent() = runTest {
        val root = createTempDirectory("filekit-copy-equivalent-path")
        try {
            Files.createDirectory(root.resolve("nested"))
            val source = PlatformFile(root.resolve("source.txt").toFile())
            val target = PlatformFile(root.resolve("nested/../source.txt").toFile())
            source.writeString("must survive")

            assertFails { source.copyRecursively(target, overwrite = true) }

            assertEquals("must survive", source.readString())
        } finally {
            PlatformFile(root.toFile()).delete(mustExist = false, recursively = true)
        }
    }

    @Test
    fun PlatformFile_copyRecursivelySymbolicLinkSource_callsOnFileSkipped() = runTest {
        withTemporaryDirectory { root ->
            val source = root / "source.txt"
            val target = root / "target.txt"
            val link = Paths.get(root.path).resolve("link")
            var skippedCalled = false
            source.writeString("must survive")
            try {
                Files.createSymbolicLink(link, Paths.get("source.txt"))
                val linkedSource = PlatformFile(link.toFile())

                linkedSource.copyRecursively(
                    target = target,
                    onFileSkipped = { skippedCalled = true },
                )

                assertFalse(target.exists())
                assertEquals("must survive", source.readString())
                assertTrue(skippedCalled, "onFileSkipped must be called when the source is a symbolic link")
            } finally {
                Files.deleteIfExists(link)
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
            val links = mutableListOf<Path>()
            try {
                for ((name, destination) in mapOf("file-link" to "regular.txt", "dangling-link" to "missing")) {
                    val link = Paths.get(source.path).resolve(name)
                    Files.createSymbolicLink(link, Paths.get(destination))
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
                    Files.deleteIfExists(link)
                }
            }
        }
    }

    private suspend fun withTemporaryDirectory(block: suspend (PlatformFile) -> Unit) {
        assumeTrue("These tests require symlink creation privileges", !Platform.isWindows())
        val root = PlatformFile(createTempDirectory("filekit-copy-links").toFile())
        try {
            block(root)
        } finally {
            root.delete(mustExist = false, recursively = true)
        }
    }
}
