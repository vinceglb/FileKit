
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialog.FileKitDialogSettings

@Composable
actual fun PickDirectory(
    platformSettings: FileKitDialogSettings?,
    directory: PlatformFile?,
    onDirectoryPicked: (PlatformFile?) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = { }, enabled = false) {
            Text("Directory picker")
        }

        Text("Directory picker is not supported")
    }
}

actual val PlatformFile.safePath: String?
    get() = null

@Composable
actual fun TakePhoto(onPhotoTaken: (PlatformFile?) -> Unit) {
}