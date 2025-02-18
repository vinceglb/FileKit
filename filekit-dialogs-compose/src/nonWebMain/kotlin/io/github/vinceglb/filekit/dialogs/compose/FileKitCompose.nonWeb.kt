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
import io.github.vinceglb.filekit.dialogs.openFileSaver
import kotlinx.coroutines.launch

@Composable
public fun rememberDirectoryPickerLauncher(
    title: String? = null,
    initialDirectory: String? = null,               // TODO change to PlatformFile?
    platformSettings: FileKitDialogSettings = FileKitDialogSettings.createDefault(),
    onResult: (PlatformFile?) -> Unit,
): PickerResultLauncher {
    // Init FileKit
    InitFileKit()

    // Coroutine
    val coroutineScope = rememberCoroutineScope()

    // Updated state
    val currentTitle by rememberUpdatedState(title)
    val currentInitialDirectory by rememberUpdatedState(initialDirectory)
    val currentOnResult by rememberUpdatedState(onResult)

    // FileKit launcher
    val returnedLauncher = remember {
        PickerResultLauncher {
            coroutineScope.launch {
                val result = FileKit.openDirectoryPicker(
                    title = currentTitle,
                    initialDirectory = currentInitialDirectory,
                    platformSettings = platformSettings,
                )
                currentOnResult(result)
            }
        }
    }

    return returnedLauncher
}

@Composable
public actual fun rememberFileSaverLauncher(
    platformSettings: FileKitDialogSettings,
    onResult: (PlatformFile?) -> Unit
): SaverResultLauncher {
    // Init FileKit
    InitFileKit()

    // Coroutine
    val coroutineScope = rememberCoroutineScope()

    // Updated state
    val currentOnResult by rememberUpdatedState(onResult)

    // FileKit launcher
    val returnedLauncher = remember {
        SaverResultLauncher { bytes, baseName, extension, initialDirectory ->
            coroutineScope.launch {
                val result = FileKit.openFileSaver(
                    bytes = bytes,
                    baseName = baseName,
                    extension = extension,
                    initialDirectory = initialDirectory,
                    platformSettings = platformSettings,
                )
                currentOnResult(result)
            }
        }
    }

    return returnedLauncher
}
