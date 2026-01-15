package io.github.vinceglb.filekit.sample.shared.ui.screens.filedetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.coil.securelyAccessFile
import io.github.vinceglb.filekit.mimeType
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.sample.shared.ui.components.AppDottedBorderCard
import io.github.vinceglb.filekit.sample.shared.ui.icons.File
import io.github.vinceglb.filekit.sample.shared.ui.icons.Folder
import io.github.vinceglb.filekit.sample.shared.ui.icons.LucideIcons
import io.github.vinceglb.filekit.sample.shared.ui.screens.filedetails.components.FileDetailsActions
import io.github.vinceglb.filekit.sample.shared.ui.screens.filedetails.components.FileMetadata
import io.github.vinceglb.filekit.sample.shared.ui.theme.AppTheme
import io.github.vinceglb.filekit.sample.shared.util.createPlatformFileForPreviews
import io.github.vinceglb.filekit.sample.shared.util.formatBytes
import io.github.vinceglb.filekit.sample.shared.util.isDirectory
import io.github.vinceglb.filekit.sample.shared.util.isImageFile
import io.github.vinceglb.filekit.size

@Composable
internal fun FileDetailsRoute(
    file: PlatformFile,
    onDeleteFile: () -> Unit,
) {
    FileDetailsScreen(
        file = file,
        onDeleteFile = onDeleteFile,
    )
}

@Composable
private fun FileDetailsScreen(
    file: PlatformFile,
    onDeleteFile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            Header(
                file = file,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        if (file.isImageFile()) {
            item {
                ImageContent(
                    file = file,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
            }
        }

        item {
            FileMetadata(
                file = file,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
        }

        item {
            FileDetailsActions(
                file = file,
                onDeleteFile = onDeleteFile,
            )
        }
    }
}

@Composable
private fun Header(
    file: PlatformFile,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.size(44.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize(),
            ) {
                Icon(
                    imageVector = if (file.isDirectory()) LucideIcons.Folder else LucideIcons.File,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp),
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = file.name,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = when (file.isDirectory()) {
                    true -> "Folder"
                    false -> "${file.mimeType()?.toString() ?: "Unknown type"} - ${file.size().formatBytes()}"
                },
                color = MaterialTheme.colorScheme.outline,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ImageContent(
    file: PlatformFile,
    modifier: Modifier = Modifier,
) {
    AppDottedBorderCard(
        contentPadding = PaddingValues(8.dp),
        modifier = modifier,
    ) {
        AsyncImage(
            model = file,
            contentDescription = "Image preview",
            contentScale = ContentScale.Inside,
            onState = { state -> state.securelyAccessFile(file) },
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .heightIn(max = 300.dp),
        )
    }
}

@Preview
@Composable
private fun FileDetailsScreenPreview() {
    AppTheme {
        FileDetailsScreen(
            file = createPlatformFileForPreviews("SampleFile.txt"),
            onDeleteFile = {},
        )
    }
}
