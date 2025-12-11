package io.github.vinceglb.filekit.sample

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.vinceglb.filekit.PlatformFile

@Composable
actual fun ShareFileSection(
    filesToShare: List<PlatformFile>,
    modifier: Modifier,
) {
    FeatureCard(title = "Share file", modifier = modifier) {
        Text("Sharing is not supported on web.")
    }
}
