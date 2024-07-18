package io.github.vinceglb.filekit.compose

import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import io.github.vinceglb.filekit.core.FileKit

@Composable
internal actual fun InitFileKit() {
    if (!LocalInspectionMode.current) {
        val context = LocalContext.current
        val registry = LocalActivityResultRegistryOwner.current?.activityResultRegistry

        // if null then MainActivity is not an Activity that implements ActivityResultRegistryOwner e.g. ComponentActivity
        // This should not generally happen
        // Calls to launcher should fail with FileKitNotInitializedException if it wasn't previously initialized
        if (registry != null) {
            LaunchedEffect(Unit) {
                FileKit.init(context, registry)
            }
        }
    }
}
