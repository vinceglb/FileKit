import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.underlyingFile

@Composable
actual fun rememberFileCoilModel(file: PlatformFile): Any? = file.underlyingFile
