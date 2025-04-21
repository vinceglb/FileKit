package io.github.vinceglb.sample.explorer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.list
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.parent
import io.github.vinceglb.filekit.startAccessingSecurityScopedResource
import io.github.vinceglb.filekit.stopAccessingSecurityScopedResource
import io.github.vinceglb.sample.explorer.icon.EllipsisVertical
import io.github.vinceglb.sample.explorer.icon.ExplorerIcons
import io.github.vinceglb.sample.explorer.icon.FolderUp
import io.github.vinceglb.sample.explorer.util.dateFormat
import io.github.vinceglb.sample.explorer.util.lastModified
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun AppContent() {
    var directory by remember { mutableStateOf<PlatformFile?>(null) }

// == Some tests on Android
//
//    LaunchedEffect(Unit) {
//        val testFile = FileKit.cacheDir / "test.txt"
//        testFile.writeString("Hello world!")
//
//        val subDirectory = FileKit.cacheDir / "sub directory"
//        subDirectory.createDirectories()
//
//        directory = FileKit.cacheDir
//    }

    val directoryPickerLauncher = rememberDirectoryPickerLauncher { file ->
        directory = file
    }

    Scaffold { contentPadding ->
        directory.let {
            when (it) {
                null -> NoDirectoryContent(
                    onPickDirectoryClick = directoryPickerLauncher::launch,
                    modifier = Modifier.fillMaxSize(),
                )

                else -> DirectorySelectedContent(
                    rootDirectory = it,
                    contentPadding = contentPadding,
                    modifier = Modifier.fillMaxSize(),
                    onPickDirectoryClick = directoryPickerLauncher::launch,
                )
            }
        }
    }
}

@Composable
private fun NoDirectoryContent(
    onPickDirectoryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxSize(),
    ) {
        Text(text = "Select a directory")
        Button(onClick = onPickDirectoryClick) {
            Text(text = "Pick Directory")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DirectorySelectedContent(
    rootDirectory: PlatformFile,
    onPickDirectoryClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    var currentDirectory by remember(rootDirectory) { mutableStateOf(rootDirectory) }
    var files by remember { mutableStateOf<List<PlatformFile>>(emptyList()) }

    var refreshKey by remember { mutableStateOf(0) }

    DisposableEffect(rootDirectory, currentDirectory, refreshKey) {
        rootDirectory.startAccessingSecurityScopedResource()
        files = currentDirectory.list().sortedByDescending { it.lastModified() }
        onDispose { rootDirectory.stopAccessingSecurityScopedResource() }
    }

    var selectedFile by remember { mutableStateOf<PlatformFile?>(null) }

    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding
    ) {
        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Button(onClick = onPickDirectoryClick) {
                    Text(text = "Change Directory")
                }

                Button(onClick = { refreshKey++ }) {
                    Text("Reload from disk")
                }

                if (currentDirectory != rootDirectory) {
                    Button(onClick = { currentDirectory.parent()?.let { currentDirectory = it } }) {
                        Icon(
                            imageVector = ExplorerIcons.FolderUp,
                            contentDescription = null,
                            modifier = Modifier
                                .size(20.dp)
                                .padding(end = 4.dp),
                        )
                        Text(text = "Directory Up")
                    }
                }
            }
        }

        item {
            Text("Current Directory: ${currentDirectory.name}")
        }

        items(files) { file ->
            FileListItem(
                file = file,
                onClick = { selectedFile = file },
            )
        }
    }

    selectedFile?.let { file ->
        FileBottomSheet(
            file = file,
            onDismissRequest = { selectedFile = null },
            onRefreshRequest = { refreshKey++ },
            onOpenSubDirectoryRequest = { currentDirectory = it },
        )
    }
}

@Composable
private fun FileListItem(
    file: PlatformFile,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strDate = remember(file) {
        val localDate = file.lastModified().toLocalDateTime(TimeZone.currentSystemDefault())
        dateFormat.format(localDate)
    }

    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.small,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
                .padding(start = 16.dp, end = 0.dp)
        ) {
            FileIcon(file)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = strDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }

            IconButton(onClick = {}) {
                Icon(
                    imageVector = ExplorerIcons.EllipsisVertical,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
