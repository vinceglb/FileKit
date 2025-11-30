package io.github.vinceglb.sample.explorer

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.isDirectory
import io.github.vinceglb.sample.explorer.icon.ExplorerIcons
import io.github.vinceglb.sample.explorer.icon.File
import io.github.vinceglb.sample.explorer.icon.FileArchive
import io.github.vinceglb.sample.explorer.icon.FileAudio
import io.github.vinceglb.sample.explorer.icon.FileCode
import io.github.vinceglb.sample.explorer.icon.FileImage
import io.github.vinceglb.sample.explorer.icon.FileJson
import io.github.vinceglb.sample.explorer.icon.FileText
import io.github.vinceglb.sample.explorer.icon.FileVideo
import io.github.vinceglb.sample.explorer.icon.Folder

@Composable
fun FileIcon(
    file: PlatformFile,
    modifier: Modifier = Modifier,
) {
    val imageVector = if (file.isDirectory()) {
        ExplorerIcons.Folder
    } else {
        when (file.extension) {
            "jpg", "jpeg", "png", "gif" -> ExplorerIcons.FileImage
            "txt", "md", "pdf", "doc", "docx" -> ExplorerIcons.FileText
            "mp4", "avi", "mkv" -> ExplorerIcons.FileVideo
            "mp3", "wav", "flac" -> ExplorerIcons.FileAudio
            "zip", "tar", "gz" -> ExplorerIcons.FileArchive
            "java", "kt", "js", "ts", "py", "html", "css" -> ExplorerIcons.FileCode
            "json", "xml", "yaml", "yml" -> ExplorerIcons.FileJson
            else -> ExplorerIcons.File
        }
    }

    Icon(
        imageVector = imageVector,
        contentDescription = null,
        modifier = modifier.size(20.dp),
    )
}
