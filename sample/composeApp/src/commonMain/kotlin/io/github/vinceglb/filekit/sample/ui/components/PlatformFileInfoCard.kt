package io.github.vinceglb.filekit.sample.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.PlatformFile

data class InfoRow(
    val label: String,
    val value: String,
)

expect suspend fun getPlatformFileInfo(file: PlatformFile): List<InfoRow>

@Composable
fun PlatformFileInfoCard(
    file: PlatformFile,
    modifier: Modifier = Modifier,
) {
    var info by remember(file) { mutableStateOf<List<InfoRow>>(emptyList()) }

    LaunchedEffect(file) {
        info = getPlatformFileInfo(file)
    }

    FeatureCard(
        title = "PlatformFile",
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            info.forEach { row ->
                Text(
                    text = "${row.label}: ${row.value}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
