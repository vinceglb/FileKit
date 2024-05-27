package io.github.vinceglb.picker.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import io.github.vinceglb.picker.core.Picker
import io.github.vinceglb.picker.core.PickerPlatformSettings
import io.github.vinceglb.picker.core.PickerSelectionMode
import io.github.vinceglb.picker.core.PickerSelectionType
import io.github.vinceglb.picker.core.PlatformDirectory
import io.github.vinceglb.picker.core.PlatformFile
import kotlinx.coroutines.launch

@Composable
public fun <Out> rememberFilePickerLauncher(
    type: PickerSelectionType = PickerSelectionType.File(),
    mode: PickerSelectionMode<Out>,
    title: String? = null,
    initialDirectory: String? = null,
    platformSettings: PickerPlatformSettings? = null,
    onResult: (Out?) -> Unit,
): PickerResultLauncher {
    // Init picker
    InitPicker()

    // Coroutine
    val coroutineScope = rememberCoroutineScope()

    // Updated state
    val currentType by rememberUpdatedState(type)
    val currentMode by rememberUpdatedState(mode)
    val currentTitle by rememberUpdatedState(title)
    val currentInitialDirectory by rememberUpdatedState(initialDirectory)
    val currentOnResult by rememberUpdatedState(onResult)

    // Picker
    val picker = remember { Picker }

    // Picker launcher
    val returnedLauncher = remember {
        PickerResultLauncher {
            coroutineScope.launch {
                val result = picker.pickFile(
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
    type: PickerSelectionType = PickerSelectionType.File(),
    title: String? = null,
    initialDirectory: String? = null,
    platformSettings: PickerPlatformSettings? = null,
    onResult: (PlatformFile?) -> Unit,
): PickerResultLauncher {
    return rememberFilePickerLauncher(
        type = type,
        mode = PickerSelectionMode.Single,
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
    platformSettings: PickerPlatformSettings? = null,
    onResult: (PlatformDirectory?) -> Unit,
): PickerResultLauncher {
    // Init picker
    InitPicker()

    // Coroutine
    val coroutineScope = rememberCoroutineScope()

    // Updated state
    val currentTitle by rememberUpdatedState(title)
    val currentInitialDirectory by rememberUpdatedState(initialDirectory)
    val currentOnResult by rememberUpdatedState(onResult)

    // Picker
    val picker = remember { Picker }

    // Picker launcher
    val returnedLauncher = remember {
        PickerResultLauncher {
            coroutineScope.launch {
                val result = picker.pickDirectory(
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
    platformSettings: PickerPlatformSettings? = null,
    onResult: (PlatformFile?) -> Unit
): SaverResultLauncher {
    // Init picker
    InitPicker()

    // Coroutine
    val coroutineScope = rememberCoroutineScope()

    // Updated state
    val currentOnResult by rememberUpdatedState(onResult)

    // Picker
    val picker = remember { Picker }

    // Picker launcher
    val returnedLauncher = remember {
        SaverResultLauncher { bytes, baseName, extension, initialDirectory ->
            coroutineScope.launch {
                val result = picker.saveFile(
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
internal expect fun InitPicker()
