package io.github.vinceglb.picker.compose

import androidx.compose.runtime.Composable
import io.github.vinceglb.picker.core.PickerSelectionMode

@Composable
public expect fun <Out> rememberPickerLauncher(
    mode: PickerSelectionMode<Out>,
    title: String? = null,
    initialDirectory: String? = null,
    onResult: (Out?) -> Unit,
): PickerResultLauncher
