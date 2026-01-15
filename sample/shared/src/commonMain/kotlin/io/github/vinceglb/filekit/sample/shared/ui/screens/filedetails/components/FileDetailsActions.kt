package io.github.vinceglb.filekit.sample.shared.ui.screens.filedetails.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.vinceglb.filekit.PlatformFile

@Composable
internal expect fun FileDetailsActions(
    file: PlatformFile,
    onDeleteFile: () -> Unit,
    modifier: Modifier = Modifier,
)
