package io.github.vinceglb.filekit.sample.shared.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import filekit.sample.shared.generated.resources.Res
import filekit.sample.shared.generated.resources.camera_picker
import filekit.sample.shared.generated.resources.directory_picker
import filekit.sample.shared.generated.resources.file_picker
import filekit.sample.shared.generated.resources.file_saver
import filekit.sample.shared.generated.resources.gallery_picker
import filekit.sample.shared.generated.resources.share_file
import io.github.vinceglb.filekit.sample.shared.ui.components.AppDottedBorderCard
import io.github.vinceglb.filekit.sample.shared.ui.components.exp.AppEmpty
import io.github.vinceglb.filekit.sample.shared.ui.theme.AppMaxWidth
import io.github.vinceglb.filekit.sample.shared.ui.theme.AppTheme
import io.github.vinceglb.filekit.sample.shared.util.AppUrl
import io.github.vinceglb.filekit.sample.shared.util.openUrlInBrowser
import io.github.vinceglb.filekit.sample.shared.util.plus
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun HomeRoute(
    onFilePickerClick: () -> Unit,
    onGalleryPickerClick: () -> Unit,
    onDirectoryPickerClick: () -> Unit,
    onCameraPickerClick: () -> Unit,
    onFileSaverClick: () -> Unit,
    onShareFileClick: () -> Unit,
) {
    HomeScreen(
        onFilePickerClick = onFilePickerClick,
        onGalleryPickerClick = onGalleryPickerClick,
        onDirectoryPickerClick = onDirectoryPickerClick,
        onCameraPickerClick = onCameraPickerClick,
        onFileSaverClick = onFileSaverClick,
        onShareFileClick = onShareFileClick,
    )
}

@Composable
private fun HomeScreen(
    onFilePickerClick: () -> Unit,
    onGalleryPickerClick: () -> Unit,
    onDirectoryPickerClick: () -> Unit,
    onCameraPickerClick: () -> Unit,
    onFileSaverClick: () -> Unit,
    onShareFileClick: () -> Unit,
) {
    Scaffold { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
        ) {
            val horizontalPadding = maxOf(16.dp, (maxWidth - AppMaxWidth * 1.4f) / 2)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = paddingValues + PaddingValues(
                    horizontal = horizontalPadding,
                    vertical = 16.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                item(
                    span = { GridItemSpan(2) },
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        AppEmpty(
                            title = "FileKit",
                            subtitle = "FileKit is a lightweight yet powerful library that " +
                                "simplifies file operations across multiple platforms.",
                            primaryButtonText = "Quick Start",
                            secondaryButtonText = "Documentation",
                            tertiaryButtonText = "GitHub",
                            onPrimaryButtonClick = { AppUrl("https://filekit.mintlify.app/quickstart").openUrlInBrowser() },
                            onSecondaryButtonClick = { AppUrl("https://filekit.mintlify.app").openUrlInBrowser() },
                            onTertiaryButtonClick = { AppUrl("https://github.com/vinceglb/FileKit").openUrlInBrowser() },
                            modifier = Modifier.widthIn(max = AppMaxWidth),
                        )
                    }
                }

                item {
                    HomeEntry(
                        label = "File Picker",
                        image = Res.drawable.file_picker,
                        onClick = onFilePickerClick,
                    )
                }

                item {
                    HomeEntry(
                        label = "Gallery Picker",
                        image = Res.drawable.gallery_picker,
                        onClick = onGalleryPickerClick,
                    )
                }

                item {
                    HomeEntry(
                        label = "Directory Picker",
                        image = Res.drawable.directory_picker,
                        onClick = onDirectoryPickerClick,
                    )
                }

                item {
                    HomeEntry(
                        label = "Camera Picker",
                        image = Res.drawable.camera_picker,
                        onClick = onCameraPickerClick,
                    )
                }

                item {
                    HomeEntry(
                        label = "File Saver",
                        image = Res.drawable.file_saver,
                        onClick = onFileSaverClick,
                    )
                }

                item {
                    HomeEntry(
                        label = "Share File",
                        image = Res.drawable.share_file,
                        onClick = onShareFileClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeEntry(
    label: String,
    image: DrawableResource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        AppDottedBorderCard(
            contentPadding = PaddingValues(6.dp),
            modifier = Modifier
                .height(220.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onClick),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(image),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(10.dp)),
                )
            }
        }

        Text(
            text = label,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            letterSpacing = (-0.45).sp,
            lineHeight = 28.sp,
        )
    }
}

@Preview
@Composable
private fun HomeScreenPreview() {
    AppTheme {
        HomeScreen(
            onGalleryPickerClick = {},
            onFilePickerClick = {},
            onDirectoryPickerClick = {},
            onCameraPickerClick = {},
            onFileSaverClick = {},
            onShareFileClick = {},
        )
    }
}
