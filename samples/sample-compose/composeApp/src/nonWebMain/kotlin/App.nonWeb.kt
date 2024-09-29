import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import io.github.vinceglb.filekit.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.core.FileKitPlatformSettings
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.path

@Composable
actual fun PickDirectory(
    platformSettings: FileKitPlatformSettings?,
    directory: PlatformFile?,
    onDirectoryPicked: (PlatformFile?) -> Unit
) {
    val directoryPicker = rememberDirectoryPickerLauncher(
        title = "Directory picker",
        initialDirectory = directory?.path,
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
    get() = path
