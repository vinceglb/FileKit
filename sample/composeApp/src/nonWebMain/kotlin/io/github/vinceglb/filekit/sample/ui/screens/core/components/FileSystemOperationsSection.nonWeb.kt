package io.github.vinceglb.filekit.sample.ui.screens.core.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
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
import io.github.vinceglb.filekit.atomicMove
import io.github.vinceglb.filekit.copyTo
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.isDirectory
import io.github.vinceglb.filekit.list
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readString
import io.github.vinceglb.filekit.sample.ui.components.FeatureCard
import io.github.vinceglb.filekit.sample.ui.components.PlatformFileInfoCard
import kotlinx.coroutines.launch

@Composable
actual fun FileSystemOperationsSection(
    selectedFile: PlatformFile?,
    onFileUpdate: (PlatformFile?) -> Unit,
    modifier: Modifier,
) {
    val scope = rememberCoroutineScope()
    var destinationPath by remember { mutableStateOf("") }
    var output by remember { mutableStateOf<String?>(null) }

    FeatureCard(title = "File system operations", modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (selectedFile == null) {
                Text("Pick a file or directory in the dialogs tab.")
                return@Column
            }

            PlatformFileInfoCard(file = selectedFile)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        scope.launch {
                            output = runCatching { selectedFile.readString() }
                                .map { it.take(500) }
                                .getOrElse { "Read failed: ${it.message}" }
                        }
                    },
                ) {
                    Text("Read text")
                }

                if (selectedFile.isDirectory()) {
                    Button(
                        onClick = {
                            output = runCatching {
                                selectedFile.list().joinToString(separator = "\n") { it.name }
                            }.getOrElse { "List failed: ${it.message}" }
                        },
                    ) {
                        Text("List children")
                    }
                }
            }

            OutlinedTextField(
                value = destinationPath,
                onValueChange = { destinationPath = it },
                label = { Text("Destination path for copy/move") },
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            ) {
                Button(
                    enabled = destinationPath.isNotBlank(),
                    onClick = {
                        val destination = destinationPath.toPlatformFileOrNull()
                        if (destination == null) {
                            output = "Invalid destination"
                            return@Button
                        }
                        scope.launch {
                            output = runCatching {
                                selectedFile.copyTo(destination)
                                "Copied to ${destination.name}"
                            }.getOrElse { "Copy failed: ${it.message}" }
                        }
                    },
                ) {
                    Text("Copy to")
                }

                Button(
                    enabled = destinationPath.isNotBlank(),
                    onClick = {
                        val destination = destinationPath.toPlatformFileOrNull()
                        if (destination == null) {
                            output = "Invalid destination"
                            return@Button
                        }
                        scope.launch {
                            output = runCatching {
                                selectedFile.atomicMove(destination)
                                onFileUpdate(destination)
                                "Moved to ${destination.name}"
                            }.getOrElse { "Move failed: ${it.message}" }
                        }
                    },
                ) {
                    Text("Move to")
                }

                Button(
                    onClick = {
                        scope.launch {
                            output = runCatching {
                                selectedFile.delete()
                                onFileUpdate(null)
                                "Deleted ${selectedFile.name}"
                            }.getOrElse { "Delete failed: ${it.message}" }
                        }
                    },
                ) {
                    Text("Delete")
                }
            }

            if (output != null) {
                Text(output.orEmpty())
            }
        }
    }
}

private fun String.toPlatformFileOrNull(): PlatformFile? =
    trim()
        .takeIf(String::isNotEmpty)
        ?.let { runCatching { PlatformFile(it) }.getOrNull() }
