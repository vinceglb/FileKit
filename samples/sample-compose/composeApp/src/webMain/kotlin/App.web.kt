import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import io.github.vinceglb.filekit.core.FileKitPlatformSettings
import io.github.vinceglb.filekit.core.PlatformFile

@Composable
actual fun PickDirectory(
    platformSettings: FileKitPlatformSettings?,
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
