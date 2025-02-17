package io.github.vinceglb.filekit.dialogs.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.download
import kotlinx.coroutines.launch

@Deprecated(message = "Opening file saver dialog is not supported on web targets. Please use expect/actual to provide web and non-web implementations.")
@Composable
public actual fun rememberFileSaverLauncher(
    platformSettings: FileKitDialogSettings,
    onResult: (PlatformFile?) -> Unit
): SaverResultLauncher {
    // Coroutine
    val coroutineScope = rememberCoroutineScope()

    // Updated state
    val currentOnResult by rememberUpdatedState(onResult)

    return remember {
        SaverResultLauncher { bytes, baseName, extension, _ ->
            coroutineScope.launch {
                if (bytes != null) {
                    FileKit.download(
                        bytes = bytes,
                        fileName = "$baseName.$extension"
                    )
                    currentOnResult(null)
                }
            }
        }
    }
}

@Composable
internal actual fun InitFileKit() {}
