import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialog.compose.rememberTakePhotoLauncher

@Composable
actual fun TakePhoto(onPhotoTaken: (PlatformFile?) -> Unit) {
    val takePhotoLauncher = rememberTakePhotoLauncher {
        onPhotoTaken(it)
    }

    Button(onClick = { takePhotoLauncher.launch() }) {
        Text("Take photo")
    }
}
