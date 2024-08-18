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
import io.github.vinceglb.filekit.core.FileKitPlatformSettings
import io.github.vinceglb.filekit.core.IPlatformFile
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import io.github.vinceglb.filekit.core.baseName
import io.github.vinceglb.filekit.core.extension
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(platformSettings: FileKitPlatformSettings? = null) {
    MaterialTheme {
        SampleApp(platformSettings)
    }
}

@Composable
private fun SampleApp(platformSettings: FileKitPlatformSettings?) {
    var files: Set<IPlatformFile> by remember { mutableStateOf(emptySet()) }
    var directory: IPlatformFile? by remember { mutableStateOf(null) }

    val singleFilePicker = rememberFilePickerLauncher(
        type = PickerType.Image,
        title = "Single file picker",
        initialDirectory = directory?.getAbsolutePath(),
        onResult = { file -> file?.let { files += it } },
        platformSettings = platformSettings
    )

    val multipleFilesPicker = rememberFilePickerLauncher(
        type = PickerType.Image,
        mode = PickerMode.Multiple(maxItems = 4),
        title = "Multiple files picker",
        initialDirectory = directory?.getAbsolutePath(),
        onResult = { file -> file?.let { files += it } },
        platformSettings = platformSettings
    )

    val filePicker = rememberFilePickerLauncher(
        type = PickerType.File(listOf("png")),
        title = "Single file picker, only png",
        initialDirectory = directory?.getAbsolutePath(),
        onResult = { file -> file?.let { files += it } },
        platformSettings = platformSettings
    )

    val filesPicker = rememberFilePickerLauncher(
        type = PickerType.File(listOf("png")),
        mode = PickerMode.Multiple(),
        title = "Multiple files picker, only png",
        initialDirectory = directory?.getAbsolutePath(),
        onResult = { file -> file?.let { files += it } },
        platformSettings = platformSettings
    )

    val directoryPicker = rememberDirectoryPickerLauncher(
        title = "Directory picker",
        initialDirectory = directory?.getAbsolutePath(),
        onResult = { dir -> directory = dir },
        platformSettings = platformSettings
    )

    val saver = rememberFileSaverLauncher { inputFile, file ->
        inputFile?.openInputStream()?.use { input -> file?.openOutputStream()?.let { output -> input.transferTo(output) } }
        file?.let { files += it }
    }

    val scope = rememberCoroutineScope()
    fun saveFile(file: IPlatformFile) {
        scope.launch {
            saver.launch(
                inputFile = file,
                baseName = file.baseName,
                extension = file.extension,
                initialDirectory = directory?.getAbsolutePath()
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
                Text("Selected directory: ${directory?.getAbsolutePath() ?: "None"}")
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
