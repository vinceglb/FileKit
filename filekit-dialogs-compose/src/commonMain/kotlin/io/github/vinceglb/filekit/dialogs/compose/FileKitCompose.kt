@file:Suppress("ktlint:compose:param-order-check")

package io.github.vinceglb.filekit.dialogs.compose

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogException
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitPickerException
import io.github.vinceglb.filekit.dialogs.FileKitPickerState
import io.github.vinceglb.filekit.dialogs.FileKitType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Creates and remembers a [PickerResultLauncher] for picking files.
 *
 * @param type The type of files to pick. Defaults to [FileKitType.File].
 * @param mode The picking mode (e.g. Single, Multiple).
 * @param directory The initial directory. Supported on desktop platforms.
 * @param dialogSettings Platform-specific settings for the dialog.
 * @param onResult Callback invoked with the result.
 * @return A [PickerResultLauncher] that can be used to launch the picker.
 *
 * Picker failures are ignored by this overload. Use the overload with `onError` to handle them.
 */
@Composable
public fun <PickerResult, ConsumedResult> rememberFilePickerLauncher(
    type: FileKitType = FileKitType.File(),
    mode: FileKitMode<PickerResult, ConsumedResult>,
    directory: PlatformFile? = null,
    dialogSettings: FileKitDialogSettings = FileKitDialogSettings.createDefault(),
    onResult: (ConsumedResult) -> Unit,
): PickerResultLauncher = rememberFilePickerLauncher(
    type = type,
    mode = mode,
    directory = directory,
    dialogSettings = dialogSettings,
    onError = {},
    onResult = onResult,
)

/**
 * Creates and remembers a [PickerResultLauncher] for picking files.
 *
 * @param type The type of files to pick. Defaults to [FileKitType.File].
 * @param mode The picking mode (e.g. Single, Multiple).
 * @param directory The initial directory. Supported on desktop platforms.
 * @param dialogSettings Platform-specific settings for the dialog.
 * @param onError Callback invoked when FileKit cannot launch the picker or resolve the selected files.
 * @param onResult Callback invoked with the result.
 * @return A [PickerResultLauncher] that can be used to launch the picker.
 */
@Composable
public fun <PickerResult, ConsumedResult> rememberFilePickerLauncher(
    type: FileKitType = FileKitType.File(),
    mode: FileKitMode<PickerResult, ConsumedResult>,
    directory: PlatformFile? = null,
    dialogSettings: FileKitDialogSettings = FileKitDialogSettings.createDefault(),
    onError: (FileKitPickerException) -> Unit,
    onResult: (ConsumedResult) -> Unit,
): PickerResultLauncher {
    val stableDialogSettings = rememberStableDialogSettings(dialogSettings)
    return rememberPlatformFilePickerLauncher(
        type = type,
        mode = mode,
        directory = directory,
        dialogSettings = stableDialogSettings,
        onError = onError,
        onResult = onResult,
    )
}

/**
 * Creates and remembers a [PickerResultLauncher] for picking a single file.
 *
 * @param type The type of files to pick. Defaults to [FileKitType.File].
 * @param directory The initial directory. Supported on desktop platforms.
 * @param dialogSettings Platform-specific settings for the dialog.
 * @param onResult Callback invoked with the picked file, or null if cancelled.
 * @return A [PickerResultLauncher] that can be used to launch the picker.
 *
 * Picker failures are ignored by this overload. Use the overload with `onError` to handle them.
 */
@Composable
public fun rememberFilePickerLauncher(
    type: FileKitType = FileKitType.File(),
    directory: PlatformFile? = null,
    dialogSettings: FileKitDialogSettings = FileKitDialogSettings.createDefault(),
    onResult: (PlatformFile?) -> Unit,
): PickerResultLauncher = rememberFilePickerLauncher(
    type = type,
    directory = directory,
    dialogSettings = dialogSettings,
    onError = {},
    onResult = onResult,
)

/**
 * Creates and remembers a [PickerResultLauncher] for picking a single file.
 *
 * @param type The type of files to pick. Defaults to [FileKitType.File].
 * @param directory The initial directory. Supported on desktop platforms.
 * @param dialogSettings Platform-specific settings for the dialog.
 * @param onError Callback invoked when FileKit cannot launch the picker or resolve the selected file.
 * @param onResult Callback invoked with the picked file, or null if cancelled.
 * @return A [PickerResultLauncher] that can be used to launch the picker.
 */
@Composable
public fun rememberFilePickerLauncher(
    type: FileKitType = FileKitType.File(),
    directory: PlatformFile? = null,
    dialogSettings: FileKitDialogSettings = FileKitDialogSettings.createDefault(),
    onError: (FileKitPickerException) -> Unit,
    onResult: (PlatformFile?) -> Unit,
): PickerResultLauncher = rememberFilePickerLauncher(
    type = type,
    mode = FileKitMode.Single,
    directory = directory,
    dialogSettings = dialogSettings,
    onError = onError,
    onResult = onResult,
)

@Composable
internal expect fun <PickerResult, ConsumedResult> rememberPlatformFilePickerLauncher(
    type: FileKitType,
    mode: FileKitMode<PickerResult, ConsumedResult>,
    directory: PlatformFile?,
    dialogSettings: FileKitDialogSettings,
    onError: (FileKitPickerException) -> Unit,
    onResult: (ConsumedResult) -> Unit,
): PickerResultLauncher

internal suspend fun <PickerResult, ConsumedResult> runFilePickerLauncher(
    mode: FileKitMode<PickerResult, ConsumedResult>,
    openPicker: suspend () -> PickerResult,
    beforeCallback: () -> Unit = {},
    onError: (FileKitPickerException) -> Unit,
    onResult: (ConsumedResult) -> Unit,
) {
    val result = try {
        openPicker()
    } catch (failure: FileKitPickerException) {
        beforeCallback()
        onError(failure)
        return
    }
    mode.consumeResult(result) { consumedResult ->
        if (mode.isTerminalConsumedResult(consumedResult)) beforeCallback()
        onResult(consumedResult)
    }
}

private fun <PickerResult, ConsumedResult> FileKitMode<PickerResult, ConsumedResult>.isTerminalConsumedResult(
    result: ConsumedResult,
): Boolean = when (this) {
    FileKitMode.Single,
    is FileKitMode.Multiple,
    -> true

    FileKitMode.SingleWithState,
    is FileKitMode.MultipleWithState,
    -> result is FileKitPickerState.Cancelled ||
        result is FileKitPickerState.Failed ||
        result is FileKitPickerState.Completed<*>
}

internal suspend fun <Result> runDialogLauncher(
    openDialog: suspend () -> Result,
    beforeCallback: () -> Unit = {},
    onError: (FileKitDialogException) -> Unit,
    onResult: (Result) -> Unit,
) {
    val result = try {
        openDialog()
    } catch (failure: FileKitDialogException) {
        beforeCallback()
        onError(failure)
        return
    }
    beforeCallback()
    onResult(result)
}

internal fun <Result> legacyNullResultOnDialogFailure(
    onResult: (Result?) -> Unit,
): (FileKitDialogException) -> Unit = { onResult(null) }

internal fun legacyIgnoreDialogFailure(): (FileKitDialogException) -> Unit = {}

internal class LauncherPendingState(
    private val launcherName: String,
) {
    internal class LaunchToken

    private var pendingLaunch: LaunchToken? = null

    fun begin(): LaunchToken {
        check(pendingLaunch == null) { "A $launcherName launch is already pending." }
        return LaunchToken().also { pendingLaunch = it }
    }

    fun finish(launch: LaunchToken) {
        if (pendingLaunch === launch) pendingLaunch = null
    }
}

internal fun CoroutineScope.launchSinglePendingDialog(
    pendingState: LauncherPendingState,
    block: suspend (finishPendingLaunch: () -> Unit) -> Unit,
) {
    val launch = pendingState.begin()
    val finishPendingLaunch = { pendingState.finish(launch) }
    launch {
        try {
            block(finishPendingLaunch)
        } finally {
            finishPendingLaunch()
        }
    }
}

/**
 * Creates and remembers a [PickerResultLauncher] for picking a directory.
 *
 * @param directory The initial directory. Supported on desktop platforms.
 * @param dialogSettings Platform-specific settings for the dialog.
 * @param onResult Callback invoked with the picked directory, or null if cancelled.
 * @return A [PickerResultLauncher] that can be used to launch the picker.
 *
 * Dialog failures are reported as `onResult(null)` by this compatibility overload.
 * Use the overload with `onError` to distinguish failure from cancellation.
 */
@Composable
public expect fun rememberDirectoryPickerLauncher(
    directory: PlatformFile? = null,
    dialogSettings: FileKitDialogSettings = FileKitDialogSettings.createDefault(),
    onResult: (PlatformFile?) -> Unit,
): PickerResultLauncher

/**
 * Creates and remembers a [PickerResultLauncher] for picking a directory.
 *
 * @param directory The initial directory. Supported on desktop platforms.
 * @param dialogSettings Platform-specific settings for the dialog.
 * @param onError Callback invoked when the directory picker fails.
 * @param onResult Callback invoked with the picked directory, or null if cancelled.
 * @return A [PickerResultLauncher] that can be used to launch the picker.
 */
@Composable
public expect fun rememberDirectoryPickerLauncher(
    directory: PlatformFile? = null,
    dialogSettings: FileKitDialogSettings = FileKitDialogSettings.createDefault(),
    onError: (FileKitDialogException) -> Unit,
    onResult: (PlatformFile?) -> Unit,
): PickerResultLauncher
