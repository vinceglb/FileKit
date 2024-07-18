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
import io.github.vinceglb.filekit.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.compose.rememberFileSaverLauncher
import io.github.vinceglb.filekit.core.FileKit
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import io.github.vinceglb.filekit.core.PlatformDirectory
import io.github.vinceglb.filekit.core.PlatformFile
import io.github.vinceglb.filekit.core.baseName
import io.github.vinceglb.filekit.core.extension
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

    val singleFilePicker = rememberFilePickerLauncher(
        type = PickerType.Image,
        title = "Single file picker",
        initialDirectory = directory?.path,
        onResult = { file -> file?.let { files += it } }
    )

    val multipleFilesPicker = rememberFilePickerLauncher(
        type = PickerType.Image,
        mode = PickerMode.Multiple(maxItems = 4),
        title = "Multiple files picker",
        initialDirectory = directory?.path,
        onResult = { file -> file?.let { files += it } }
    )

    val filePicker = rememberFilePickerLauncher(
        type = PickerType.File(listOf("png")),
        title = "Single file picker, only png",
        initialDirectory = directory?.path,
        onResult = { file -> file?.let { files += it } }
    )

    val filesPicker = rememberFilePickerLauncher(
        type = PickerType.File(listOf("png")),
        mode = PickerMode.Multiple(),
        title = "Multiple files picker, only png",
        initialDirectory = directory?.path,
        onResult = { file -> file?.let { files += it } }
    )

    val directoryPicker = rememberDirectoryPickerLauncher(
        title = "Directory picker",
        initialDirectory = directory?.path,
        onResult = { dir -> directory = dir }
    )

    val saver = rememberFileSaverLauncher { file ->
        file?.let { files += it }
    }

    val scope = rememberCoroutineScope()
    fun saveFile(file: PlatformFile) {
        scope.launch {
            saver.launch(
                bytes = file.readBytes(),
                baseName = file.baseName,
                extension = file.extension,
                initialDirectory = directory?.path
            )
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

            Button(onClick = { filePicker.launch() }) {
                Text("Single file picker, only png")
            }

            Button(onClick = { filesPicker.launch() }) {
                Text("Multiple files picker, only png")
            }

            Button(
                onClick = { directoryPicker.launch() },
                enabled = FileKit.isDirectoryPickerSupported()
            ) {
                Text("Directory picker")
            }

            if (FileKit.isDirectoryPickerSupported()) {
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
