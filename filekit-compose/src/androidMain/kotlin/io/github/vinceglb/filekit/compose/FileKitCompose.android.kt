package io.github.vinceglb.filekit.compose

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import io.github.vinceglb.filekit.core.FileKit
import androidx.compose.ui.platform.LocalInspectionMode

@Composable
internal actual fun InitFileKit() {
    if (!LocalInspectionMode.current) {
        val componentActivity = LocalContext.current as ComponentActivity
        LaunchedEffect(Unit) {
            FileKit.init(componentActivity)
        }
    }
}
