package io.github.vinceglb.filekit.compose

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import io.github.vinceglb.filekit.core.Picker

@Composable
internal actual fun InitPicker() {
    val componentActivity = LocalContext.current as ComponentActivity
    LaunchedEffect(Unit) {
        Picker.init(componentActivity)
    }
}
