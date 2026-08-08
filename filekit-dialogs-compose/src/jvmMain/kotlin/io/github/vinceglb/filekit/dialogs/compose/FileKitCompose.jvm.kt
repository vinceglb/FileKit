@file:Suppress("ktlint:compose:param-order-check")

package io.github.vinceglb.filekit.dialogs.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.WindowScope
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogException
import io.github.vinceglb.filekit.dialogs.FileKitDialogParent
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitPickerException
import io.github.vinceglb.filekit.dialogs.FileKitType

@Composable
public fun <PickerResult, ConsumedResult> WindowScope.rememberFilePickerLauncher(
    type: FileKitType = FileKitType.File(),
    mode: FileKitMode<PickerResult, ConsumedResult>,
    directory: PlatformFile? = null,
    dialogSettings: FileKitDialogSettings? = null,
    onResult: (ConsumedResult?) -> Unit,
): PickerResultLauncher = io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher(
    type = type,
    mode = mode,
    directory = directory,
    dialogSettings = injectDialogSettings(dialogSettings, FileKitDialogParent.awt(this.window)),
    onResult = onResult,
)

@Composable
public fun <PickerResult, ConsumedResult> WindowScope.rememberFilePickerLauncher(
    type: FileKitType = FileKitType.File(),
    mode: FileKitMode<PickerResult, ConsumedResult>,
    directory: PlatformFile? = null,
    dialogSettings: FileKitDialogSettings? = null,
    onError: (FileKitPickerException) -> Unit,
    onResult: (ConsumedResult?) -> Unit,
): PickerResultLauncher = io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher(
    type = type,
    mode = mode,
    directory = directory,
    dialogSettings = injectDialogSettings(dialogSettings, FileKitDialogParent.awt(this.window)),
    onError = onError,
    onResult = onResult,
)

@Composable
public fun WindowScope.rememberFilePickerLauncher(
    type: FileKitType = FileKitType.File(),
    directory: PlatformFile? = null,
    dialogSettings: FileKitDialogSettings? = null,
    onResult: (PlatformFile?) -> Unit,
): PickerResultLauncher = io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher(
    type = type,
    directory = directory,
    dialogSettings = injectDialogSettings(dialogSettings, FileKitDialogParent.awt(this.window)),
    onResult = onResult,
)

@Composable
public fun WindowScope.rememberFilePickerLauncher(
    type: FileKitType = FileKitType.File(),
    directory: PlatformFile? = null,
    dialogSettings: FileKitDialogSettings? = null,
    onError: (FileKitPickerException) -> Unit,
    onResult: (PlatformFile?) -> Unit,
): PickerResultLauncher = io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher(
    type = type,
    directory = directory,
    dialogSettings = injectDialogSettings(dialogSettings, FileKitDialogParent.awt(this.window)),
    onError = onError,
    onResult = onResult,
)

@Composable
public fun WindowScope.rememberDirectoryPickerLauncher(
    directory: PlatformFile? = null,
    dialogSettings: FileKitDialogSettings? = null,
    onResult: (PlatformFile?) -> Unit,
): PickerResultLauncher = io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher(
    directory = directory,
    dialogSettings = injectDialogSettings(dialogSettings, FileKitDialogParent.awt(this.window)),
    onResult = onResult,
)

@Composable
public fun WindowScope.rememberDirectoryPickerLauncher(
    directory: PlatformFile? = null,
    dialogSettings: FileKitDialogSettings? = null,
    onError: (FileKitDialogException) -> Unit,
    onResult: (PlatformFile?) -> Unit,
): PickerResultLauncher = io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher(
    directory = directory,
    dialogSettings = injectDialogSettings(dialogSettings, FileKitDialogParent.awt(this.window)),
    onError = onError,
    onResult = onResult,
)

@Composable
public fun WindowScope.rememberFileSaverLauncher(
    dialogSettings: FileKitDialogSettings? = null,
    onResult: (PlatformFile?) -> Unit,
): SaverResultLauncher = io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher(
    dialogSettings = injectDialogSettings(dialogSettings, FileKitDialogParent.awt(this.window)),
    onResult = onResult,
)

@Composable
public fun WindowScope.rememberFileSaverLauncher(
    dialogSettings: FileKitDialogSettings? = null,
    onError: (FileKitDialogException) -> Unit,
    onResult: (PlatformFile?) -> Unit,
): SaverResultLauncher = io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher(
    dialogSettings = injectDialogSettings(dialogSettings, FileKitDialogParent.awt(this.window)),
    onError = onError,
    onResult = onResult,
)

internal fun injectDialogSettings(
    dialogSettings: FileKitDialogSettings?,
    parent: FileKitDialogParent,
): FileKitDialogSettings = dialogSettings
    ?.copy(parent = parent)
    ?: FileKitDialogSettings(parent = parent)
