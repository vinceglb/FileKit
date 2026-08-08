package io.github.vinceglb.filekit.sample.shared.ui.screens.sharefile

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogException

internal interface ShareFileLauncher {
    val isSupported: Boolean

    fun launch(files: List<PlatformFile>)
}

@Composable
internal expect fun rememberShareFileLauncher(
    onError: (FileKitDialogException) -> Unit,
): ShareFileLauncher
