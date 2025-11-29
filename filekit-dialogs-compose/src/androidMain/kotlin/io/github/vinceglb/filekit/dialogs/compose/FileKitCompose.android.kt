package io.github.vinceglb.filekit.dialogs.compose

import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.core.net.toUri
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitOpenCameraSettings
import io.github.vinceglb.filekit.dialogs.TakePictureWithCameraFacing
import io.github.vinceglb.filekit.dialogs.init
import io.github.vinceglb.filekit.dialogs.toAndroidUri

@Composable
internal actual fun InitFileKit() {
    if (!LocalInspectionMode.current) {
        val registry = LocalActivityResultRegistryOwner.current?.activityResultRegistry

        // if null then MainActivity is not an Activity that implements ActivityResultRegistryOwner e.g. ComponentActivity
        // This should not generally happen
        // Calls to launcher should fail with FileKitNotInitializedException if it wasn't previously initialized
        LaunchedEffect(registry) {
            if (registry != null) {
                FileKit.init(registry)
            }
        }
    }
}

@Composable
public actual fun rememberCameraPickerLauncher(
    openCameraSettings: FileKitOpenCameraSettings,
    onResult: (PlatformFile?) -> Unit,
): PhotoResultLauncher {
    // Init FileKit
    InitFileKit()

    // Store the destination file URI string to survive process death
    var pendingDestinationUri by rememberSaveable { mutableStateOf<String?>(null) }

    // Updated callback
    val currentOnResult by rememberUpdatedState(onResult)

    // Create a stable contract instance (reused across recompositions)
    val contract = remember { TakePictureWithCameraFacing() }

    // Create the launcher using the Activity Result API
    val launcher = rememberLauncherForActivityResult(contract) { success ->
        val uri = pendingDestinationUri
        val result = if (success && uri != null) {
            PlatformFile(uri.toUri())
        } else {
            null
        }
        pendingDestinationUri = null
        currentOnResult(result)
    }

    // Return the PhotoResultLauncher wrapper
    return remember(launcher, contract) {
        PhotoResultLauncher { type, cameraFacing, destinationFile ->
            // Store the destination URI for retrieval after potential activity recreation
            val uri = destinationFile.toAndroidUri(openCameraSettings.authority)
            pendingDestinationUri = uri.toString()

            // Set the camera facing on the contract before launching
            contract.setCameraFacing(cameraFacing)

            // Launch the camera
            launcher.launch(uri)
        }
    }
}
