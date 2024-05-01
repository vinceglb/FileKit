package io.github.vinceglb.picker.core

public expect object Picker {
    /**
     * This function is used to pick a file or multiple files based on the mode provided.
     * It is a suspend function and should be called from a coroutine or a suspend function.
     *
     * @param mode The mode of file picking. It could be single file, multiple files, or directory.
     * @param title The title for the file picker dialog. It is optional and defaults to null.
     * @param initialDirectory The initial directory that the file picker should open. It is optional and defaults to null.
     * @return The picked file(s) or directory as defined by the mode.
     */
    public suspend fun <Out> pick(
        mode: PickerSelectionMode<Out>,
        title: String? = null,
        initialDirectory: String? = null,
    ): Out?

    /**
     * This function is used to save a file with the provided byte data, base name, and extension.
     * It is a suspend function and should be called from a coroutine or a suspend function.
     *
     * @param bytes The byte data to be written to the file.
     * @param baseName The base name of the file without extension. It defaults to "file".
     * @param extension The extension of the file. It should not include the dot.
     * @param initialDirectory The initial directory that the file save dialog should open. It is optional and defaults to null.
     * @return The saved file as a PlatformFile object.
     */
    public suspend fun save(
        bytes: ByteArray,
        baseName: String = "file",
        extension: String,
        initialDirectory: String? = null,
    ): PlatformFile?
}
