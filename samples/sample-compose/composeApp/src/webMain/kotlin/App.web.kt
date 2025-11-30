import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings

@Composable
actual fun PickDirectory(
    dialogSettings: FileKitDialogSettings,
    directory: PlatformFile?,
    onPickDirectory: (PlatformFile?) -> Unit,
    modifier: Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = { }, enabled = false) {
            Text("Directory picker")
        }

        Text("Directory picker is not supported")
    }
}

@Composable
actual fun TakePhoto(onTakePhoto: (PlatformFile?) -> Unit) {
}

@Composable
actual fun ShareButton(file: PlatformFile) {
}
