package io.github.vinceglb.filekit.sample.shared.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.isDirectory
import io.github.vinceglb.filekit.list
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.sample.shared.ui.icons.ChevronDown
import io.github.vinceglb.filekit.sample.shared.ui.icons.File
import io.github.vinceglb.filekit.sample.shared.ui.icons.Folder
import io.github.vinceglb.filekit.sample.shared.ui.icons.LucideIcons
import io.github.vinceglb.filekit.sample.shared.ui.theme.AppMaxWidth
import io.github.vinceglb.filekit.sample.shared.ui.theme.AppTheme
import io.github.vinceglb.filekit.sample.shared.util.createPlatformFileForPreviews

/**
 * A tree view that displays the hierarchy of a directory.
 */
@Composable
internal fun DirectoryTreeView(
    file: PlatformFile,
    onFileClick: (PlatformFile) -> Unit,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = false,
) {
    FileNode(
        file = file,
        level = 0,
        onFileClick = onFileClick,
        initiallyExpanded = initiallyExpanded,
        modifier = modifier,
    )
}

@Composable
private fun FileNode(
    file: PlatformFile,
    level: Int,
    onFileClick: (PlatformFile) -> Unit,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = false,
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    val isDirectory = file.isDirectory()
    val children = remember(file) {
        if (isDirectory) {
            file.list()
        } else {
            emptyList()
        }
    }

    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onFileClick(file) }
                .padding(vertical = 4.dp)
                .padding(start = (level * 16).dp),
        ) {
            if (isDirectory) {
                Box(
                    modifier = Modifier
                        // .minimumInteractiveComponentSize()
                        .size(20.dp)
                        .clickable { expanded = !expanded },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = LucideIcons.ChevronDown,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(if (expanded) 0f else -90f),
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(20.dp))
            }

            Icon(
                imageVector = if (isDirectory) LucideIcons.Folder else LucideIcons.File,
                contentDescription = null,
                modifier = Modifier
                    .size(20.dp)
                    .padding(horizontal = 4.dp),
                tint = if (isDirectory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
            )

            Text(
                text = file.name,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 4.dp),
            )
        }

        if (isDirectory) {
            AnimatedVisibility(visible = expanded) {
                Column {
                    children.forEach { child ->
                        FileNode(
                            file = child,
                            level = level + 1,
                            onFileClick = onFileClick,
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun DirectoryTreeViewPreview() {
    AppTheme {
        Surface(
            modifier = Modifier.sizeIn(maxWidth = AppMaxWidth).background(MaterialTheme.colorScheme.background),
        ) {
            DirectoryTreeView(
                file = createPlatformFileForPreviews("."),
                onFileClick = {},
                initiallyExpanded = true,
            )
        }
    }
}
