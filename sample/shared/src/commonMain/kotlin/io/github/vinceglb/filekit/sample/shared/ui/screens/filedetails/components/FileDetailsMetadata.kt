package io.github.vinceglb.filekit.sample.shared.ui.screens.filedetails.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.sample.shared.ui.components.AppTextButton

internal data class FileMetadataItem(
    val label: String,
    val value: String,
    val hidden: Boolean = false,
)

internal expect fun PlatformFile.toMetadataItems(): List<FileMetadataItem>

@Composable
internal fun FileMetadata(
    file: PlatformFile,
    modifier: Modifier = Modifier,
) {
    val metadataItems = remember(file) { file.toMetadataItems() }
    var showHidden by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "File Metadata",
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 12.dp),
            )

            AppTextButton(onClick = { showHidden = !showHidden }) {
                AnimatedContent(showHidden) { isShowing ->
                    Text(
                        text = when {
                            isShowing -> "Show less"
                            else -> "Show more"
                        },
                    )
                }
            }
        }

        metadataItems.forEach { item ->
            AnimatedVisibility(visible = !item.hidden || showHidden) {
                FileMetadataItemRow(item = item)
            }
        }
    }
}

@Composable
private fun FileMetadataItemRow(
    item: FileMetadataItem,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = {}),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Text(
                text = item.label,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 14.sp,
                letterSpacing = 0.25.sp,
            )
            SelectionContainer {
                Text(
                    text = item.value,
                    fontSize = 14.sp,
                    lineHeight = 14.sp,
                    softWrap = true,
                )
            }
        }
    }
}
