package io.github.vinceglb.filekit.sample.shared.ui.screens.filedetails.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.atomicMove
import io.github.vinceglb.filekit.copyTo
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.dialogs.openDirectoryPicker
import io.github.vinceglb.filekit.dialogs.openFileSaver
import io.github.vinceglb.filekit.dialogs.openFileWithDefaultApplication
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.isDirectory
import io.github.vinceglb.filekit.isRegularFile
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.nameWithoutExtension
import io.github.vinceglb.filekit.parent
import io.github.vinceglb.filekit.sample.shared.ui.icons.Copy
import io.github.vinceglb.filekit.sample.shared.ui.icons.ExternalLink
import io.github.vinceglb.filekit.sample.shared.ui.icons.LucideIcons
import io.github.vinceglb.filekit.sample.shared.ui.icons.Trash
import io.github.vinceglb.filekit.sample.shared.ui.icons.Truck
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
internal actual fun FileDetailsActions(
    file: PlatformFile,
    onDeleteFile: () -> Unit,
    modifier: Modifier,
) {
    val scope = rememberCoroutineScope()

    Column(modifier = modifier) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.3f))

        // Open File / Folder
        FileDetailsActionRow(
            text = when {
                file.isDirectory() -> "Open Folder"
                else -> "Open File"
            },
            icon = LucideIcons.ExternalLink,
            onClick = { FileKit.openFileWithDefaultApplication(file) },
        )

        // Duplicate File
        if (file.isRegularFile()) {
            FileDetailsActionRow(
                text = "Duplicate File",
                icon = LucideIcons.Copy,
                onClick = {
                    scope.launch {
                        val selectedFile = FileKit.openFileSaver(
                            suggestedName = "${file.nameWithoutExtension}/(copy)",
                            extension = file.extension,
                            directory = file.parent(),
                        )

                        if (selectedFile != null) {
                            file.copyTo(selectedFile)
                            selectedFile.parent()?.let(FileKit::openFileWithDefaultApplication)
                        }
                    }
                },
            )
        }

        // Move File / Folder
        FileDetailsActionRow(
            text = when {
                file.isDirectory() -> "Move Folder"
                else -> "Move File"
            },
            icon = LucideIcons.Truck,
            onClick = {
                scope.launch {
                    val destinationFolder = FileKit.openDirectoryPicker()
                    if (destinationFolder != null) {
                        file.atomicMove(destinationFolder / file.name)
                        FileKit.openFileWithDefaultApplication(destinationFolder)
                    }
                }
            },
        )

        // Mobile Specific Actions
        FileDetailsMobileActions(
            file = file,
            scope = scope,
        )

        // Delete File
        if (file.isRegularFile()) {
            FileDetailsActionRow(
                text = "Delete File",
                icon = LucideIcons.Trash,
                onClick = {
                    scope.launch {
                        file.delete(mustExist = false)
                        onDeleteFile()
                    }
                },
            )
        }
    }
}

@Composable
internal fun FileDetailsActionRow(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.clickable(onClick = onClick)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
internal expect fun FileDetailsMobileActions(
    file: PlatformFile,
    scope: CoroutineScope,
)
