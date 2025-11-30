package io.github.vinceglb.filekit.dialogs.deprecated

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings

@Deprecated(
    message = "Use the function without the bytes parameter. " +
        "More info in the migration guide: https://filekit.mintlify.app/migrate-to-v0.10",
)
public expect suspend fun FileKit.openFileSaver(
    bytes: ByteArray?,
    suggestedName: String = "file",
    extension: String? = null,
    directory: PlatformFile? = null,
    dialogSettings: FileKitDialogSettings = FileKitDialogSettings.createDefault(),
): PlatformFile?
