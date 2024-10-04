import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes

@Composable
actual fun rememberFileCoilModel(file: PlatformFile): Any? {
    var bytes by remember(file) { mutableStateOf<ByteArray?>(null) }

    LaunchedEffect(file) {
        bytes = file.readBytes()
    }

    return bytes
}
