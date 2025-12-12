package io.github.vinceglb.filekit.sample.ui.screens.core.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.ImageFormat
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.compressImage
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.sample.ui.components.FeatureCard
import io.github.vinceglb.filekit.saveImageToGallery
import io.github.vinceglb.filekit.size
import kotlinx.coroutines.launch

@Composable
actual fun ImageUtilsSection(
    selectedFile: PlatformFile?,
    modifier: Modifier,
) {
    val scope = rememberCoroutineScope()
    var format by remember { mutableStateOf(ImageFormat.JPEG) }
    var qualityText by remember { mutableStateOf("80") }
    var maxWidthText by remember { mutableStateOf("") }
    var maxHeightText by remember { mutableStateOf("") }
    var status by remember { mutableStateOf<String?>(null) }
    var compressedBytes by remember { mutableStateOf<ByteArray?>(null) }

    FeatureCard(title = "Image utils", modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (selectedFile == null) {
                Text("Pick an image file in the dialogs tab.")
                return@Column
            }

            FormatDropdown(
                selected = format,
                onSelect = { format = it },
            )

            OutlinedTextField(
                value = qualityText,
                onValueChange = { qualityText = it },
                label = { Text("Quality (0-100)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = maxWidthText,
                onValueChange = { maxWidthText = it },
                label = { Text("Max width (optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = maxHeightText,
                onValueChange = { maxHeightText = it },
                label = { Text("Max height (optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            ) {
                Button(
                    onClick = {
                        val quality = qualityText.toIntOrNull()?.coerceIn(0, 100) ?: 80
                        val maxWidth = maxWidthText.toIntOrNull()
                        val maxHeight = maxHeightText.toIntOrNull()
                        scope.launch {
                            val before = selectedFile.size()
                            val bytes = runCatching {
                                FileKit.compressImage(
                                    file = selectedFile,
                                    imageFormat = format,
                                    quality = quality,
                                    maxWidth = maxWidth,
                                    maxHeight = maxHeight,
                                )
                            }.getOrElse {
                                status = "Compress failed: ${it.message}"
                                return@launch
                            }
                            compressedBytes = bytes
                            status = "Compressed ${before}B → ${bytes.size}B"
                        }
                    },
                ) {
                    Text("Compress")
                }

                Button(
                    onClick = {
                        scope.launch {
                            val bytes = compressedBytes ?: selectedFile.readBytes()
                            val fileName = selectedFile.name
                            status = runCatching {
                                FileKit.saveImageToGallery(bytes, fileName)
                                "Saved to gallery: $fileName"
                            }.getOrElse { "Save failed: ${it.message}" }
                        }
                    },
                ) {
                    Text("Save to gallery")
                }
            }

            if (status != null) {
                Text(status.orEmpty())
            }
        }
    }
}

@Composable
private fun FormatDropdown(
    selected: ImageFormat,
    onSelect: (ImageFormat) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text("Format")
        OutlinedButton(onClick = { expanded = true }) {
            Text(selected.name)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ImageFormat.entries.forEach { format ->
                DropdownMenuItem(
                    text = { Text(format.name) },
                    onClick = {
                        expanded = false
                        onSelect(format)
                    },
                )
            }
        }
    }
}
