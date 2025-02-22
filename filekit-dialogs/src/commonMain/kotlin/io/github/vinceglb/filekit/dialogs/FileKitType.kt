package io.github.vinceglb.filekit.dialogs

public sealed class FileKitType {
    public data object Image : FileKitType()
    public data object Video : FileKitType()
    public data object ImageAndVideo : FileKitType()
    public data class File(
        val extensions: List<String>? = null
    ) : FileKitType()
}
