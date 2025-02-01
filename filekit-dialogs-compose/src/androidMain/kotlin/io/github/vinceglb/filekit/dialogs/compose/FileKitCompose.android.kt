package io.github.vinceglb.filekit.dialogs.compose

import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalInspectionMode
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init

@Composable
internal actual fun InitFileKit() {
    if (!LocalInspectionMode.current) {
        val registry = LocalActivityResultRegistryOwner.current?.activityResultRegistry

        // if null then MainActivity is not an Activity that implements ActivityResultRegistryOwner e.g. ComponentActivity
        // This should not generally happen
        // Calls to launcher should fail with FileKitNotInitializedException if it wasn't previously initialized
        LaunchedEffect(Unit) {
            if (registry != null) {
                FileKit.init(registry)
            }
        }
    }
}
