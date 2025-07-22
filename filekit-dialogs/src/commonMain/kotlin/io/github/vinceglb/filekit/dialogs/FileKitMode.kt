package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.transform

public sealed class FileKitMode<PickerResult, ConsumedResult> {
    internal abstract fun getPickerMode(): PickerMode

    internal abstract suspend fun parseResult(flow: Flow<FileKitPickerState<List<PlatformFile>>>): PickerResult

    public abstract suspend fun consumeResult(
        result: PickerResult,
        onConsumed: (ConsumedResult) -> Unit
    )

    public data object Single : FileKitMode<PlatformFile?, PlatformFile?>() {
        override fun getPickerMode(): PickerMode = PickerMode.Single
        override suspend fun parseResult(flow: Flow<FileKitPickerState<List<PlatformFile>>>): PlatformFile? {
            return flow.last().let {
                when (it) {
                    is FileKitPickerState.Completed -> it.result.firstOrNull()
                    else -> null
                }
            }
        }

        override suspend fun consumeResult(
            result: PlatformFile?,
            onConsumed: (PlatformFile?) -> Unit
        ) {
            onConsumed(result)
        }
    }

    public data class Multiple(val maxItems: Int? = null) :
        FileKitMode<List<PlatformFile>?, List<PlatformFile>?>() {
        override fun getPickerMode(): PickerMode = PickerMode.Multiple(maxItems)
        override suspend fun parseResult(flow: Flow<FileKitPickerState<List<PlatformFile>>>): List<PlatformFile>? {
            return flow.last().let {
                when (it) {
                    is FileKitPickerState.Completed -> it.result
                    else -> null
                }
            }
        }

        override suspend fun consumeResult(
            result: List<PlatformFile>?,
            onConsumed: (List<PlatformFile>?) -> Unit
        ) {
            onConsumed(result)
        }
    }

    public data object SingleWithState :
        FileKitMode<Flow<FileKitPickerState<PlatformFile>>, FileKitPickerState<PlatformFile>>() {
        override fun getPickerMode(): PickerMode = PickerMode.Single

        override suspend fun parseResult(flow: Flow<FileKitPickerState<List<PlatformFile>>>): Flow<FileKitPickerState<PlatformFile>> {
            return flow.transform { pickerState ->
                when (pickerState) {
                    is FileKitPickerState.Cancelled -> emit(FileKitPickerState.Cancelled)
                    is FileKitPickerState.Started -> emit(FileKitPickerState.Started(total = pickerState.total))

                    is FileKitPickerState.Progress -> {
                        val file = pickerState.processed.firstOrNull()
                        if (file != null) {
                            emit(
                                FileKitPickerState.Progress(
                                    processed = file,
                                    total = pickerState.total
                                )
                            )
                        }
                    }

                    is FileKitPickerState.Completed -> {
                        val file = pickerState.result.firstOrNull()
                        when {
                            file != null -> emit(FileKitPickerState.Completed(result = file))
                            else -> emit(FileKitPickerState.Cancelled) // Treat empty result as cancellation
                        }
                    }
                }
            }
        }

        override suspend fun consumeResult(
            result: Flow<FileKitPickerState<PlatformFile>>,
            onConsumed: (FileKitPickerState<PlatformFile>) -> Unit
        ) {
            result.collect(onConsumed)
        }
    }

    public data class MultipleWithState(val maxItems: Int? = null) :
        FileKitMode<Flow<FileKitPickerState<List<PlatformFile>>>, FileKitPickerState<List<PlatformFile>>>() {
        override fun getPickerMode(): PickerMode = PickerMode.Multiple(maxItems)

        override suspend fun parseResult(flow: Flow<FileKitPickerState<List<PlatformFile>>>): Flow<FileKitPickerState<List<PlatformFile>>> {
            return flow.mapNotNull {
                when (it) {
                    is FileKitPickerState.Cancelled -> FileKitPickerState.Cancelled
                    is FileKitPickerState.Started -> FileKitPickerState.Started(total = it.total)
                    is FileKitPickerState.Progress ->
                        FileKitPickerState.Progress(processed = it.processed, total = it.total)

                    is FileKitPickerState.Completed ->
                        FileKitPickerState.Completed(result = it.result)
                }
            }
        }

        override suspend fun consumeResult(
            result: Flow<FileKitPickerState<List<PlatformFile>>>,
            onConsumed: (FileKitPickerState<List<PlatformFile>>) -> Unit
        ) {
            result.collect(onConsumed)
        }
    }
}

internal sealed class PickerMode {
    data object Single : PickerMode()
    data class Multiple(val maxItems: Int? = null) : PickerMode() {
        init {
            require(maxItems == null || maxItems in 1..50) {
                "maxItems must be contained between 1 <= maxItems <= 50 but current value is $maxItems"
            }
        }
    }
}
