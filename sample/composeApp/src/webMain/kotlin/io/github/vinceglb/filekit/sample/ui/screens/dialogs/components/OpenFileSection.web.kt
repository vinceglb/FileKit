package io.github.vinceglb.filekit.sample.ui.screens.dialogs.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.sample.ui.components.FeatureCard

@Composable
actual fun OpenFileSection(
    fileToOpen: PlatformFile?,
    modifier: Modifier,
) {
    FeatureCard(title = "Open file with default app", modifier = modifier) {
        Text("Opening files with the default application is not supported on web.")
    }
}
