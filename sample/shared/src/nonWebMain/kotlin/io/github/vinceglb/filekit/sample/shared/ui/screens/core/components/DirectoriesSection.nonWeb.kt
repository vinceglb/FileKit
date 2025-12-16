package io.github.vinceglb.filekit.sample.shared.ui.screens.core.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.databasesDir
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.path
import io.github.vinceglb.filekit.projectDir
import io.github.vinceglb.filekit.sample.shared.ui.components.FeatureCard

@Composable
internal actual fun DirectoriesSection(
    modifier: Modifier,
) {
    FeatureCard(title = "Directories", modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            val dirs = listOf(
                "filesDir" to FileKit.filesDir,
                "cacheDir" to FileKit.cacheDir,
                "databasesDir" to FileKit.databasesDir,
                "projectDir" to FileKit.projectDir,
            )
            dirs.forEach { (label, dir) ->
                Text("$label: ${dir.path}")
            }
        }
    }
}
