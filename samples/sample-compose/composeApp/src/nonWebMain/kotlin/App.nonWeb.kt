
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.path

@Composable
actual fun PickDirectory(
    dialogSettings: FileKitDialogSettings,
    directory: PlatformFile?,
    onPickDirectory: (PlatformFile?) -> Unit,
    modifier: Modifier,
) {
    val directoryPicker = rememberDirectoryPickerLauncher(
        title = "Directory picker",
        directory = directory,
        onResult = onPickDirectory,
        dialogSettings = dialogSettings,
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = { directoryPicker.launch() }) {
            Text("Directory picker")
        }

        Text("Selected directory: ${directory?.path ?: "None"}")
    }
}
