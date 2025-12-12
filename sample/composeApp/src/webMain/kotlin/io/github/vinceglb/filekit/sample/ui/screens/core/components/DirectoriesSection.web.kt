package io.github.vinceglb.filekit.sample.ui.screens.core.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.vinceglb.filekit.sample.ui.components.FeatureCard

@Composable
actual fun DirectoriesSection(
    modifier: Modifier,
) {
    FeatureCard(title = "Directories", modifier = modifier) {
        Text("App directories are not available on web.")
    }
}
