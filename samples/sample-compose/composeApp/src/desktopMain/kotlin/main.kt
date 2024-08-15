import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.vinceglb.filekit.core.FileKitPlatformSettings

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "SampleCompose") {
        val platformSettings = FileKitPlatformSettings(this.window)
        App(platformSettings)
    }
}
