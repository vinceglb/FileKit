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
import io.github.vinceglb.filekit.dialogs.openFileSaver

@Composable
internal actual fun rememberPlatformFileSaverLauncher(
    dialogSettings: FileKitDialogSettings,
    onError: (FileKitDialogException) -> Unit,
    onResult: (PlatformFile?) -> Unit,
): SaverResultLauncher {
    val coroutineScope = rememberCoroutineScope()
    val pendingState = remember { LauncherPendingState("file saver") }
    val stableDialogSettings = rememberStableDialogSettings(dialogSettings)
    val currentDialogSettings by rememberUpdatedState(stableDialogSettings)
    val currentOnError by rememberUpdatedState(onError)
    val currentOnResult by rememberUpdatedState(onResult)

    return remember {
        SaverResultLauncher { suggestedName, defaultExtension, allowedExtensions, directory ->
            coroutineScope.launchSinglePendingDialog(pendingState) { finishPendingLaunch ->
                runDialogLauncher(
                    openDialog = {
                        FileKit.openFileSaver(
                            suggestedName = suggestedName,
                            defaultExtension = defaultExtension,
                            allowedExtensions = allowedExtensions,
                            directory = directory,
                            dialogSettings = currentDialogSettings,
                        )
                    },
                    beforeCallback = finishPendingLaunch,
                    onError = currentOnError,
                    onResult = currentOnResult,
                )
            }
        }
    }
}
