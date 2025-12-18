package io.github.vinceglb.filekit.sample.shared.ui.screens.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.sample.shared.theme.AppTheme
import io.github.vinceglb.filekit.sample.shared.ui.components.AppButton
import io.github.vinceglb.filekit.sample.shared.ui.components.AppButtonDefaults
import io.github.vinceglb.filekit.sample.shared.ui.components.AppDottedBorderCard
import io.github.vinceglb.filekit.sample.shared.ui.components.AppFileItem
import io.github.vinceglb.filekit.sample.shared.ui.components.AppOutlinedButton
import io.github.vinceglb.filekit.sample.shared.ui.components.AppOutlinedTextField
import io.github.vinceglb.filekit.sample.shared.ui.components.AppTextButton
import io.github.vinceglb.filekit.sample.shared.ui.icons.ArrowUpRight
import io.github.vinceglb.filekit.sample.shared.ui.icons.Check
import io.github.vinceglb.filekit.sample.shared.ui.icons.ChevronDown
import io.github.vinceglb.filekit.sample.shared.ui.icons.File
import io.github.vinceglb.filekit.sample.shared.ui.icons.LucideIcons
import io.github.vinceglb.filekit.sample.shared.util.createPlatformFileForPreviews

@Composable
internal fun DialogsRoute(
    viewModel: DialogsViewModel = viewModel { DialogsViewModel() },
) {
    val platformFiles by viewModel.platformFiles.collectAsStateWithLifecycle()

    DialogsScreen(
        platformFiles = platformFiles,
        addPlatformFiles = viewModel::addPlatformFiles,
    )
}

@Composable
internal fun DialogsScreen(
    platformFiles: List<PlatformFile>,
    addPlatformFiles: (List<PlatformFile>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pickerLauncher = rememberFilePickerLauncher(
        type = FileKitType.File(),
        mode = FileKitMode.Multiple(),
    ) { files ->
        files?.let { addPlatformFiles(files) }
    }

    Scaffold(modifier = modifier) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 52.dp),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier
                                    .padding(bottom = 4.dp)
                                    .size(40.dp),
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        imageVector = LucideIcons.File,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(24.dp),
                                    )
                                }
                            }

                            Text(
                                text = "File picker",
                                fontWeight = FontWeight.Medium,
                                fontSize = 18.sp,
                                letterSpacing = (-0.45).sp,
                                lineHeight = 28.sp,
                            )

                            Text(
                                text = "You haven't created any projects yet. Get started by creating your first project.",
                                color = MaterialTheme.colorScheme.outline,
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                modifier = Modifier.widthIn(max = 280.dp),
                                lineHeight = 22.75.sp,
                                letterSpacing = 0.25.sp,
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            AppButton(
                                onClick = { pickerLauncher.launch() },
                                contentPadding = AppButtonDefaults.SmallButtonContentPadding,
                            ) {
                                Text("Open file picker")
                            }

                            AppOutlinedButton(
                                onClick = {},
                                contentPadding = AppButtonDefaults.SmallButtonContentPadding,
                            ) {
                                Text("Import project")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        AppTextButton(onClick = {}) {
                            Text("Learn more")
                            Spacer(modifier = Modifier.size(4.dp))
                            Icon(
                                imageVector = LucideIcons.ArrowUpRight,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }

            item {
                AppDottedBorderCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Vince(modifier = Modifier.weight(1f))
                            AppOutlinedButton(
                                onClick = { },
                            ) {
                                Text("Open file picker")
                            }
                        }
                    }
                }
            }

            if (platformFiles.isNotEmpty()) {
                item {
                    AppDottedBorderCard(
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            platformFiles.forEach { file ->
                                AppFileItem(file = file)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun Vince(modifier: Modifier = Modifier) {
    val options = listOf("Image", "Video", "Image and video", "File")
    var expanded by remember { mutableStateOf(false) }
    val textFieldState = rememberTextFieldState(options[0])
    var checkedIndex: Int by remember { mutableIntStateOf(0) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        AppOutlinedTextField(
            state = textFieldState,
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = LucideIcons.ChevronDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .rotate(if (expanded) 180f else 0f)
                        .size(20.dp),
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        ) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                        )
                    },
                    onClick = {
                        textFieldState.setTextAndPlaceCursorAtEnd(option)
                        checkedIndex = index
                        expanded = false
                    },
                    contentPadding = ButtonDefaults.SmallContentPadding,
                    modifier = Modifier.height(40.dp),
                    trailingIcon = if (index == checkedIndex) {
                        {
                            Icon(
                                imageVector = LucideIcons.Check,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    } else {
                        null
                    },
                )
            }
        }
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun DialogsScreenPreview() {
    AppTheme {
        DialogsScreen(
            platformFiles = listOf(
                createPlatformFileForPreviews("file.txt"),
                createPlatformFileForPreviews("file2.txt"),
            ),
            addPlatformFiles = {},
        )
    }
}
