import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vinceglb.picker.compose.rememberPickerLauncher
import io.github.vinceglb.picker.compose.rememberSaverLauncher
import io.github.vinceglb.picker.core.PickerSelectionMode
import io.github.vinceglb.picker.core.PlatformDirectory
import io.github.vinceglb.picker.core.PlatformFile
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        SampleApp()
    }
}

@Composable
private fun SampleApp() {
    var files: Set<PlatformFile> by remember { mutableStateOf(emptySet()) }
    var directory: PlatformDirectory? by remember { mutableStateOf(null) }

    val singleFilePicker = rememberPickerLauncher(
        mode = PickerSelectionMode.SingleFile(extensions = listOf("png", "jpg", "jpeg")),
        title = "Single file picker",
        initialDirectory = directory?.path,
        onResult = { file -> file?.let { files += it } }
    )

    val multipleFilesPicker = rememberPickerLauncher(
        mode = PickerSelectionMode.MultipleFiles(extensions = listOf("png", "jpg", "jpeg")),
        title = "Multiple files picker",
        initialDirectory = directory?.path,
        onResult = { file -> file?.let { files += it } }
    )

    val directoryPicker = rememberPickerLauncher(
        mode = PickerSelectionMode.Directory,
        title = "Directory picker",
        initialDirectory = directory?.path,
        onResult = { dir -> directory = dir }
    )

    val saver = rememberSaverLauncher(
        fileExtension = "jpg",
        onResult = { file -> file?.let { files += it } }
    )

    val scope = rememberCoroutineScope()
    fun saveFile(file: PlatformFile) {
        scope.launch {
            saver.launch(file.readBytes(), file.name, directory?.path)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = { singleFilePicker.launch() }) {
                Text("Single file picker")
            }

            Button(onClick = { multipleFilesPicker.launch() }) {
                Text("Multiple files picker")
            }

            Button(
                onClick = { directoryPicker.launch() },
                enabled = PickerSelectionMode.Directory.isSupported
            ) {
                Text("Directory picker")
            }

            if (PickerSelectionMode.Directory.isSupported) {
                Text("Selected directory: ${directory?.path ?: "None"}")
            } else {
                Text("Directory picker is not supported")
            }

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 128.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(files.toList()) {
                    PhotoItem(file = it, onSaveFile = ::saveFile)
                }
            }
        }
    }
}
