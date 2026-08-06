@file:Suppress("UNUSED_VARIABLE")

package io.github.vinceglb.filekit.dialogs.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.WindowScope
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogException
import io.github.vinceglb.filekit.dialogs.FileKitPickerException

@Composable
private fun WindowScope.CompileJvmPickerCallShapes() {
    val legacy = rememberFilePickerLauncher { _: PlatformFile? -> }
    val explicit = rememberFilePickerLauncher(
        onError = { _: FileKitPickerException -> },
        onResult = { _: PlatformFile? -> },
    )

    val legacyDirectory = rememberDirectoryPickerLauncher { _: PlatformFile? -> }
    val explicitDirectory = rememberDirectoryPickerLauncher(
        onError = { _: FileKitDialogException -> },
        onResult = { _: PlatformFile? -> },
    )
}
