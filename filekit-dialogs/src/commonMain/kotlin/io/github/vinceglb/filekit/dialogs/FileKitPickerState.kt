package io.github.vinceglb.filekit.dialogs

public sealed class FileKitPickerState<out T> {
    public data object Cancelled : FileKitPickerState<Nothing>()

    public data class Started(
        val total: Int,
    ) : FileKitPickerState<Nothing>()

    public data class Progress<T>(
        val processed: T,
        val total: Int,
    ) : FileKitPickerState<T>()

    public data class Completed<T>(
        val result: T,
    ) : FileKitPickerState<T>()
}
