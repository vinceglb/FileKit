import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "SampleCompose") {
        val dialogSettings = FileKitDialogSettings(this.window)
        App(dialogSettings)
    }
}
