package io.github.vinceglb.filekit.sample.shared.ui.screens.filesaver

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.github.vinceglb.filekit.ExperimentalFileKitApi
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogException
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.openFileSaver
import io.github.vinceglb.filekit.writeString
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch

@OptIn(ExperimentalFileKitApi::class)
@Composable
internal actual fun rememberFileSaverLauncher(
    dialogSettings: FileKitDialogSettings,
    onError: (FileKitDialogException) -> Unit,
    onResult: (PlatformFile?) -> Unit,
): FileSaverLauncher {
    val scope = rememberCoroutineScope()

    return remember(scope, dialogSettings, onError, onResult) {
        object : FileSaverLauncher {
            override val isSupported: Boolean = true
            override val supportsDirectorySelection: Boolean = false
            override val supportMessage: String =
                "Web opens the native save picker when available. Otherwise, writing the file downloads it. " +
                    "Save In is ignored on web."

            override fun launch(
                suggestedName: String,
                defaultExtension: String?,
                allowedExtensions: Set<String>?,
                directory: PlatformFile?,
            ) {
                // Run up to showSaveFilePicker() synchronously to preserve the click's user activation.
                scope.launch(start = CoroutineStart.UNDISPATCHED) {
                    try {
                        val file = FileKit.openFileSaver(
                            suggestedName = suggestedName,
                            defaultExtension = defaultExtension,
                            allowedExtensions = allowedExtensions,
                            directory = directory,
                            dialogSettings = dialogSettings,
                        )
                        if (file != null) {
                            file.writeString("FileKit web file saver sample")
                        }
                        onResult(file)
                    } catch (failure: FileKitDialogException) {
                        onError(failure)
                    } catch (failure: CancellationException) {
                        throw failure
                    } catch (failure: Exception) {
                        onError(FileKitDialogException("Could not write the selected file", failure))
                    }
                }
            }
        }
    }
}
