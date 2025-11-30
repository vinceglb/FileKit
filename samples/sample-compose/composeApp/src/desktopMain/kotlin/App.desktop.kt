
import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile

@Composable
actual fun TakePhoto(onTakePhoto: (PlatformFile?) -> Unit) {
}

@Composable
actual fun ShareButton(file: PlatformFile) {}
