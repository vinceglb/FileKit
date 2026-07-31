package io.github.vinceglb.filekit.dialogs.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogException
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitPickerException
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openDirectoryPicker
import io.github.vinceglb.filekit.dialogs.openFilePicker

/**
 * Creates and remembers a [PickerResultLauncher] for picking a directory.
 *
 * @param directory The initial directory. Supported on desktop platforms.
 * @param dialogSettings Platform-specific settings for the dialog.
 * @param onResult Callback invoked with the picked directory, or null if cancelled.
 * @return A [PickerResultLauncher] that can be used to launch the picker.
 */
@Composable
public actual fun rememberDirectoryPickerLauncher(
    directory: PlatformFile?,
    dialogSettings: FileKitDialogSettings,
    onResult: (PlatformFile?) -> Unit,
): PickerResultLauncher = rememberDirectoryPickerLauncher(
    directory = directory,
    dialogSettings = dialogSettings,
    onError = legacyNullResultOnDialogFailure(onResult),
    onResult = onResult,
)

@Composable
public actual fun rememberDirectoryPickerLauncher(
    directory: PlatformFile?,
    dialogSettings: FileKitDialogSettings,
    onError: (FileKitDialogException) -> Unit,
    onResult: (PlatformFile?) -> Unit,
): PickerResultLauncher {
    val coroutineScope = rememberCoroutineScope()
    val pendingState = remember { LauncherPendingState("directory picker") }
    val stableDialogSettings = rememberStableDialogSettings(dialogSettings)

    val currentDirectory by rememberUpdatedState(directory)
    val currentDialogSettings by rememberUpdatedState(stableDialogSettings)
    val currentOnError by rememberUpdatedState(onError)
    val currentOnResult by rememberUpdatedState(onResult)

    return remember {
        PickerResultLauncher {
            coroutineScope.launchSinglePendingDialog(pendingState) {
                runDialogLauncher(
                    openDialog = {
                        FileKit.openDirectoryPicker(
                            directory = currentDirectory,
                            dialogSettings = currentDialogSettings,
                        )
                    },
                    onError = currentOnError,
                    onResult = currentOnResult,
                )
            }
        }
    }
}

@Composable
internal actual fun <PickerResult, ConsumedResult> rememberPlatformFilePickerLauncher(
    type: FileKitType,
    mode: FileKitMode<PickerResult, ConsumedResult>,
    directory: PlatformFile?,
    dialogSettings: FileKitDialogSettings,
    onError: (FileKitPickerException) -> Unit,
    onResult: (ConsumedResult) -> Unit,
): PickerResultLauncher {
    val coroutineScope = rememberCoroutineScope()
    val pendingState = remember { LauncherPendingState("file picker") }
    val stableDialogSettings = rememberStableDialogSettings(dialogSettings)

    val currentType by rememberUpdatedState(type)
    val currentMode by rememberUpdatedState(mode)
    val currentDirectory by rememberUpdatedState(directory)
    val currentDialogSettings by rememberUpdatedState(stableDialogSettings)
    val currentOnError by rememberUpdatedState(onError)
    val currentOnConsumed by rememberUpdatedState(onResult)

    return remember {
        PickerResultLauncher {
            coroutineScope.launchSinglePendingDialog(pendingState) {
                runFilePickerLauncher(
                    mode = currentMode,
                    openPicker = {
                        FileKit.openFilePicker(
                            type = currentType,
                            mode = currentMode,
                            directory = currentDirectory,
                            dialogSettings = currentDialogSettings,
                        )
                    },
                    onError = currentOnError,
                    onResult = currentOnConsumed,
                )
            }
        }
    }
}
