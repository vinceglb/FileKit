package io.github.vinceglb.filekit.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import io.github.vinceglb.filekit.core.FileKit
import io.github.vinceglb.filekit.core.FileKitPlatformSettings
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import io.github.vinceglb.filekit.core.PlatformDirectory
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.launch

@Composable
public fun <Out> rememberFilePickerLauncher(
    type: PickerType = PickerType.File(),
    mode: PickerMode<Out>,
    title: String? = null,
    initialDirectory: String? = null,
    platformSettings: FileKitPlatformSettings? = null,
    onResult: (Out?) -> Unit,
): PickerResultLauncher {
    // Init FileKit
    InitFileKit()

    // Coroutine
    val coroutineScope = rememberCoroutineScope()

    // Updated state
    val currentType by rememberUpdatedState(type)
    val currentMode by rememberUpdatedState(mode)
    val currentTitle by rememberUpdatedState(title)
    val currentInitialDirectory by rememberUpdatedState(initialDirectory)
    val currentOnResult by rememberUpdatedState(onResult)

    // FileKit
    val fileKit = remember { FileKit }

    // FileKit launcher
    val returnedLauncher = remember {
        PickerResultLauncher {
            coroutineScope.launch {
                val result = fileKit.pickFile(
                    type = currentType,
                    mode = currentMode,
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
public fun rememberFilePickerLauncher(
    type: PickerType = PickerType.File(),
    title: String? = null,
    initialDirectory: String? = null,
    platformSettings: FileKitPlatformSettings? = null,
    onResult: (PlatformFile?) -> Unit,
): PickerResultLauncher {
    return rememberFilePickerLauncher(
        type = type,
        mode = PickerMode.Single,
        title = title,
        initialDirectory = initialDirectory,
        platformSettings = platformSettings,
        onResult = onResult,
    )
}

@Composable
public fun rememberDirectoryPickerLauncher(
    title: String? = null,
    initialDirectory: String? = null,
    platformSettings: FileKitPlatformSettings? = null,
    onResult: (PlatformDirectory?) -> Unit,
): PickerResultLauncher {
    // Init FileKit
    InitFileKit()

    // Coroutine
    val coroutineScope = rememberCoroutineScope()

    // Updated state
    val currentTitle by rememberUpdatedState(title)
    val currentInitialDirectory by rememberUpdatedState(initialDirectory)
    val currentOnResult by rememberUpdatedState(onResult)

    // FileKit
    val fileKit = remember { FileKit }

    // FileKit launcher
    val returnedLauncher = remember {
        PickerResultLauncher {
            coroutineScope.launch {
                val result = fileKit.pickDirectory(
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
public fun rememberFileSaverLauncher(
    platformSettings: FileKitPlatformSettings? = null,
    onResult: (PlatformFile?) -> Unit
): SaverResultLauncher {
    // Init FileKit
    InitFileKit()

    // Coroutine
    val coroutineScope = rememberCoroutineScope()

    // Updated state
    val currentOnResult by rememberUpdatedState(onResult)

    // FileKit
    val fileKit = remember { FileKit }

    // FileKit launcher
    val returnedLauncher = remember {
        SaverResultLauncher { bytes, baseName, extension, initialDirectory ->
            coroutineScope.launch {
                val result = fileKit.saveFile(
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

@Composable
internal expect fun InitFileKit()
