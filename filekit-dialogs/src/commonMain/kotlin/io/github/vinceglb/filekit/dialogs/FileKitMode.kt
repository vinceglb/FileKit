package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.mapNotNull

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
            return flow.mapNotNull {
                when (it) {
                    is FileKitPickerState.Cancelled -> FileKitPickerState.Cancelled
                    is FileKitPickerState.Started -> FileKitPickerState.Started(
                        total = it.total
                    )

                    is FileKitPickerState.Progress -> {
                        it.processed.firstOrNull()?.let { file ->
                            FileKitPickerState.Progress(
                                processed = file,
                                total = it.total
                            )
                        }
                    }

                    is FileKitPickerState.Completed -> {
                        it.result.firstOrNull()?.let { file ->
                            FileKitPickerState.Completed(result = file)
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
