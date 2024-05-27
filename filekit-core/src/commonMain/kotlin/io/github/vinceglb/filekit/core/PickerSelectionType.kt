package io.github.vinceglb.filekit.core

public sealed class PickerSelectionType {
    public data object Image : PickerSelectionType()
    public data object Video : PickerSelectionType()
    public data object ImageAndVideo : PickerSelectionType()
    public data class File(
        val extensions: List<String>? = null
    ) : PickerSelectionType()
}