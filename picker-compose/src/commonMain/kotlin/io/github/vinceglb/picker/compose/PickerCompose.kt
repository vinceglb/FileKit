package io.github.vinceglb.picker.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import io.github.vinceglb.picker.core.Picker
import io.github.vinceglb.picker.core.PickerSelectionMode
import io.github.vinceglb.picker.core.PlatformFile
import kotlinx.coroutines.launch

@Composable
public fun <Out> rememberPickerLauncher(
    mode: PickerSelectionMode<Out>,
    title: String? = null,
    initialDirectory: String? = null,
    onResult: (Out?) -> Unit,
): PickerResultLauncher {
    // Init picker
    InitPicker()

    // Coroutine
    val coroutineScope = rememberCoroutineScope()

    // Updated state
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
                val result = picker.pick(
                    mode = currentMode,
                    title = currentTitle,
                    initialDirectory = currentInitialDirectory,
                )
                currentOnResult(result)
            }
        }
    }

    return returnedLauncher
}

@Composable
public fun rememberSaverLauncher(
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
                val result = picker.save(
                    bytes = bytes,
                    baseName = baseName,
                    extension = extension,
                    initialDirectory = initialDirectory,
                )
                currentOnResult(result)
            }
        }
    }

    return returnedLauncher
}

@Composable
internal expect fun InitPicker()
