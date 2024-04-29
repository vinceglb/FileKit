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
import io.github.vinceglb.picker.core.PickerSelectionMode
import io.github.vinceglb.sample.core.MainViewModel
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.dsl.module

@Composable
fun App() {
    KoinApplication(
        application = {
            modules(module {
                factory { MainViewModel() }
            })
        }
    ) {
        MaterialTheme {
            SampleApp()
        }
    }
}

@Composable
private fun SampleApp(viewModel: MainViewModel = koinInject<MainViewModel>()) {
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

            Button(
                onClick = viewModel::pickDirectory,
                enabled = PickerSelectionMode.Directory.isSupported
            ) {
                Text("Directory picker")
            }

            if (uiState.loading) {
                CircularProgressIndicator()
            }

            if (PickerSelectionMode.Directory.isSupported) {
                Text("Selected directory: ${uiState.directory?.path ?: "None"}")
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
                items(uiState.files.toList()) {
                    PhotoItem(it, viewModel::saveFile)
                }
            }
        }
    }
}
