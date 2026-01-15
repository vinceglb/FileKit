package io.github.vinceglb.filekit.sample.shared.ui.screens.filepicker

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings

/**
 * State holder for platform-specific dialog settings.
 */
internal interface FilePickerDialogSettingsState {
    /**
     * Builds a [FileKitDialogSettings] instance from the current state.
     */
    fun build(): FileKitDialogSettings
}

/**
 * Remembers the platform-specific dialog settings state.
 */
@Composable
internal expect fun rememberFilePickerDialogSettingsState(): FilePickerDialogSettingsState

/**
 * Composable that displays platform-specific dialog settings UI.
 * On platforms without settings, this composable renders nothing.
 *
 * @param state The dialog settings state to display and modify.
 */
@Composable
internal expect fun FilePickerDialogSettingsContent(state: FilePickerDialogSettingsState)
