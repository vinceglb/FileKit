package io.github.vinceglb.filekit.dialogs.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.openFileSaver
import kotlinx.coroutines.launch

/**
 * Creates and remembers a [PickerResultLauncher] for picking a directory.
 *
 * @param title The title of the dialog. Supported on desktop platforms.
 * @param directory The initial directory. Supported on desktop platforms.
 * @param dialogSettings Platform-specific settings for the dialog.
 * @param onResult Callback invoked with the picked directory, or null if cancelled.
 * @return A [PickerResultLauncher] that can be used to launch the picker.
 */
@Composable
public expect fun rememberDirectoryPickerLauncher(
    title: String? = null,
    directory: PlatformFile? = null,
    dialogSettings: FileKitDialogSettings = FileKitDialogSettings.createDefault(),
    onResult: (PlatformFile?) -> Unit,
): PickerResultLauncher

/**
 * Creates and remembers a [SaverResultLauncher] for saving a file.
 *
 * @param dialogSettings Platform-specific settings for the dialog.
 * @param onResult Callback invoked with the saved file path, or null if cancelled.
 * @return A [SaverResultLauncher] that can be used to launch the saver.
 */
@Composable
public fun rememberFileSaverLauncher(
    dialogSettings: FileKitDialogSettings,
    onResult: (PlatformFile?) -> Unit,
): SaverResultLauncher {
    // Init FileKit
    InitFileKit()

    // Coroutine
    val coroutineScope = rememberCoroutineScope()

    // Updated state
    val currentOnResult by rememberUpdatedState(onResult)

    // FileKit launcher
    val returnedLauncher = remember {
        SaverResultLauncher { suggestedName, extension, directory ->
            coroutineScope.launch {
                val result = FileKit.openFileSaver(
                    suggestedName = suggestedName,
                    extension = extension,
                    directory = directory,
                    dialogSettings = dialogSettings,
                )
                currentOnResult(result)
            }
        }
    }

    return returnedLauncher
}
