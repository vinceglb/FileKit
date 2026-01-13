package io.github.vinceglb.filekit.sample.shared.ui.screens.gallerypicker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitPickerState
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.sample.shared.ui.components.AppDottedBorderCard
import io.github.vinceglb.filekit.sample.shared.ui.components.AppDropdown
import io.github.vinceglb.filekit.sample.shared.ui.components.AppDropdownItem
import io.github.vinceglb.filekit.sample.shared.ui.components.AppField
import io.github.vinceglb.filekit.sample.shared.ui.components.AppFileItem
import io.github.vinceglb.filekit.sample.shared.ui.components.AppOutlinedTextField
import io.github.vinceglb.filekit.sample.shared.ui.components.AppScreenHeader
import io.github.vinceglb.filekit.sample.shared.ui.components.AppScreenHeaderButtonState
import io.github.vinceglb.filekit.sample.shared.ui.icons.BookImage
import io.github.vinceglb.filekit.sample.shared.ui.icons.BookOpenText
import io.github.vinceglb.filekit.sample.shared.ui.icons.Camera
import io.github.vinceglb.filekit.sample.shared.ui.icons.Check
import io.github.vinceglb.filekit.sample.shared.ui.icons.CheckCheck
import io.github.vinceglb.filekit.sample.shared.ui.icons.ChevronLeft
import io.github.vinceglb.filekit.sample.shared.ui.icons.Film
import io.github.vinceglb.filekit.sample.shared.ui.icons.Images
import io.github.vinceglb.filekit.sample.shared.ui.icons.LucideIcons
import io.github.vinceglb.filekit.sample.shared.ui.screens.gallerypicker.components.GalleryPickerDirectory
import io.github.vinceglb.filekit.sample.shared.ui.theme.AppTheme
import io.github.vinceglb.filekit.sample.shared.ui.theme.geistMonoFontFamily
import io.github.vinceglb.filekit.sample.shared.util.AppUrl
import io.github.vinceglb.filekit.sample.shared.util.openUrlInBrowser
import io.github.vinceglb.filekit.sample.shared.util.plus

@Composable
internal fun GalleryPickerRoute(
    onNavigateBack: () -> Unit,
    onDisplayFileDetails: (file: PlatformFile) -> Unit,
) {
    GalleryPickerScreen(
        onNavigateBack = onNavigateBack,
        onDisplayFileDetails = onDisplayFileDetails,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GalleryPickerScreen(
    onNavigateBack: () -> Unit,
    onDisplayFileDetails: (file: PlatformFile) -> Unit,
) {
    var buttonState by remember { mutableStateOf(AppScreenHeaderButtonState.Enabled) }
    var pickerType: FileKitType by remember { mutableStateOf(FileKitType.ImageAndVideo) }
    var pickerMode: Modes by remember { mutableStateOf(Modes.Multiple) }
    var pickerMaxItems: Int? by remember { mutableStateOf(null) }
    var pickerDirectory: PlatformFile? by remember { mutableStateOf(null) }

    var files: List<PlatformFile> by remember { mutableStateOf(emptyList()) }

    val gallerySinglePicker = rememberFilePickerLauncher(
        type = pickerType,
        mode = FileKitMode.Single,
        directory = pickerDirectory,
    ) { selectedFile ->
        buttonState = AppScreenHeaderButtonState.Enabled
        files = selectedFile?.let(::listOf) ?: emptyList()
    }

    val galleryMultiplePicker = rememberFilePickerLauncher(
        type = pickerType,
        mode = FileKitMode.Multiple(maxItems = pickerMaxItems),
        directory = pickerDirectory,
    ) { selectedFiles ->
        buttonState = AppScreenHeaderButtonState.Enabled
        files = selectedFiles ?: emptyList()
    }

    val gallerySingleWithStatePicker = rememberFilePickerLauncher(
        type = pickerType,
        mode = FileKitMode.SingleWithState,
        directory = pickerDirectory,
    ) { state ->
        buttonState = AppScreenHeaderButtonState.Enabled
        files = when (state) {
            FileKitPickerState.Cancelled -> emptyList()
            is FileKitPickerState.Completed<PlatformFile> -> listOf(state.result)
            is FileKitPickerState.Progress<PlatformFile> -> listOf(state.processed)
            is FileKitPickerState.Started -> emptyList()
        }
    }

    val galleryMultipleWithStatePicker = rememberFilePickerLauncher(
        type = pickerType,
        mode = FileKitMode.MultipleWithState(maxItems = pickerMaxItems),
        directory = pickerDirectory,
    ) { state ->
        buttonState = AppScreenHeaderButtonState.Enabled
        files = when (state) {
            FileKitPickerState.Cancelled -> emptyList()
            is FileKitPickerState.Completed<List<PlatformFile>> -> state.result
            is FileKitPickerState.Progress<List<PlatformFile>> -> state.processed
            is FileKitPickerState.Started -> emptyList()
        }
    }

    fun openGalleryPicker() {
        buttonState = AppScreenHeaderButtonState.Loading
        when (pickerMode) {
            Modes.Single -> gallerySinglePicker.launch()
            Modes.Multiple -> galleryMultiplePicker.launch()
            Modes.SingleWithState -> gallerySingleWithStatePicker.launch()
            Modes.MultipleWithState -> galleryMultipleWithStatePicker.launch()
        }
    }

    Scaffold(
        topBar = {
            GalleryPickerTopBar(
                onNavigateBack = onNavigateBack,
                onOpenDocumentation = { AppUrl("https://filekit.mintlify.app/dialogs/gallery-picker").openUrlInBrowser() },
                modifier = Modifier.padding(8.dp),
            )
        },
    ) { contentPadding ->
        LazyColumn(
            contentPadding = contentPadding + PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            item {
                AppScreenHeader(
                    icon = LucideIcons.BookImage,
                    title = "Gallery Picker",
                    subtitle = "Open the native photos and videos picker on Android and iOS",
                    documentationUrl = "https://filekit.mintlify.app/dialogs/gallery-picker",
                    primaryButtonState = buttonState,
                    onPrimaryButtonClick = ::openGalleryPicker,
                )
            }

            item {
                AppDottedBorderCard {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        AppField(label = "Type") {
                            AppDropdown(
                                value = pickerType,
                                onValueChange = { pickerType = it },
                                options = typeOptions,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            AppField(
                                label = "Mode",
                                modifier = Modifier.weight(1f),
                            ) {
                                AppDropdown(
                                    value = pickerMode,
                                    onValueChange = { pickerMode = it },
                                    options = modeOptions,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }

                            val isMaxItemsEnabled = pickerMode == Modes.Multiple || pickerMode == Modes.MultipleWithState
                            AnimatedVisibility(visible = isMaxItemsEnabled) {
                                AppField(
                                    label = "Max Items",
                                    modifier = Modifier.weight(1f),
                                ) {
                                    AppOutlinedTextField(
                                        value = pickerMaxItems?.toString() ?: "",
                                        onValueChange = { newValue ->
                                            val number = newValue.toIntOrNull()
                                            pickerMaxItems = when {
                                                number != null && number > 50 -> 50
                                                else -> number
                                            }
                                        },
                                        placeholder = {
                                            Text(
                                                text = "-",
                                                color = MaterialTheme.colorScheme.outline,
                                                fontFamily = geistMonoFontFamily(),
                                            )
                                        },
                                        modifier = Modifier.width(100.dp),
                                        fontFamily = geistMonoFontFamily(),
                                    )
                                }
                            }
                        }

                        GalleryPickerDirectory(
                            directory = pickerDirectory,
                            onPickDirectory = { pickerDirectory = it },
                        )
                    }
                }
            }

            item {
                AppDottedBorderCard(
                    contentPadding = PaddingValues(0.dp),
                ) {
                    if (files.isEmpty()) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .height(120.dp)
                                .fillMaxWidth(),
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = LucideIcons.Camera,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(20.dp),
                                )
                                Text(
                                    text = "No files selected",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline,
                                )
                            }
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            files.forEach { file ->
                                AppFileItem(
                                    file = file,
                                    isSelected = false,
                                    onClick = { onDisplayFileDetails(file) },
                                )
                            }
                        }
                    }
                }
            }
        }

//        selectedFile?.let { file ->
//            ModalBottomSheet(
//                onDismissRequest = { selectedFile = null },
//            ) {
//                FileDetailsContent(
//                    file = file,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 16.dp, vertical = 12.dp),
//                )
//            }
//        }
    }
}

@Composable
private fun GalleryPickerTopBar(
    onNavigateBack: () -> Unit,
    onOpenDocumentation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth(),
    ) {
        GalleryPickerTopBarButton(
            icon = LucideIcons.ChevronLeft,
            onClick = onNavigateBack,
        )
        GalleryPickerTopBarButton(
            icon = LucideIcons.BookOpenText,
            onClick = onOpenDocumentation,
        )
    }
}

@Composable
private fun GalleryPickerTopBarButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppDottedBorderCard(
        contentPadding = PaddingValues(all = 0.dp),
        modifier = modifier
            .systemBarsPadding()
            .size(48.dp)
            .background(MaterialTheme.colorScheme.background)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.Center),
            )
        }
    }
}

private val typeOptions: List<AppDropdownItem.IconItem<FileKitType>> = listOf(
    AppDropdownItem.IconItem(
        label = "Image",
        value = FileKitType.Image,
        icon = LucideIcons.Images,
    ),
    AppDropdownItem.IconItem(
        label = "Video",
        value = FileKitType.Video,
        icon = LucideIcons.Film,
    ),
    AppDropdownItem.IconItem(
        label = "Image & Video",
        value = FileKitType.ImageAndVideo,
        icon = LucideIcons.Camera,
    ),
)

private enum class Modes {
    Single,
    Multiple,
    SingleWithState,
    MultipleWithState,
}

private val modeOptions: List<AppDropdownItem.IconItem<Modes>> = listOf(
    AppDropdownItem.IconItem(
        label = "Single",
        value = Modes.Single,
        icon = LucideIcons.Check,
    ),
    AppDropdownItem.IconItem(
        label = "Single with state",
        value = Modes.SingleWithState,
        icon = LucideIcons.Check,
    ),
    AppDropdownItem.IconItem(
        label = "Multiple",
        value = Modes.Multiple,
        icon = LucideIcons.CheckCheck,
    ),
    AppDropdownItem.IconItem(
        label = "Multiple with state",
        value = Modes.MultipleWithState,
        icon = LucideIcons.CheckCheck,
    ),
)

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun GalleryPickerScreenPreview() {
    AppTheme {
        GalleryPickerScreen(
            onNavigateBack = {},
            onDisplayFileDetails = {},
        )
    }
}
