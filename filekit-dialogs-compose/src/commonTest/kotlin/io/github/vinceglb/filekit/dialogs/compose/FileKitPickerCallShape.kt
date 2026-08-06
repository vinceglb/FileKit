@file:Suppress("UNUSED_VARIABLE")

package io.github.vinceglb.filekit.dialogs.compose

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitPickerException
import io.github.vinceglb.filekit.dialogs.FileKitPickerState

@Composable
private fun CompileCommonPickerCallShapes() {
    val legacySingle = rememberFilePickerLauncher { _: PlatformFile? -> }
    val explicitSingle = rememberFilePickerLauncher(
        onError = { _: FileKitPickerException -> },
        onResult = { _: PlatformFile? -> },
    )

    val legacyState = rememberFilePickerLauncher(
        mode = FileKitMode.SingleWithState,
        onResult = { _: FileKitPickerState<PlatformFile> -> },
    )
    val explicitState = rememberFilePickerLauncher(
        mode = FileKitMode.SingleWithState,
        onError = { _: FileKitPickerException -> },
        onResult = { _: FileKitPickerState<PlatformFile> -> },
    )
}
