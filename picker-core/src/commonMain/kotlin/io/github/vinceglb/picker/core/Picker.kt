package io.github.vinceglb.picker.core

public expect object Picker {
    public suspend fun <Out> pickFile(
        type: PickerSelectionType = PickerSelectionType.File(),
        mode: PickerSelectionMode<Out>,
        title: String? = null,
        initialDirectory: String? = null,
    ): Out?

    public suspend fun pickDirectory(
        title: String? = null,
        initialDirectory: String? = null,
    ): PlatformDirectory?

    public fun isDirectoryPickerSupported(): Boolean

    public suspend fun saveFile(
        bytes: ByteArray,
        baseName: String = "file",
        extension: String,
        initialDirectory: String? = null,
    ): PlatformFile?
}

public suspend fun Picker.pickFile(
    type: PickerSelectionType = PickerSelectionType.File(),
    title: String? = null,
    initialDirectory: String? = null,
): PlatformFile? {
    return pickFile(
        type = type,
        mode = PickerSelectionMode.Single,
        title = title,
        initialDirectory = initialDirectory,
    )
}
