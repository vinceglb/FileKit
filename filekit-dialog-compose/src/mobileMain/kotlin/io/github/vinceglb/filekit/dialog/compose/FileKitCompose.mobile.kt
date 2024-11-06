package io.github.vinceglb.filekit.dialog.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialog.takePhoto
import kotlinx.coroutines.launch

@Composable
public fun rememberTakePhotoLauncher(
    onResult: (PlatformFile?) -> Unit,
): PhotoResultLauncher {
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
        PhotoResultLauncher {
            coroutineScope.launch {
                val result = fileKit.takePhoto()
                currentOnResult(result)
            }
        }
    }

    return returnedLauncher
}
