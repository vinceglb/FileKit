@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit

import kotlinx.coroutines.test.runTest
import kotlinx.io.files.SystemTemporaryDirectory
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlatformFileCopyRecursivelyCallbacksTest : PlatformFileTestBase() {
    @Test
    fun PlatformFile_copyRecursivelyExistingFileWithoutOverwrite_callsOnlyOnFileSkipped() = runTest {
        withTemporaryDirectory { root ->
            val source = root / "source.txt"
            val target = root / "target.txt"
            var skippedCalled = false
            var addedCalled = false
            var overwrittenCalled = false
            source.writeString("new content")
            target.writeString("original content")

            source.copyRecursively(
                target = target,
                onFileSkipped = { skippedCalled = true },
                onFileAdded = { addedCalled = true },
                onFileOverwritten = { overwrittenCalled = true },
            )

            assertEquals("original content", target.readString())
            assertEquals("new content", source.readString())
            assertTrue(skippedCalled)
            assertFalse(addedCalled)
            assertFalse(overwrittenCalled)
        }
    }

    @Test
    fun PlatformFile_copyRecursivelyMixedTreeWithoutOverwrite_callsOnFileSkippedAndOnFileAdded() = runTest {
        withTemporaryDirectory { root ->
            val source = root / "source"
            val target = root / "target"
            var skippedCalled = false
            var addedCalled = false
            var overwrittenCalled = false
            source.createDirectories()
            target.createDirectories()
            (source / "new.txt").writeString("added content")
            (source / "existing.txt").writeString("source content")
            (target / "existing.txt").writeString("original content")

            source.copyRecursively(
                target = target,
                onFileSkipped = { skippedCalled = true },
                onFileAdded = { addedCalled = true },
                onFileOverwritten = { overwrittenCalled = true },
            )

            assertEquals("added content", (target / "new.txt").readString())
            assertEquals("original content", (target / "existing.txt").readString())
            assertEquals("source content", (source / "existing.txt").readString())
            assertTrue(skippedCalled)
            assertTrue(addedCalled)
            assertFalse(overwrittenCalled)
        }
    }

    @Test
    fun PlatformFile_copyRecursivelyNewFile_callsOnFileAdded() = runTest {
        withTemporaryDirectory { root ->
            val source = root / "source.txt"
            val target = root / "renamed.txt"
            var addedCalled = false
            source.writeString("source content")

            source.copyRecursively(
                target = target,
                onFileAdded = { addedCalled = true },
            )

            assertEquals("source content", target.readString())
            assertEquals("source content", source.readString())
            assertTrue(addedCalled, "onFileAdded must be called when a new file is copied")
        }
    }

    @Test
    fun PlatformFile_copyRecursivelyNewTreeFiles_callsOnFileAdded() = runTest {
        withTemporaryDirectory { root ->
            val source = root / "source"
            val target = root / "target"
            var addedCalled = false
            source.createDirectories()
            target.createDirectories()
            (source / "first.txt").writeString("first content")
            (source / "second.txt").writeString("second content")

            source.copyRecursively(
                target = target,
                onFileAdded = { addedCalled = true },
            )

            assertEquals("first content", (target / "first.txt").readString())
            assertEquals("second content", (target / "second.txt").readString())
            assertTrue(addedCalled, "onFileAdded must be called when tree files are copied")
        }
    }

    @Test
    fun PlatformFile_copyRecursivelyExistingFile_callsOnFileOverwritten() = runTest {
        withTemporaryDirectory { root ->
            val source = root / "source.txt"
            val target = root / "target.txt"
            var overwrittenCalled = false
            source.writeString("new")
            target.writeString("long original content")

            source.copyRecursively(
                target = target,
                overwrite = true,
                onFileOverwritten = { overwrittenCalled = true },
            )

            assertEquals("new", target.readString())
            assertEquals("new", source.readString())
            assertTrue(overwrittenCalled, "onFileOverwritten must be called when an existing file is replaced")
        }
    }

    @Test
    fun PlatformFile_copyRecursivelyMixedTree_callsOnFileAddedAndOnFileOverwritten() = runTest {
        withTemporaryDirectory { root ->
            val source = root / "source"
            val target = root / "target"
            var addedCalled = false
            var overwrittenCalled = false
            source.createDirectories()
            target.createDirectories()
            (source / "new.txt").writeString("added content")
            (source / "existing.txt").writeString("replaced content")
            (target / "existing.txt").writeString("old content")
            (target / "keep.txt").writeString("must survive")

            source.copyRecursively(
                target = target,
                overwrite = true,
                onFileAdded = { addedCalled = true },
                onFileOverwritten = { overwrittenCalled = true },
            )

            assertEquals("added content", (target / "new.txt").readString())
            assertEquals("replaced content", (target / "existing.txt").readString())
            assertEquals("must survive", (target / "keep.txt").readString())
            assertTrue(addedCalled, "onFileAdded must be called for new files in a merged tree")
            assertTrue(overwrittenCalled, "onFileOverwritten must be called for replaced files in a merged tree")
        }
    }

    private suspend fun withTemporaryDirectory(block: suspend (PlatformFile) -> Unit) {
        val root = PlatformFile(SystemTemporaryDirectory) / "filekit-copy-callbacks-${Random.nextLong()}"
        try {
            root.createDirectories()
            block(root)
        } finally {
            root.delete(mustExist = false, recursively = true)
        }
    }
}
