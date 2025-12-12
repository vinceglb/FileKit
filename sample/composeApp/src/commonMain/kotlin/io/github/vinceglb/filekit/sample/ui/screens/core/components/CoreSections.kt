package io.github.vinceglb.filekit.sample.ui.screens.core.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.vinceglb.filekit.PlatformFile

@Composable
expect fun DirectoriesSection(
    modifier: Modifier = Modifier,
)

@Composable
expect fun FileSystemOperationsSection(
    selectedFile: PlatformFile?,
    onFileUpdate: (PlatformFile?) -> Unit,
    modifier: Modifier = Modifier,
)

@Composable
expect fun ImageUtilsSection(
    selectedFile: PlatformFile?,
    modifier: Modifier = Modifier,
)

@Composable
expect fun WebDownloadSection(
    modifier: Modifier = Modifier,
)
