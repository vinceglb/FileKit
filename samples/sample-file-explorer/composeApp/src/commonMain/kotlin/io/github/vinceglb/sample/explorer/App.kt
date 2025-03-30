package io.github.vinceglb.sample.explorer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.nameWithoutExtension
import io.github.vinceglb.filekit.writeString
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    var fileToSave by remember { mutableStateOf<PlatformFile?>(null) }
    val localCoroutineScope = rememberCoroutineScope()
    val launcher = rememberFileSaverLauncher { file ->
        println("File saved: $file")
        fileToSave = file
    }

    fun saveFile() {
        localCoroutineScope.launch {
            fileToSave?.writeString("Hello world! ${fileToSave?.name}")
        }
    }

    MaterialTheme {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = { launcher.launch("test", "txt") }) {
                Text("Pick!")
            }

            Button(onClick = ::saveFile) {
                Text("Save to ${fileToSave?.nameWithoutExtension}")
            }
        }
    }
}