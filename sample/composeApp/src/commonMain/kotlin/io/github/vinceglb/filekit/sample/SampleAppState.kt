package io.github.vinceglb.filekit.sample

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.vinceglb.filekit.PlatformFile

class SampleAppState {
    var lastPickedFile: PlatformFile? by mutableStateOf(null)
    var lastPickedFiles: List<PlatformFile>? by mutableStateOf(null)
    var lastPickedDirectory: PlatformFile? by mutableStateOf(null)
    var lastSavedFile: PlatformFile? by mutableStateOf(null)
    var lastCameraFile: PlatformFile? by mutableStateOf(null)
}

@Composable
fun rememberSampleAppState(): SampleAppState = remember { SampleAppState() }
