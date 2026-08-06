@file:Suppress("UNUSED_VARIABLE")

package io.github.vinceglb.filekit.dialogs.compose

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.dialogs.FileKitDialogException

@Composable
private fun CompileSharingLauncherCallShapes() {
    val legacy = rememberShareFileLauncher()
    val explicit = rememberShareFileLauncher(
        onError = { _: FileKitDialogException -> },
    )
}
