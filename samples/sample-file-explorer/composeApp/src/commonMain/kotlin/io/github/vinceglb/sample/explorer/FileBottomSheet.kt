package io.github.vinceglb.sample.explorer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.atomicMove
import io.github.vinceglb.filekit.copyTo
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.dialogs.openFileSaver
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.isDirectory
import io.github.vinceglb.filekit.isRegularFile
import io.github.vinceglb.filekit.list
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.nameWithoutExtension
import io.github.vinceglb.filekit.parent
import io.github.vinceglb.filekit.size
import io.github.vinceglb.sample.explorer.icon.Copy
import io.github.vinceglb.sample.explorer.icon.ExplorerIcons
import io.github.vinceglb.sample.explorer.icon.Folder
import io.github.vinceglb.sample.explorer.icon.Trash
import io.github.vinceglb.sample.explorer.icon.Truck
import io.github.vinceglb.sample.explorer.util.createdAt
import io.github.vinceglb.sample.explorer.util.dateFormat
import io.github.vinceglb.sample.explorer.util.lastModified
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import nl.jacobras.humanreadable.HumanReadable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileBottomSheet(
    file: PlatformFile,
    onRefreshRequest: () -> Unit,
    onOpenSubDirectoryRequest: (PlatformFile) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    val (createdAt, lastModified) = remember(file) {
        val createdAt = file.createdAt()?.toLocalDateTime(TimeZone.currentSystemDefault())
        val lastModified = file.lastModified().toLocalDateTime(TimeZone.currentSystemDefault())
        createdAt?.let { dateFormat.format(createdAt) } to dateFormat.format(lastModified)
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        dragHandle = {},
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
            ) {
                FileIcon(file)
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            HorizontalDivider()

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(all = 16.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    FileInfo(
                        label = "Name",
                        value = file.nameWithoutExtension,
                        modifier = Modifier.weight(1f)
                    )

                    if (file.isRegularFile()) {
                        FileInfo(
                            label = "Extension",
                            value = file.extension,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    FileInfo(
                        label = "Created At",
                        value = createdAt ?: "-",
                        modifier = Modifier.weight(1f),
                    )

                    FileInfo(
                        label = "Modified",
                        value = lastModified,
                        modifier = Modifier.weight(1f),
                    )
                }

                if (file.isRegularFile()) {
                    FileInfo(
                        label = "Size",
                        value = HumanReadable.fileSize(file.size()),
                    )
                }

                if (file.isDirectory()) {
                    FileInfo(
                        label = "Children number",
                        value = file.list().size.toString(),
                    )
                }

                FileInfo(
                    label = "Parent Directory",
                    value = file.parent()?.name ?: "-",
                )

                FileInfo(
                    label = "Location",
                    value = file.absolutePath(),
                )
            }

            HorizontalDivider()

            if (file.isRegularFile()) {
                FileAction(
                    text = "Duplicate file",
                    icon = ExplorerIcons.Copy,
                    onClick = {
                        scope.launch {
                            val selectedFile = FileKit.openFileSaver(
                                suggestedName = "${file.nameWithoutExtension}/(copy)",
                                extension = file.extension,
                                directory = file.parent(),
                            )

                            if (selectedFile != null) {
                                file.copyTo(selectedFile)
                                onRefreshRequest()
                            }
                        }
                    }
                )
            }

            if (file.isDirectory()) {
                FileAction(
                    text = "Open directory",
                    icon = ExplorerIcons.Folder,
                    onClick = {
                        onOpenSubDirectoryRequest(file)
                        onDismissRequest()
                    }
                )
            }

            FileAction(
                text = when {
                    file.isDirectory() -> "Move directory"
                    else -> "Move file"
                },
                icon = ExplorerIcons.Truck,
                onClick = {
                    scope.launch {
                        println("name ${file.name} | nameWithoutExtension ${file.nameWithoutExtension} | extension ${file.extension} | parent ${file.parent()} | isRegularFile ${file.isRegularFile()} | isDirectory ${file.isDirectory()}")

                        val selectedFile = FileKit.openFileSaver(
                            suggestedName = file.nameWithoutExtension,
                            extension = file.extension.takeIf { file.isRegularFile() },
                            directory = file.parent(),
                        )

                        if (selectedFile != null) {
                            file.atomicMove(selectedFile)
                            onRefreshRequest()
                            onDismissRequest()
                        }
                    }
                }
            )

            PlatformActions(file, scope)

            if (file.isRegularFile()) {
                FileAction(
                    text = "Delete file",
                    icon = ExplorerIcons.Trash,
                    onClick = {
                        scope.launch {
                            file.delete()
                            onRefreshRequest()
                            onDismissRequest()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun FileInfo(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.outline,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun FileAction(
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
                .padding(all = 16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
expect fun PlatformActions(
    file: PlatformFile,
    scope: CoroutineScope,
)
