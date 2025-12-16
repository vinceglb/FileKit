package io.github.vinceglb.filekit.sample.shared.ui.screens.core.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.sample.shared.ui.components.FeatureCard

@Composable
internal actual fun ImageUtilsSection(
    selectedFile: PlatformFile?,
    modifier: Modifier,
) {
    FeatureCard(title = "Image utils", modifier = modifier) {
        Text("Image compression and gallery saving are not supported on web.")
    }
}
