package io.github.vinceglb.sample.explorer

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.shareFile
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.saveImageToGallery
import io.github.vinceglb.sample.explorer.icon.ExplorerIcons
import io.github.vinceglb.sample.explorer.icon.Images
import io.github.vinceglb.sample.explorer.icon.Share2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
actual fun PlatformActions(
    file: PlatformFile,
    scope: CoroutineScope,
) {
    FileAction(
        text = "Share file",
        icon = ExplorerIcons.Share2,
        onClick = {
            scope.launch {
                FileKit.shareFile(file)
            }
        },
    )

    if (listOf("jpg", "jpeg", "png", "gif").contains(file.extension)) {
        FileAction(
            text = "Save image to gallery",
            icon = ExplorerIcons.Images,
            onClick = {
                scope.launch {
                    FileKit.saveImageToGallery(file)
                }
            },
        )
    }
}
