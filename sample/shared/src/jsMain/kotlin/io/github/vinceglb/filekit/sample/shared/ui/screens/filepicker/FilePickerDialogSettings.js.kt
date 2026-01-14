package io.github.vinceglb.filekit.sample.shared.ui.screens.filepicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings

/**
 * JS implementation - no settings available.
 */
private class JsFilePickerDialogSettingsState : FilePickerDialogSettingsState {
    override fun build(): FileKitDialogSettings = FileKitDialogSettings()
}

@Composable
internal actual fun rememberFilePickerDialogSettingsState(): FilePickerDialogSettingsState = remember { JsFilePickerDialogSettingsState() }

@Composable
internal actual fun FilePickerDialogSettingsContent(state: FilePickerDialogSettingsState) {
    // No settings available on JS
}
