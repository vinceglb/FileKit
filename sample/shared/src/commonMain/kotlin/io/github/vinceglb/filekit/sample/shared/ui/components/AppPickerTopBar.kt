package io.github.vinceglb.filekit.sample.shared.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.sample.shared.ui.icons.BookOpenText
import io.github.vinceglb.filekit.sample.shared.ui.icons.ChevronLeft
import io.github.vinceglb.filekit.sample.shared.ui.icons.LucideIcons

@Composable
internal fun AppPickerTopBar(
    onNavigateBack: () -> Unit,
    onOpenDocumentation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 8.dp),
        ) {
            AppPickerTopBarButton(
                icon = LucideIcons.ChevronLeft,
                onClick = onNavigateBack,
            )
            AppPickerTopBarButton(
                icon = LucideIcons.BookOpenText,
                onClick = onOpenDocumentation,
            )
        }
    }
}

@Composable
private fun AppPickerTopBarButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppDottedBorderCard(
        contentPadding = PaddingValues(all = 0.dp),
        modifier = modifier
            .systemBarsPadding()
            .size(48.dp)
            .background(MaterialTheme.colorScheme.background)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.Center),
            )
        }
    }
}
