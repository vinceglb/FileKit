
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.compose.rememberCameraPickerLauncher
import io.github.vinceglb.filekit.dialogs.shareImageFile

@Composable
actual fun TakePhoto(onPhotoTaken: (PlatformFile?) -> Unit) {
    val takePhotoLauncher = rememberCameraPickerLauncher {
        onPhotoTaken(it)
    }

    Button(onClick = { takePhotoLauncher.launch() }) {
        Text("Take photo")
    }
}


actual suspend fun shareImageIfSupported(file: PlatformFile) {
    FileKit.shareImageFile(file)
}