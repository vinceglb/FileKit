package io.github.vinceglb.filekit.sample.shared.ui.screens.dialogs.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.vinceglb.filekit.PlatformFile

@Composable
internal expect fun DirectoryInputField(
    label: String,
    directory: PlatformFile?,
    onDirectoryChange: (PlatformFile?) -> Unit,
    modifier: Modifier = Modifier,
)
