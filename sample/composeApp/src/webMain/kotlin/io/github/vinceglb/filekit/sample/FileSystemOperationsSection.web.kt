package io.github.vinceglb.filekit.sample

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.vinceglb.filekit.PlatformFile

@Composable
actual fun FileSystemOperationsSection(
    selectedFile: PlatformFile?,
    onFileUpdate: (PlatformFile?) -> Unit,
    modifier: Modifier,
) {
    FeatureCard(title = "File system operations", modifier = modifier) {
        Text("Direct file system operations are not available on web.")
    }
}
