package io.github.vinceglb.filekit.sample

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun WebDownloadSection(
    modifier: Modifier,
) {
    FeatureCard(title = "Web download", modifier = modifier) {
        Text("Download API is only available on web targets.")
    }
}
