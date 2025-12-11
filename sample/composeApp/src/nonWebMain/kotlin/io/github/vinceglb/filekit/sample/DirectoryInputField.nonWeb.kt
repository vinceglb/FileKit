package io.github.vinceglb.filekit.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.path

@Composable
actual fun DirectoryInputField(
    label: String,
    directory: PlatformFile?,
    onDirectoryChange: (PlatformFile?) -> Unit,
    modifier: Modifier,
) {
    var pathText by remember(directory) { mutableStateOf(directory?.path.orEmpty()) }

    val launcher = rememberDirectoryPickerLauncher(
        title = label,
        directory = directory,
    ) { picked ->
        pathText = picked?.path.orEmpty()
        onDirectoryChange(picked)
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = pathText,
            onValueChange = {
                pathText = it
                onDirectoryChange(it.toPlatformFileOrNull())
            },
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = { launcher.launch() }) {
                Text("Browse")
            }
            if (pathText.isNotBlank()) {
                TextButton(
                    onClick = {
                        pathText = ""
                        onDirectoryChange(null)
                    },
                ) {
                    Text("Clear")
                }
            }
        }
    }
}

private fun String.toPlatformFileOrNull(): PlatformFile? =
    trim()
        .takeIf(String::isNotEmpty)
        ?.let { runCatching { PlatformFile(it) }.getOrNull() }
