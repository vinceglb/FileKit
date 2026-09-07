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

class PlatformFileDeletionTest {
    @Test
    fun PlatformFile_delete_danglingLink_isUnlinked() = runTest {
        val root = PlatformFile(SystemTemporaryDirectory) / "filekit-delete-link-${Random.nextInt(0, Int.MAX_VALUE)}"
        root.createDirectories()
        val link = root / "link"
        try {
            for (recursively in listOf(false, true)) {
                for (mustExist in listOf(false, true)) {
                    assertEquals(0, symlink("missing", link.path))

                    link.delete(mustExist, recursively)

                    assertEquals(emptyList(), root.list(), "The dangling link must be unlinked")
                }
            }
        } finally {
            unlink(link.path)
            root.delete(mustExist = false)
        }
    }

    @Test
    fun PlatformFile_deleteRecursively_links_areUnlinkedWithoutFollowingTargets() = runTest {
        val root = PlatformFile(SystemTemporaryDirectory) / "filekit-delete-tree-${Random.nextInt(0, Int.MAX_VALUE)}"
        val outside = root / "outside"
        outside.createDirectories()
        val treasure = outside / "treasure.txt"
        treasure.writeString("must survive")
        val doomed = root / "doomed"
        doomed.createDirectories()
        val links = listOf(doomed / "dangling", doomed / "cycle", doomed / "outside")
        try {
            assertEquals(0, symlink("missing", links[0].path))
            assertEquals(0, symlink("cycle", links[1].path))
            assertEquals(0, symlink(outside.path, links[2].path))

            doomed.delete(recursively = true)

            assertFalse(doomed.exists())
            assertEquals("must survive", treasure.readString())
        } finally {
            links.forEach { unlink(it.path) }
            doomed.delete(mustExist = false)
            treasure.delete(mustExist = false)
            outside.delete(mustExist = false)
            root.delete(mustExist = false)
        }
    }
}
