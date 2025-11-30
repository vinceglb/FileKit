package io.github.vinceglb.filekit.dialogs.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.openDirectoryPicker
import kotlinx.coroutines.launch

@Composable
public actual fun rememberDirectoryPickerLauncher(
    title: String?,
    directory: PlatformFile?,
    dialogSettings: FileKitDialogSettings,
    onResult: (PlatformFile?) -> Unit,
): PickerResultLauncher {
    // Init FileKit
    InitFileKit()

    // Coroutine
    val coroutineScope = rememberCoroutineScope()

    // Updated state
    val currentTitle by rememberUpdatedState(title)
    val currentDirectory by rememberUpdatedState(directory)
    val currentOnResult by rememberUpdatedState(onResult)

    // FileKit launcher
    val returnedLauncher = remember {
        PickerResultLauncher {
            coroutineScope.launch {
                val result = FileKit.openDirectoryPicker(
                    title = currentTitle,
                    directory = currentDirectory,
                    dialogSettings = dialogSettings,
                )
                currentOnResult(result)
            }
        }
    }

    return returnedLauncher
}
