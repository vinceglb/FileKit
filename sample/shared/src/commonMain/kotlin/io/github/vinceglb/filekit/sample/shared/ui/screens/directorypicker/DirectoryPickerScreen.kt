package io.github.vinceglb.filekit.sample.shared.ui.screens.directorypicker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.sample.shared.ui.components.AppDottedBorderCard
import io.github.vinceglb.filekit.sample.shared.ui.components.AppField
import io.github.vinceglb.filekit.sample.shared.ui.components.AppOutlinedButton
import io.github.vinceglb.filekit.sample.shared.ui.components.AppPickerResultsCard
import io.github.vinceglb.filekit.sample.shared.ui.components.AppPickerSupportCard
import io.github.vinceglb.filekit.sample.shared.ui.components.AppPickerTopBar
import io.github.vinceglb.filekit.sample.shared.ui.components.AppScreenHeader
import io.github.vinceglb.filekit.sample.shared.ui.components.AppScreenHeaderButtonState
import io.github.vinceglb.filekit.sample.shared.ui.icons.Folder
import io.github.vinceglb.filekit.sample.shared.ui.icons.Home
import io.github.vinceglb.filekit.sample.shared.ui.icons.LucideIcons
import io.github.vinceglb.filekit.sample.shared.ui.icons.X
import io.github.vinceglb.filekit.sample.shared.ui.theme.AppMaxWidth
import io.github.vinceglb.filekit.sample.shared.ui.theme.AppTheme
import io.github.vinceglb.filekit.sample.shared.util.AppUrl
import io.github.vinceglb.filekit.sample.shared.util.openUrlInBrowser
import io.github.vinceglb.filekit.sample.shared.util.plus

@Composable
internal fun DirectoryPickerRoute(
    onNavigateBack: () -> Unit,
    onDisplayFileDetails: (file: PlatformFile) -> Unit,
) {
    DirectoryPickerScreen(
        onNavigateBack = onNavigateBack,
        onDisplayFileDetails = onDisplayFileDetails,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DirectoryPickerScreen(
    onNavigateBack: () -> Unit,
    onDisplayFileDetails: (file: PlatformFile) -> Unit,
) {
    var buttonState by remember { mutableStateOf(AppScreenHeaderButtonState.Enabled) }
    var startDirectory by remember { mutableStateOf<PlatformFile?>(null) }
    var pickedDirectories by remember { mutableStateOf(emptyList<PlatformFile>()) }

    val directoryLauncher = rememberDirectoryPickerLauncher(
        directory = startDirectory,
    ) { directory ->
        buttonState = AppScreenHeaderButtonState.Enabled
        if (directory != null) {
            pickedDirectories = listOf(directory) + pickedDirectories
        }
    }
    val startDirectoryLauncher = rememberDirectoryPickerLauncher(
        directory = startDirectory,
    ) { directory ->
        if (directory != null) {
            startDirectory = directory
        }
    }
    val isSupported = directoryLauncher.isSupported
    val primaryButtonText = if (isSupported) "Pick Directory" else "Directory Unavailable"

    fun openDirectoryPicker() {
        if (!isSupported) {
            return
        }
        buttonState = AppScreenHeaderButtonState.Loading
        directoryLauncher.launch()
    }

    Scaffold(
        topBar = {
            AppPickerTopBar(
                onNavigateBack = onNavigateBack,
                onOpenDocumentation = { AppUrl("https://filekit.mintlify.app/dialogs/directory-picker").openUrlInBrowser() },
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
                    icon = LucideIcons.Folder,
                    title = "Directory Picker",
                    subtitle = "Select folders with the native picker on desktop and mobile",
                    documentationUrl = "https://filekit.mintlify.app/dialogs/directory-picker",
                    primaryButtonText = primaryButtonText,
                    primaryButtonEnabled = isSupported,
                    primaryButtonState = buttonState,
                    onPrimaryButtonClick = ::openDirectoryPicker,
                    modifier = Modifier.sizeIn(maxWidth = AppMaxWidth),
                )
            }

            item {
                DirectoryPickerSettingsCard(
                    startDirectoryName = startDirectory?.name,
                    isSupported = isSupported,
                    onPickStartDirectory = startDirectoryLauncher::launch,
                    onClearStartDirectory = { startDirectory = null },
                    modifier = Modifier.sizeIn(maxWidth = AppMaxWidth),
                )
            }

            if (!isSupported) {
                item {
                    AppPickerSupportCard(
                        text = "Directory picker is available on Android, iOS, and desktop targets.",
                        icon = LucideIcons.Folder,
                        modifier = Modifier.sizeIn(maxWidth = AppMaxWidth),
                    )
                }
            }

            item {
                AppPickerResultsCard(
                    files = pickedDirectories,
                    emptyText = "No directory selected yet",
                    emptyIcon = LucideIcons.Folder,
                    onFileClick = onDisplayFileDetails,
                    modifier = Modifier.sizeIn(maxWidth = AppMaxWidth),
                )
            }
        }
    }
}

@Composable
private fun DirectoryPickerSettingsCard(
    startDirectoryName: String?,
    isSupported: Boolean,
    onPickStartDirectory: () -> Unit,
    onClearStartDirectory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppDottedBorderCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            AppField(
                label = "Start In",
            ) {
                AppOutlinedButton(
                    onClick = onPickStartDirectory,
                    enabled = isSupported,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    contentPadding = PaddingValues(start = 16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.Start),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = LucideIcons.Home,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(startDirectoryName ?: "System default")
                        Spacer(modifier = Modifier.weight(1f))
                        AnimatedVisibility(visible = startDirectoryName != null) {
                            IconButton(onClick = onClearStartDirectory) {
                                Icon(
                                    imageVector = LucideIcons.X,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                }
            }

            Text(
                text = "Tip: Start In hints the initial folder, but some platforms remember the last location.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun DirectoryPickerScreenPreview() {
    AppTheme {
        DirectoryPickerScreen(
            onNavigateBack = {},
            onDisplayFileDetails = {},
        )
    }
}
