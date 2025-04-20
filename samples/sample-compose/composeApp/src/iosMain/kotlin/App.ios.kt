
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.compose.rememberCameraPickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberShareFileLauncher

@Composable
actual fun TakePhoto(onPhotoTaken: (PlatformFile?) -> Unit) {
    val takePhotoLauncher = rememberCameraPickerLauncher {
        onPhotoTaken(it)
    }

    Button(onClick = { takePhotoLauncher.launch() }) {
        Text("Take photo")
    }
}

@Composable
actual fun ShareButton(file: PlatformFile) {
    val shareLauncher = rememberShareFileLauncher()

    IconButton(
        onClick = { shareLauncher.launch(file) },
        modifier = Modifier.size(36.dp)
    ) {
        Icon(
            Icons.Default.Share,
            modifier = Modifier.size(22.dp),
            contentDescription = "share",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
