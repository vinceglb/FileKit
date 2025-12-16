package io.github.vinceglb.filekit.sample.shared.ui.screens.core.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.vinceglb.filekit.sample.shared.ui.components.FeatureCard

@Composable
internal actual fun WebDownloadSection(
    modifier: Modifier,
) {
    FeatureCard(title = "Web download", modifier = modifier) {
        Text("Download API is only available on web targets.")
    }
}
