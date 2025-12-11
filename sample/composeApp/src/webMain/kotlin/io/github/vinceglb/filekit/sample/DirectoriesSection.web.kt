package io.github.vinceglb.filekit.sample

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun DirectoriesSection(
    modifier: Modifier,
) {
    FeatureCard(title = "Directories", modifier = modifier) {
        Text("App directories are not available on web.")
    }
}
