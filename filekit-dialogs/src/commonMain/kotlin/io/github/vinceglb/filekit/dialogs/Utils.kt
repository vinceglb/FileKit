package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow

internal val imageExtensions = setOf("png", "jpg", "jpeg", "gif", "bmp")
internal val videoExtensions = setOf("mp4", "mov", "avi", "mkv", "webm")

internal fun List<PlatformFile>?.toPickerStateFlow(): Flow<FileKitPickerState<List<PlatformFile>>> {
    val files = this
    return channelFlow {
        when {
            files.isNullOrEmpty() -> send(FileKitPickerState.Cancelled)
            else -> {
                send(FileKitPickerState.Started(files.size))
                files.forEachIndexed { index, file ->
                    send(FileKitPickerState.Progress(files.subList(0, index + 1), files.size))
                }
                send(FileKitPickerState.Completed(files))
            }
        }
    }
}
