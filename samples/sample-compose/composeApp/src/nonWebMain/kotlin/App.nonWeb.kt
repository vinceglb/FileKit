
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialog.FileKitDialogSettings
import io.github.vinceglb.filekit.dialog.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.path

@Composable
actual fun PickDirectory(
    platformSettings: FileKitDialogSettings?,
    directory: PlatformFile?,
    onDirectoryPicked: (PlatformFile?) -> Unit
) {
    val directoryPicker = rememberDirectoryPickerLauncher(
        title = "Directory picker",
        initialDirectory = directory?.path.toString(),
        onResult = onDirectoryPicked,
        platformSettings = platformSettings
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = { directoryPicker.launch() }) {
            Text("Directory picker")
        }

        Text("Selected directory: ${directory?.path ?: "None"}")
    }
}

actual val PlatformFile.safePath: String?
    get() = path.toString()
