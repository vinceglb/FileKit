package io.github.vinceglb.filekit.sample.shared.ui.screens.filepicker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.FileKitMacOSSettings
import io.github.vinceglb.filekit.sample.shared.ui.components.AppField
import io.github.vinceglb.filekit.sample.shared.ui.components.AppOutlinedTextField
import io.github.vinceglb.filekit.sample.shared.ui.theme.geistMonoFontFamily

/**
 * JVM implementation with title and macOS-specific settings.
 */
internal class JvmFilePickerDialogSettingsState : FilePickerDialogSettingsState {
    var title: String by mutableStateOf("")
    var canCreateDirectories: Boolean by mutableStateOf(true)
    var resolvesAliases: Boolean by mutableStateOf(true)

    override fun build(): FileKitDialogSettings = FileKitDialogSettings(
        title = title.ifBlank { null },
        parentWindow = null,
        macOS = FileKitMacOSSettings(
            resolvesAliases = resolvesAliases,
            canCreateDirectories = canCreateDirectories,
        ),
    )
}

@Composable
internal actual fun rememberFilePickerDialogSettingsState(): FilePickerDialogSettingsState = remember { JvmFilePickerDialogSettingsState() }

@Composable
internal actual fun FilePickerDialogSettingsContent(state: FilePickerDialogSettingsState) {
    val jvmState = state as JvmFilePickerDialogSettingsState

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AppField(label = "Dialog Title (optional)") {
            AppOutlinedTextField(
                value = jvmState.title,
                onValueChange = { jvmState.title = it },
                placeholder = {
                    Text(
                        text = "Select a file",
                        color = MaterialTheme.colorScheme.outline,
                        fontFamily = geistMonoFontFamily(),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                fontFamily = geistMonoFontFamily(),
            )
        }

        Text(
            text = "macOS Settings",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Can Create Directories",
                style = MaterialTheme.typography.bodyMedium,
            )
            Switch(
                checked = jvmState.canCreateDirectories,
                onCheckedChange = { jvmState.canCreateDirectories = it },
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Resolves Aliases",
                style = MaterialTheme.typography.bodyMedium,
            )
            Switch(
                checked = jvmState.resolvesAliases,
                onCheckedChange = { jvmState.resolvesAliases = it },
            )
        }
    }
}
