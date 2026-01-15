package io.github.vinceglb.filekit.sample.shared.ui.screens.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.sample.shared.ui.components.AppPickerResultsCard
import io.github.vinceglb.filekit.sample.shared.ui.components.AppPickerTopBar
import io.github.vinceglb.filekit.sample.shared.ui.components.AppScreenHeader
import io.github.vinceglb.filekit.sample.shared.ui.components.AppScreenHeaderButtonState
import io.github.vinceglb.filekit.sample.shared.ui.icons.LucideIcons
import io.github.vinceglb.filekit.sample.shared.ui.icons.MessageCircleCode
import io.github.vinceglb.filekit.sample.shared.ui.theme.AppMaxWidth
import io.github.vinceglb.filekit.sample.shared.util.plus

@Composable
internal fun DebugRoute(
    onNavigateBack: () -> Unit,
    onDisplayFileDetails: (file: PlatformFile) -> Unit,
) {
    DebugScreen(
        onNavigateBack = onNavigateBack,
        onDisplayFileDetails = onDisplayFileDetails,
    )
}

@Composable
private fun DebugScreen(
    onNavigateBack: () -> Unit,
    onDisplayFileDetails: (file: PlatformFile) -> Unit,
) {
    var buttonState by remember { mutableStateOf(AppScreenHeaderButtonState.Enabled) }
    var files by remember { mutableStateOf(emptyList<PlatformFile>()) }

    // ========================================
    // CUSTOMIZE YOUR PICKER HERE
    // ========================================
    val picker = rememberFilePickerLauncher { file ->
        buttonState = AppScreenHeaderButtonState.Enabled
        files = file?.let(::listOf) ?: emptyList()
    }

    Scaffold(
        topBar = {
            AppPickerTopBar(
                onNavigateBack = onNavigateBack,
                onOpenDocumentation = {},
            )
        },
    ) { contentPadding ->
        LazyColumn(
            contentPadding = contentPadding + PaddingValues(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            item {
                AppScreenHeader(
                    icon = LucideIcons.MessageCircleCode,
                    title = "Debug",
                    subtitle = "Debug page for testing FileKit features",
                    documentationUrl = "",
                    primaryButtonText = "Pick File",
                    primaryButtonState = buttonState,
                    onPrimaryButtonClick = {
                        buttonState = AppScreenHeaderButtonState.Loading
                        picker.launch()
                    },
                    modifier = Modifier.sizeIn(maxWidth = AppMaxWidth),
                )
            }

            // ========================================
            // ADD YOUR CUSTOM DEBUG UI HERE
            // ========================================

            item {
                AppPickerResultsCard(
                    files = files,
                    emptyText = "No files selected yet",
                    emptyIcon = LucideIcons.MessageCircleCode,
                    onFileClick = onDisplayFileDetails,
                    modifier = Modifier.sizeIn(maxWidth = AppMaxWidth),
                )
            }
        }
    }
}
