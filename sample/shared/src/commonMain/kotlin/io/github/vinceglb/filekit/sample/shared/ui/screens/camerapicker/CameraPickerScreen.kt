package io.github.vinceglb.filekit.sample.shared.ui.screens.camerapicker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import io.github.vinceglb.filekit.sample.shared.ui.components.AppDottedBorderCard
import io.github.vinceglb.filekit.sample.shared.ui.components.AppDropdown
import io.github.vinceglb.filekit.sample.shared.ui.components.AppDropdownItem
import io.github.vinceglb.filekit.sample.shared.ui.components.AppField
import io.github.vinceglb.filekit.sample.shared.ui.components.AppOutlinedTextField
import io.github.vinceglb.filekit.sample.shared.ui.components.AppPickerResultsCard
import io.github.vinceglb.filekit.sample.shared.ui.components.AppPickerSupportCard
import io.github.vinceglb.filekit.sample.shared.ui.components.AppPickerTopBar
import io.github.vinceglb.filekit.sample.shared.ui.components.AppScreenHeader
import io.github.vinceglb.filekit.sample.shared.ui.components.AppScreenHeaderButtonState
import io.github.vinceglb.filekit.sample.shared.ui.icons.Camera
import io.github.vinceglb.filekit.sample.shared.ui.icons.Folder
import io.github.vinceglb.filekit.sample.shared.ui.icons.LucideIcons
import io.github.vinceglb.filekit.sample.shared.ui.icons.ScanFace
import io.github.vinceglb.filekit.sample.shared.ui.theme.AppMaxWidth
import io.github.vinceglb.filekit.sample.shared.ui.theme.AppTheme
import io.github.vinceglb.filekit.sample.shared.util.AppUrl
import io.github.vinceglb.filekit.sample.shared.util.openUrlInBrowser
import io.github.vinceglb.filekit.sample.shared.util.plus

@Composable
internal fun CameraPickerRoute(
    onNavigateBack: () -> Unit,
    onDisplayFileDetails: (file: PlatformFile) -> Unit,
) {
    CameraPickerScreen(
        onNavigateBack = onNavigateBack,
        onDisplayFileDetails = onDisplayFileDetails,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CameraPickerScreen(
    onNavigateBack: () -> Unit,
    onDisplayFileDetails: (file: PlatformFile) -> Unit,
) {
    var buttonState by remember { mutableStateOf(AppScreenHeaderButtonState.Enabled) }
    var cameraFacing by remember { mutableStateOf(CameraFacingOption.System) }
    var capturedFiles by remember { mutableStateOf(emptyList<PlatformFile>()) }

    val cameraLauncher = rememberCameraPickerLauncher { file ->
        buttonState = AppScreenHeaderButtonState.Enabled
        if (file != null) {
            capturedFiles = listOf(file) + capturedFiles
        }
    }
    val isSupported = cameraLauncher.isSupported
    val primaryButtonText = if (isSupported) "Open Camera" else "Camera Unavailable"

    fun openCamera() {
        if (!isSupported) {
            return
        }
        buttonState = AppScreenHeaderButtonState.Loading
        cameraLauncher.launch(cameraFacing)
    }

    Scaffold(
        topBar = {
            AppPickerTopBar(
                onNavigateBack = onNavigateBack,
                onOpenDocumentation = { AppUrl("https://filekit.mintlify.app/dialogs/camera-picker").openUrlInBrowser() },
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
                    icon = LucideIcons.Camera,
                    title = "Camera Picker",
                    subtitle = "Capture a photo with the native camera on Android and iOS",
                    documentationUrl = "https://filekit.mintlify.app/dialogs/camera-picker",
                    primaryButtonText = primaryButtonText,
                    primaryButtonEnabled = isSupported,
                    primaryButtonState = buttonState,
                    onPrimaryButtonClick = ::openCamera,
                    modifier = Modifier.sizeIn(maxWidth = AppMaxWidth),
                )
            }

            item {
                CameraPickerSettingsCard(
                    cameraFacing = cameraFacing,
                    onCameraFacingChange = { cameraFacing = it },
                    modifier = Modifier.sizeIn(maxWidth = AppMaxWidth),
                )
            }

            if (!isSupported) {
                item {
                    AppPickerSupportCard(
                        text = "Camera picker is available on Android and iOS targets.",
                        icon = LucideIcons.ScanFace,
                        modifier = Modifier.sizeIn(maxWidth = AppMaxWidth),
                    )
                }
            }

            item {
                AppPickerResultsCard(
                    files = capturedFiles,
                    emptyText = "No photos captured yet",
                    emptyIcon = LucideIcons.Camera,
                    onFileClick = onDisplayFileDetails,
                    modifier = Modifier.sizeIn(maxWidth = AppMaxWidth),
                )
            }
        }
    }
}

@Composable
private fun CameraPickerSettingsCard(
    cameraFacing: CameraFacingOption,
    onCameraFacingChange: (CameraFacingOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppDottedBorderCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            AppField(label = "Capture") {
                AppOutlinedTextField(
                    value = "Photo",
                    onValueChange = {},
                    readOnly = true,
                    leadingIcon = {
                        Icon(
                            imageVector = LucideIcons.Camera,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            AppField(label = "Camera Facing") {
                AppDropdown(
                    value = cameraFacing,
                    onValueChange = onCameraFacingChange,
                    options = facingOptions,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            AppField(label = "Output") {
                AppOutlinedTextField(
                    value = "Cache directory (auto-cleaned)",
                    onValueChange = {},
                    readOnly = true,
                    leadingIcon = {
                        Icon(
                            imageVector = LucideIcons.Folder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Text(
                text = "Tip: captured photos are cached. Open details to save them permanently.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

private val facingOptions: List<AppDropdownItem.IconItem<CameraFacingOption>> = listOf(
    AppDropdownItem.IconItem(
        label = "System Default",
        value = CameraFacingOption.System,
        icon = LucideIcons.Camera,
    ),
    AppDropdownItem.IconItem(
        label = "Rear Camera",
        value = CameraFacingOption.Back,
        icon = LucideIcons.Camera,
    ),
    AppDropdownItem.IconItem(
        label = "Front Camera",
        value = CameraFacingOption.Front,
        icon = LucideIcons.ScanFace,
    ),
)

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun CameraPickerScreenPreview() {
    AppTheme {
        CameraPickerScreen(
            onNavigateBack = {},
            onDisplayFileDetails = {},
        )
    }
}
