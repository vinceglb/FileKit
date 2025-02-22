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
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.nameWithoutExtension
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(dialogSettings: FileKitDialogSettings = FileKitDialogSettings.createDefault()) {
    MaterialTheme {
        SampleApp(dialogSettings)
    }
}

@Composable
private fun SampleApp(dialogSettings: FileKitDialogSettings) {
    var files: Set<PlatformFile> by remember { mutableStateOf(emptySet()) }
    var directory: PlatformFile? by remember { mutableStateOf(null) }

    val singleFilePicker = rememberFilePickerLauncher(
        type = FileKitType.Image,
        title = "Single file picker",
        initialDirectory = directory?.safePath,
        onResult = { file -> file?.let { files += it } },
        dialogSettings = dialogSettings
    )

    val multipleFilesPicker = rememberFilePickerLauncher(
        type = FileKitType.Image,
        mode = FileKitMode.Multiple(maxItems = 4),
        title = "Multiple files picker",
        initialDirectory = directory?.safePath,
        onResult = { file -> file?.let { files += it } },
        dialogSettings = dialogSettings
    )

    val filePicker = rememberFilePickerLauncher(
        type = FileKitType.File(listOf("jpg", "png")),
        title = "Single file picker, only jpg / png",
        initialDirectory = directory?.safePath,
        onResult = { file -> file?.let { files += it } },
        dialogSettings = dialogSettings
    )

    val filesPicker = rememberFilePickerLauncher(
        type = FileKitType.File("jpg", "png"),
        mode = FileKitMode.Multiple(),
        title = "Multiple files picker, only jpg / png",
        initialDirectory = directory?.safePath,
        onResult = { file -> file?.let { files += it } },
        dialogSettings = dialogSettings
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

            TakePhoto(
                onPhotoTaken = { file -> file?.let { files += it } }
            )

            PickDirectory(
                dialogSettings = dialogSettings,
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
    dialogSettings: FileKitDialogSettings,
    directory: PlatformFile?,
    onDirectoryPicked: (PlatformFile?) -> Unit,
)

@Composable
expect fun TakePhoto(onPhotoTaken: (PlatformFile?) -> Unit)

expect val PlatformFile.safePath: String?
