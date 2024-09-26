package io.github.vinceglb.filekit.core

public expect object FileKit

public expect suspend fun <Out> FileKit.pickFile(
    type: PickerType = PickerType.File(),
    mode: PickerMode<Out>,
    title: String? = null,
    initialDirectory: String? = null,
    platformSettings: FileKitPlatformSettings? = null,
): Out?

public expect suspend fun FileKit.saveFile(
    bytes: ByteArray? = null,
    baseName: String = "file",
    extension: String,
    initialDirectory: String? = null,
    platformSettings: FileKitPlatformSettings? = null,
): PlatformFile?

public suspend fun FileKit.pickFile(
    type: PickerType = PickerType.File(),
    title: String? = null,
    initialDirectory: String? = null,
    platformSettings: FileKitPlatformSettings? = null,
): PlatformFile? {
    return pickFile(
        type = type,
        mode = PickerMode.Single,
        title = title,
        initialDirectory = initialDirectory,
        platformSettings = platformSettings,
    )
}
