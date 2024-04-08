package io.github.vinceglb.picker.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import io.github.vinceglb.picker.core.Picker
import io.github.vinceglb.picker.core.PickerSelectionMode
import kotlinx.coroutines.launch

@Composable
public actual fun <Out> rememberPickerLauncher(
    mode: PickerSelectionMode<Out>,
    title: String?,
    initialDirectory: String?,
    onResult: (Out?) -> Unit
): PickerResultLauncher {
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
