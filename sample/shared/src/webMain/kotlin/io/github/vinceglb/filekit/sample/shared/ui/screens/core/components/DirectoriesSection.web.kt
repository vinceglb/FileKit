package io.github.vinceglb.filekit.sample.shared.ui.screens.core.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.vinceglb.filekit.sample.shared.ui.components.FeatureCard

@Composable
internal actual fun DirectoriesSection(
    modifier: Modifier,
) {
    FeatureCard(title = "Directories", modifier = modifier) {
        Text("App directories are not available on web.")
    }
}
