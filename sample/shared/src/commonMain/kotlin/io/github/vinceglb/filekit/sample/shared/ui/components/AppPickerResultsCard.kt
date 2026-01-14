package io.github.vinceglb.filekit.sample.shared.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.PlatformFile

@Composable
internal fun AppPickerResultsCard(
    files: List<PlatformFile>,
    emptyText: String,
    emptyIcon: ImageVector,
    onFileClick: (PlatformFile) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppDottedBorderCard(
        contentPadding = PaddingValues(0.dp),
        modifier = modifier,
    ) {
        if (files.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(120.dp)
                    .fillMaxWidth(),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = emptyIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = emptyText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxWidth()) {
                files.forEach { file ->
                    AppFileItem(
                        file = file,
                        isSelected = false,
                        onClick = { onFileClick(file) },
                    )
                }
            }
        }
    }
}
