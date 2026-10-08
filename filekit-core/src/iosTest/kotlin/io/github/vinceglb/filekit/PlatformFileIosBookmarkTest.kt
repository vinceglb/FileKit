@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.exceptions.FileKitException
import kotlinx.coroutines.test.runTest
import kotlinx.io.Buffer
import kotlinx.io.files.SystemTemporaryDirectory
import kotlinx.io.readString
import platform.posix.symlink
import platform.posix.unlink
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame

class PlatformFileIosBookmarkTest {
    // The simulator does not enforce device sandbox permissions. These tests exercise the
    // real bookmark/derived-path lifecycle; the permission regression also needs a device
    // reboot with a bookmarked directory selected outside the app's container (issue #667).
    @Test
    fun PlatformFile_restoredDirectory_sharesAccessWithDescendants() = runTest {
        withDirectory { directory ->
            val bookmark = directory.bookmarkData()
            val resolution = PlatformFile.resolveBookmarkData(bookmark)
            val root = resolution.file
            val child = root / "nested/child.txt"

            val lease = assertNotNull(root.appleBookmarkLease)
            assertSame(lease, child.appleBookmarkLease)
            assertSame(lease, child.parent()?.appleBookmarkLease)
            assertSame(lease, child.copy().appleBookmarkLease)
            assertSame(lease, root.copy(child.nsUrl).appleBookmarkLease)
            assertFalse(resolution.shouldRefresh)
            assertFalse(resolution.isStale)
        }
    }

    @Test
    fun PlatformFile_restoredDirectory_listsChildrenWithSharedAccess() = runTest {
        withDirectory { directory ->
            (directory / "child.txt").writeString("content")
            val root = PlatformFile.fromBookmarkData(directory.bookmarkData())
            val lease = assertNotNull(root.appleBookmarkLease)

            assertSame(lease, root.list().single().appleBookmarkLease)
            root.list { children ->
                assertSame(lease, children.single().appleBookmarkLease)
            }
        }
    }

    @Test
    fun PlatformFile_restoredDirectory_doesNotShareAccessOutsideRoot() = runTest {
        withDirectory { directory ->
            val root = PlatformFile.fromBookmarkData(directory.bookmarkData())
            assertNotNull(root.appleBookmarkLease)

            assertNull(root.parent()?.appleBookmarkLease)
            assertNull((root / "../outside").appleBookmarkLease)
            assertNull((root / "missing/../../outside").appleBookmarkLease)
            assertNull(root.copy(PlatformFile("${root.path}-sibling/child").nsUrl).appleBookmarkLease)
        }
    }

    @Test
    fun PlatformFile_restoredDirectory_doesNotShareAccessThroughEscapingSymlink() = runTest {
        withDirectory { directory ->
            val rootDirectory = directory / "root"
            val outside = directory / "outside"
            rootDirectory.createDirectories()
            outside.createDirectories()
            val link = rootDirectory / "link"
            assertEquals(0, symlink(outside.path, link.path))
            try {
                val root = PlatformFile.fromBookmarkData(rootDirectory.bookmarkData())
                assertNotNull(root.appleBookmarkLease)

                assertNull((root / "link/child.txt").appleBookmarkLease)
            } finally {
                unlink(link.path)
            }
        }
    }

    @Test
    fun PlatformFile_releaseRestoredDirectory_rejectsOperationsOnExistingDescendants() = runTest {
        withDirectory { directory ->
            val root = PlatformFile.fromBookmarkData(directory.bookmarkData())
            val child = root / "child.txt"
            child.writeString("content")
            val listed = root.list().single()

            root.releaseBookmark()
            root.releaseBookmark()

            for (file in listOf(root, child, child.copy(), listed)) {
                assertFailsWith<FileKitException> { file.exists() }
            }
            assertFailsWith<FileKitException> { child.source() }
            assertFailsWith<FileKitException> { child.sink() }
        }
    }

    @Test
    fun PlatformFile_releaseRestoredDirectory_allowsAlreadyOpenSourceToClose() = runTest {
        withDirectory { directory ->
            (directory / "child.txt").writeString("content")
            val root = PlatformFile.fromBookmarkData(directory.bookmarkData())
            val child = root / "child.txt"
            val source = child.source()
            try {
                root.releaseBookmark()

                val buffer = Buffer()
                assertEquals(7L, source.readAtMostTo(buffer, 7))
                assertEquals("content", buffer.readString())
                assertFailsWith<FileKitException> { child.source() }
            } finally {
                source.close()
                source.close()
            }
        }
    }

    private suspend fun withDirectory(block: suspend (PlatformFile) -> Unit) {
        val directory = PlatformFile(SystemTemporaryDirectory) / "filekit-ios-bookmark-${Random.nextInt(0, Int.MAX_VALUE)}"
        directory.createDirectories()
        try {
            block(directory)
        } finally {
            directory.delete(recursively = true)
        }
    }
}
