package io.github.vinceglb.filekit.core

public expect object FileKit {
    public suspend fun <Out> pickFile(
        type: PickerType = PickerType.File(),
        mode: PickerMode<Out>,
        title: String? = null,
        initialDirectory: String? = null,
        platformSettings: FileKitPlatformSettings? = null,
    ): Out?

    public suspend fun pickDirectory(
        title: String? = null,
        initialDirectory: String? = null,
        platformSettings: FileKitPlatformSettings? = null,
    ): IPlatformFile?

    public fun isDirectoryPickerSupported(): Boolean

    public suspend fun saveFile(
        bytes: ByteArray? = null,
        baseName: String = "file",
        extension: String,
        initialDirectory: String? = null,
        platformSettings: FileKitPlatformSettings? = null,
    ): IPlatformFile?

    public suspend fun isSaveFileWithoutBytesSupported(): Boolean
}

public suspend fun FileKit.pickFile(
    type: PickerType = PickerType.File(),
    title: String? = null,
    initialDirectory: String? = null,
    platformSettings: FileKitPlatformSettings? = null,
): IPlatformFile? {
    return pickFile(
        type = type,
        mode = PickerMode.Single,
        title = title,
        initialDirectory = initialDirectory,
        platformSettings = platformSettings,
    )
}
