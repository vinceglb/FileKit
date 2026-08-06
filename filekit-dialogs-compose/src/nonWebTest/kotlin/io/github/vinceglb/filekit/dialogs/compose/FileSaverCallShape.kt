@file:Suppress("UNUSED_VARIABLE")

package io.github.vinceglb.filekit.dialogs.compose

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogException
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings

@Composable
private fun CompileFileSaverCallShapes() {
    val settings = FileKitDialogSettings.createDefault()
    val legacy = rememberFileSaverLauncher(settings) { _: PlatformFile? -> }
    val explicit = rememberFileSaverLauncher(
        dialogSettings = settings,
        onError = { _: FileKitDialogException -> },
        onResult = { _: PlatformFile? -> },
    )
}
