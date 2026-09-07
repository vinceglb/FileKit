@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit

import android.system.ErrnoException
import android.system.Os
import android.system.OsConstants
import android.system.StructStat
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import java.nio.file.Files
import java.nio.file.LinkOption.NOFOLLOW_LINKS
import java.nio.file.NoSuchFileException
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.createDirectory
import kotlin.io.path.createTempDirectory
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [23, 36], shadows = [NoFollowOsShadow::class])
class PlatformFileDeletionTest {
    @Test
    fun PlatformFile_deleteRecursively_links_areUnlinkedWithoutFollowingTargets() = runTest {
        val root = createTempDirectory("filekit-delete-links")
        val outside = root.resolve("outside").createDirectory()
        val treasure = outside.resolve("treasure.txt")
        treasure.writeText("must survive")
        val doomed = root.resolve("doomed").createDirectory()
        val dangling = Files.createSymbolicLink(doomed.resolve("dangling"), doomed.resolve("missing"))
        val link = Files.createSymbolicLink(doomed.resolve("outside"), outside)
        try {
            PlatformFile(doomed.toFile()).delete(recursively = true)

            assertFalse(doomed.exists(NOFOLLOW_LINKS))
            assertEquals("must survive", treasure.readText())
        } finally {
            Files.deleteIfExists(dangling)
            Files.deleteIfExists(link)
            root.toFile().deleteRecursively()
        }
    }
}

// Robolectric's default lstat delegates to stat and follows directory links. Supply the native
// no-follow contract using the host filesystem so this test can catch traversal into a target.
@Implements(Os::class)
class NoFollowOsShadow {
    companion object {
        @JvmStatic
        @Implementation
        fun lstat(path: String): StructStat {
            val attributes = try {
                Files.readAttributes(Paths.get(path), BasicFileAttributes::class.java, NOFOLLOW_LINKS)
            } catch (error: NoSuchFileException) {
                throw ErrnoException("lstat", OsConstants.ENOENT, error)
            }
            val mode = when {
                attributes.isSymbolicLink -> OsConstants.S_IFLNK
                attributes.isDirectory -> OsConstants.S_IFDIR
                else -> OsConstants.S_IFREG
            }
            return StructStat(0, 0, mode, 0, 0, 0, 0, attributes.size(), 0, 0, 0, 0, 0)
        }

        @JvmStatic
        @Implementation
        fun remove(path: String) {
            Files.delete(Paths.get(path))
        }
    }
}
