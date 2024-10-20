
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
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialog.FileKitDialogSettings
import io.github.vinceglb.filekit.dialog.PickerMode
import io.github.vinceglb.filekit.dialog.PickerType
import io.github.vinceglb.filekit.dialog.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.dialog.compose.rememberFileSaverLauncher
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.nameWithoutExtension
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(platformSettings: FileKitDialogSettings? = null) {
    MaterialTheme {
        SampleApp(platformSettings)
    }
}

@Composable
private fun SampleApp(platformSettings: FileKitDialogSettings?) {
    var files: Set<PlatformFile> by remember { mutableStateOf(emptySet()) }
    var directory: PlatformFile? by remember { mutableStateOf(null) }

    val singleFilePicker = rememberFilePickerLauncher(
        type = PickerType.Image,
        title = "Single file picker",
        initialDirectory = directory?.safePath,
        onResult = { file -> file?.let { files += it } },
        platformSettings = platformSettings
    )

    val multipleFilesPicker = rememberFilePickerLauncher(
        type = PickerType.Image,
        mode = PickerMode.Multiple(maxItems = 4),
        title = "Multiple files picker",
        initialDirectory = directory?.safePath,
        onResult = { file -> file?.let { files += it } },
        platformSettings = platformSettings
    )

    val filePicker = rememberFilePickerLauncher(
        type = PickerType.File(listOf("png")),
        title = "Single file picker, only png",
        initialDirectory = directory?.safePath,
        onResult = { file -> file?.let { files += it } },
        platformSettings = platformSettings
    )

    val filesPicker = rememberFilePickerLauncher(
        type = PickerType.File(listOf("png")),
        mode = PickerMode.Multiple(),
        title = "Multiple files picker, only png",
        initialDirectory = directory?.safePath,
        onResult = { file -> file?.let { files += it } },
        platformSettings = platformSettings
    )

    val saver = rememberFileSaverLauncher { file ->
        file?.let { files += it }
    }

    val scope = rememberCoroutineScope()
    fun saveFile(file: PlatformFile) {
        scope.launch {
            saver.launch(
                bytes = file.readBytes(),
                baseName = file.nameWithoutExtension ?: "file",
                extension = file.extension ?: "txt",
                initialDirectory = directory?.safePath
            )
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
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

            PickDirectory(
                platformSettings = platformSettings,
                directory = directory,
                onDirectoryPicked = { directory = it }
            )

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

@Composable
expect fun PickDirectory(
    platformSettings: FileKitDialogSettings?,
    directory: PlatformFile?,
    onDirectoryPicked: (PlatformFile?) -> Unit,
)

expect val PlatformFile.safePath: String?
