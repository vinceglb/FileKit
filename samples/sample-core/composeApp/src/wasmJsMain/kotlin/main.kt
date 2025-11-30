
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import io.github.vinceglb.sample.core.compose.App

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport {
		App()
	}
}
