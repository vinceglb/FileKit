package io.github.vinceglb.filekit.sample.shared.ui.screens.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.sample.shared.ui.components.AppPickerResultsCard
import io.github.vinceglb.filekit.sample.shared.ui.components.AppPickerTopBar
import io.github.vinceglb.filekit.sample.shared.ui.components.AppScreenHeader
import io.github.vinceglb.filekit.sample.shared.ui.components.AppScreenHeaderButtonState
import io.github.vinceglb.filekit.sample.shared.ui.icons.LucideIcons
import io.github.vinceglb.filekit.sample.shared.ui.icons.MessageCircleCode
import io.github.vinceglb.filekit.sample.shared.ui.screens.directorypicker.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.sample.shared.ui.theme.AppMaxWidth
import io.github.vinceglb.filekit.sample.shared.ui.theme.AppTheme
import io.github.vinceglb.filekit.sample.shared.util.plus
import kotlinx.coroutines.launch

@Composable
internal fun DebugRoute(
    onNavigateBack: () -> Unit,
    onDisplayFileDetails: (file: PlatformFile) -> Unit,
) {
    DebugScreen(
        onNavigateBack = onNavigateBack,
        onDisplayFileDetails = onDisplayFileDetails,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DebugScreen(
    onNavigateBack: () -> Unit,
    onDisplayFileDetails: (file: PlatformFile) -> Unit,
) {
    var buttonState by remember { mutableStateOf(AppScreenHeaderButtonState.Enabled) }
    var files by remember { mutableStateOf(emptyList<PlatformFile>()) }
    var showPickerReproSheet by remember { mutableStateOf(false) }
    var launchImagePickerAfterSheetDismiss by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val pickerReproSheetState = rememberModalBottomSheetState()
    val picker = rememberFilePickerLauncher { file ->
        buttonState = AppScreenHeaderButtonState.Enabled
        files = file?.let(::listOf) ?: emptyList()

        scope.launch {
            file?.let { debugPlatformTest(it) }
        }
    }
    val imagePicker = rememberFilePickerLauncher(
        type = FileKitType.Image,
        mode = FileKitMode.Multiple(),
    ) { pickedFiles ->
        files = pickedFiles ?: emptyList()
    }

    val folderPicker = rememberDirectoryPickerLauncher(directory = null) { folder ->
        scope.launch {
            folder?.let {
                debugPlatformTest(folder)
                // bookmarkFolder(folder)
            }
        }
    }

    fun test() {
        scope.launch {
            loadBookmarkedFolder()
        }
    }

    LaunchedEffect(showPickerReproSheet, launchImagePickerAfterSheetDismiss) {
        if (!showPickerReproSheet && launchImagePickerAfterSheetDismiss) {
            launchImagePickerAfterSheetDismiss = false
            imagePicker.launch()
        }
    }

    Scaffold(
        topBar = {
            AppPickerTopBar(
                onNavigateBack = onNavigateBack,
                onOpenDocumentation = {},
            )
        },
    ) { contentPadding ->
        LazyColumn(
            contentPadding = contentPadding + PaddingValues(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            item {
                AppScreenHeader(
                    icon = LucideIcons.MessageCircleCode,
                    title = "Debug",
                    subtitle = "Debug page for testing FileKit features",
                    documentationUrl = "",
                    primaryButtonText = "Pick File",
                    primaryButtonState = buttonState,
                    onPrimaryButtonClick = {
                        buttonState = AppScreenHeaderButtonState.Loading
                        folderPicker.launch()
                    },
                    modifier = Modifier.sizeIn(maxWidth = AppMaxWidth),
                )
            }

            item {
                Button(
                    modifier = Modifier.sizeIn(maxWidth = AppMaxWidth).fillMaxWidth(),
                    onClick = { showPickerReproSheet = true },
                ) {
                    Text("Open iOS picker race repro")
                }
            }

            item {
                Button(
                    onClick = { test() },
                ) {
                    Text("Load Bookmarked Folder")
                }
            }

            item {
                AppPickerResultsCard(
                    files = files,
                    emptyText = "No files selected yet",
                    emptyIcon = LucideIcons.MessageCircleCode,
                    onFileClick = onDisplayFileDetails,
                    modifier = Modifier.sizeIn(maxWidth = AppMaxWidth),
                )
            }
        }
    }

    if (showPickerReproSheet) {
        ModalBottomSheet(
            sheetState = pickerReproSheetState,
            onDismissRequest = { showPickerReproSheet = false },
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp),
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        scope.launch {
                            launchImagePickerAfterSheetDismiss = true
                            pickerReproSheetState.hide()
                            showPickerReproSheet = false
                        }
                    },
                ) {
                    Text("Launch picker")
                }
            }
        }
    }
}

internal expect suspend fun debugPlatformTest(folder: PlatformFile)

internal expect suspend fun bookmarkFolder(folder: PlatformFile)

internal expect suspend fun loadBookmarkedFolder(): PlatformFile

@Preview
@Composable
private fun DebugScreenPreview() {
    AppTheme {
        DebugScreen(
            onNavigateBack = {},
            onDisplayFileDetails = {},
        )
    }
}
