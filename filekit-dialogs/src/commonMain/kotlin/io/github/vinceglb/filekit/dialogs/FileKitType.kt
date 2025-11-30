package io.github.vinceglb.filekit.dialogs

/**
 * Represents the type of files to pick.
 */
public sealed class FileKitType {
    /**
     * Pick images only.
     */
    public data object Image : FileKitType()

    /**
     * Pick videos only.
     */
    public data object Video : FileKitType()

    /**
     * Pick both images and videos.
     */
    public data object ImageAndVideo : FileKitType()

    /**
     * Pick files with specific extensions.
     *
     * @property extensions The set of allowed file extensions (e.g. "png", "pdf"). If null, all files are allowed.
     */
    public data class File(
        val extensions: Set<String>? = null,
    ) : FileKitType() {
        /**
         * Creates a [File] type with allowed extensions.
         *
         * @param extensions Allowed file extensions.
         */
        public constructor(vararg extensions: String) : this(extensions.toSet())

        /**
         * Creates a [File] type with allowed extensions.
         *
         * @param extensions Allowed file extensions.
         */
        public constructor(extensions: List<String>) : this(extensions.toSet())

        /**
         * Creates a [File] type with a single allowed extension.
         *
         * @param extension Allowed file extension.
         */
        public constructor(extension: String) : this(setOf(extension))
    }
}
