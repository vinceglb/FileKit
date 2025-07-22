package io.github.vinceglb.sample.core.compose

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.coil.addPlatformFileSupport
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.sample.core.MainViewModel

@Composable
fun App(dialogSettings: FileKitDialogSettings = FileKitDialogSettings.createDefault()) {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context = context)
            .components { addPlatformFileSupport() }
            .build()
    }

    MaterialTheme {
        SampleApp(viewModel = viewModel { MainViewModel(dialogSettings) })
    }
}

@Composable
private fun SampleApp(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = viewModel::pickImage) {
                Text("Single image picker")
            }

            Button(onClick = viewModel::pickImages) {
                Text("Multiple image picker")
            }

            Button(onClick = viewModel::pickFile) {
                Text("Single file picker, only jpg / png")
            }

            Button(onClick = viewModel::pickFiles) {
                Text("Multiple files picker, only jpg / png")
            }

            Button(onClick = viewModel::pickFilesWithState) {
                Text("Multiple files picker with state")
            }

            Button(onClick = viewModel::takePhoto) {
                Text("Take photo")
            }

            PickDirectoryButton(
                directory = uiState.directory,
                onClick = viewModel::pickDirectory
            )

            if (uiState.loading) {
                CircularProgressIndicator()
            }

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 128.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(uiState.files.toList()) {
                    PhotoItem(it, viewModel::saveFile, viewModel::shareFile)
                }
            }
        }
    }
}

@Composable
expect fun PickDirectoryButton(
    directory: PlatformFile?,
    onClick: () -> Unit
)
