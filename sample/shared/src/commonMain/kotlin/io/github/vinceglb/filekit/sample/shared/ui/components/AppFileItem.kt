package io.github.vinceglb.filekit.sample.shared.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.sample.shared.ui.icons.File
import io.github.vinceglb.filekit.sample.shared.ui.icons.Folder
import io.github.vinceglb.filekit.sample.shared.ui.icons.LucideIcons
import io.github.vinceglb.filekit.sample.shared.ui.theme.AppTheme
import io.github.vinceglb.filekit.sample.shared.util.createPlatformFileForPreviews
import io.github.vinceglb.filekit.sample.shared.util.isDirectory

@Composable
public fun AppFileItem(
    file: PlatformFile,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
) {
    AppFileItem(
        fileName = file.name,
        fileType = if (file.isDirectory()) FileType.Directory else FileType.File,
        isSelected = isSelected,
        onClick = onClick,
    )
}

@Composable
public fun AppFileItem(
    fileName: String,
    fileType: FileType,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    } else {
        Color.Transparent
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
        ) {
            Icon(
                imageVector = when (fileType) {
                    FileType.File -> LucideIcons.File
                    FileType.Directory -> LucideIcons.Folder
                },
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.outline,
            )

            Text(
                text = fileName,
                fontSize = 14.sp,
                softWrap = false,
                overflow = TextOverflow.MiddleEllipsis,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

public enum class FileType {
    File,
    Directory,
}

@Preview
@Composable
private fun AppFileItem1Preview() {
    AppTheme {
        Surface {
            AppFileItem(
                file = createPlatformFileForPreviews("platformFile.txt"),
            )
        }
    }
}

@Preview
@Composable
private fun AppFileItemPreview() {
    AppTheme {
        Surface {
            AppFileItem(
                fileName = "file.txt",
                fileType = FileType.File,
                modifier = Modifier,
            )
        }
    }
}
