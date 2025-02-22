package io.github.vinceglb.filekit.dialogs

public sealed class FileKitType {
    public data object Image : FileKitType()
    public data object Video : FileKitType()
    public data object ImageAndVideo : FileKitType()
    public data class File(
        val extensions: Set<String>? = null
    ) : FileKitType() {
        public constructor(vararg extensions: String) : this(extensions.toSet())
        public constructor(extensions: List<String>) : this(extensions.toSet())
        public constructor(extension: String) : this(setOf(extension))
    }
}
