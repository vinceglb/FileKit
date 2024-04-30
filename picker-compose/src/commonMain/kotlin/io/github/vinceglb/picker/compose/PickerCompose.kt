package io.github.vinceglb.picker.compose

import androidx.compose.runtime.Composable
import io.github.vinceglb.picker.core.PickerSelectionMode
import io.github.vinceglb.picker.core.PlatformFile

@Composable
public expect fun <Out> rememberPickerLauncher(
    mode: PickerSelectionMode<Out>,
    title: String? = null,
    initialDirectory: String? = null,
    onResult: (Out?) -> Unit,
): PickerResultLauncher

@Composable
public expect fun rememberSaverLauncher(
    fileExtension: String,
    onResult: (PlatformFile?) -> Unit
): SaverResultLauncher
