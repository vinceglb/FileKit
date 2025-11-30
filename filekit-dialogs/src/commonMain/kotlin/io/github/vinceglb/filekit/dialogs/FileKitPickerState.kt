package io.github.vinceglb.filekit.dialogs

/**
 * Represents the state of the file picker.
 *
 * @param T The type of the result (e.g. [PlatformFile] or List<[PlatformFile]>).
 */
public sealed class FileKitPickerState<out T> {
    /**
     * The picker was cancelled by the user.
     */
    public data object Cancelled : FileKitPickerState<Nothing>()

    /**
     * The picker has started and is processing the files.
     *
     * @property total The total number of files to process.
     */
    public data class Started(
        val total: Int,
    ) : FileKitPickerState<Nothing>()

    /**
     * A file has been processed.
     *
     * @property processed The processed file or list of files so far.
     * @property total The total number of files to process.
     */
    public data class Progress<T>(
        val processed: T,
        val total: Int,
    ) : FileKitPickerState<T>()

    /**
     * The picking process is completed.
     *
     * @property result The final result of the picker.
     */
    public data class Completed<T>(
        val result: T,
    ) : FileKitPickerState<T>()
}
