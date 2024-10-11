package io.github.vinceglb.filekit.dialog

public sealed class PickerType {
    public data object Image : PickerType()
    public data object Video : PickerType()
    public data object ImageAndVideo : PickerType()
    public data class File(
        val extensions: List<String>? = null
    ) : PickerType()
}
