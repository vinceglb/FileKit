package io.github.vinceglb.filekit.sample.shared.ui.screens.dialogs.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.sample.shared.ui.components.FeatureCard

@Composable
internal actual fun CameraPickerSection(
    lastPhoto: PlatformFile?,
    onPhotoCapture: (PlatformFile?) -> Unit,
    modifier: Modifier,
) {
    FeatureCard(title = "Camera picker", modifier = modifier) {
        Text("Camera picker is not supported on web.")
    }
}
