@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FileKitModeMaxItemsTest {
    @Test
    fun Single_parseResult_throwsPickerException_whenFailed() = runTest {
        val failure = FileKitPickerException("Failed to load the selected file.")

        val thrown = assertFailsWith<FileKitPickerException> {
            FileKitMode.Single.parseResult(
                flow = flowOf(FileKitPickerState.Failed(failure)),
            )
        }

        assertEquals(expected = failure.message, actual = thrown.message)
    }

    @Test
    fun Single_parseResult_returnsNull_whenCompletedResultEmpty() = runTest {
        val result = FileKitMode.Single.parseResult(
            flow = flowOf(FileKitPickerState.Completed(emptyList())),
        )

        assertEquals(expected = null, actual = result)
    }

    @Test
    fun SingleWithState_parseResult_treatsEmptyCompletedResultAsCancelled() = runTest {
        val states = FileKitMode
            .SingleWithState
            .parseResult(flow = flowOf(FileKitPickerState.Completed(emptyList())))
            .toList()

        assertEquals(
            expected = listOf(FileKitPickerState.Cancelled),
            actual = states,
        )
    }

    @Test
    fun SingleWithState_parseResult_keepsFailedUnchanged() = runTest {
        val failure = FileKitPickerException("Failed to load the selected file.")

        val states = FileKitMode
            .SingleWithState
            .parseResult(flow = flowOf(FileKitPickerState.Failed(failure)))
            .toList()

        assertEquals(
            expected = listOf(FileKitPickerState.Failed(failure)),
            actual = states,
        )
    }

    @Test
    fun Multiple_parseResult_truncatesCompletedResult_whenMaxItemsSet() = runTest {
        val files = createFiles(count = 4)
        val result = FileKitMode.Multiple(maxItems = 2).parseResult(
            flow = flowOf(FileKitPickerState.Completed(files)),
        )

        assertEquals(expected = files.take(2), actual = result)
    }

    @Test
    fun Multiple_parseResult_returnsAll_whenMaxItemsNull() = runTest {
        val files = createFiles(count = 4)
        val result = FileKitMode.Multiple(maxItems = null).parseResult(
            flow = flowOf(FileKitPickerState.Completed(files)),
        )

        assertEquals(expected = files, actual = result)
    }

    @Test
    fun Multiple_parseResult_returnsNull_whenCompletedResultEmpty() = runTest {
        val result = FileKitMode.Multiple(maxItems = null).parseResult(
            flow = flowOf(FileKitPickerState.Completed(emptyList())),
        )

        assertEquals(expected = null, actual = result)
    }

    @Test
    fun Multiple_parseResult_throwsPickerException_whenFailed() = runTest {
        val failure = FileKitPickerException("Failed to load one of the selected files.")

        val thrown = assertFailsWith<FileKitPickerException> {
            FileKitMode.Multiple(maxItems = null).parseResult(
                flow = flowOf(FileKitPickerState.Failed(failure)),
            )
        }

        assertEquals(expected = failure.message, actual = thrown.message)
    }

    @Test
    fun MultipleWithState_parseResult_capsStartedProgressCompleted_whenMaxItemsSet() = runTest {
        val files = createFiles(count = 4)
        val sourceStates: List<FileKitPickerState<List<PlatformFile>>> = listOf(
            FileKitPickerState.Started(total = 4),
            FileKitPickerState.Progress(processed = files.take(1), total = 4),
            FileKitPickerState.Progress(processed = files.take(3), total = 4),
            FileKitPickerState.Completed(result = files),
        )

        val states = FileKitMode
            .MultipleWithState(maxItems = 2)
            .parseResult(flow = sourceStates.asFlow())
            .toList()

        val expected: List<FileKitPickerState<List<PlatformFile>>> = listOf(
            FileKitPickerState.Started(total = 2),
            FileKitPickerState.Progress(processed = files.take(1), total = 2),
            FileKitPickerState.Progress(processed = files.take(2), total = 2),
            FileKitPickerState.Completed(result = files.take(2)),
        )
        assertEquals(expected = expected, actual = states)
    }

    @Test
    fun MultipleWithState_parseResult_keepsCancelledUnchanged() = runTest {
        val states = FileKitMode
            .MultipleWithState(maxItems = 2)
            .parseResult(flow = flowOf(FileKitPickerState.Cancelled))
            .toList()

        assertEquals(
            expected = listOf(FileKitPickerState.Cancelled),
            actual = states,
        )
    }

    @Test
    fun MultipleWithState_parseResult_treatsEmptyCompletedResultAsCancelled() = runTest {
        val states = FileKitMode
            .MultipleWithState(maxItems = null)
            .parseResult(flow = flowOf(FileKitPickerState.Completed(emptyList())))
            .toList()

        assertEquals(
            expected = listOf(FileKitPickerState.Cancelled),
            actual = states,
        )
    }

    @Test
    fun MultipleWithState_parseResult_keepsFailedUnchanged() = runTest {
        val failure = FileKitPickerException("Failed to load one of the selected files.")

        val states = FileKitMode
            .MultipleWithState(maxItems = null)
            .parseResult(flow = flowOf(FileKitPickerState.Failed(failure)))
            .toList()

        assertEquals(
            expected = listOf(FileKitPickerState.Failed(failure)),
            actual = states,
        )
    }

    @Test
    fun MultipleWithState_parseResult_returnsUnchangedFlow_whenMaxItemsNull() = runTest {
        val files = createFiles(count = 4)
        val sourceStates: List<FileKitPickerState<List<PlatformFile>>> = listOf(
            FileKitPickerState.Started(total = 4),
            FileKitPickerState.Progress(processed = files.take(3), total = 4),
            FileKitPickerState.Completed(result = files),
        )

        val states = FileKitMode
            .MultipleWithState(maxItems = null)
            .parseResult(flow = sourceStates.asFlow())
            .toList()

        assertEquals(expected = sourceStates, actual = states)
    }

    private fun createFiles(count: Int): List<PlatformFile> =
        List(count) { index -> createTestPlatformFile(name = "file-$index.txt") }
}

internal expect fun createTestPlatformFile(name: String): PlatformFile
