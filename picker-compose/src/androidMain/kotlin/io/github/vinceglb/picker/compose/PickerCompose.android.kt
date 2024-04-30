package io.github.vinceglb.picker.compose

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.activity.result.contract.ActivityResultContracts.OpenDocumentTree
import androidx.activity.result.contract.ActivityResultContracts.OpenMultipleDocuments
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import io.github.vinceglb.picker.core.Picker
import io.github.vinceglb.picker.core.PickerSelectionMode
import io.github.vinceglb.picker.core.PickerSelectionMode.Directory
import io.github.vinceglb.picker.core.PickerSelectionMode.MultipleFiles
import io.github.vinceglb.picker.core.PickerSelectionMode.SingleFile
import io.github.vinceglb.picker.core.PlatformDirectory
import io.github.vinceglb.picker.core.PlatformFile

@Composable
public actual fun <Out> rememberPickerLauncher(
    mode: PickerSelectionMode<Out>,
    title: String?,
    initialDirectory: String?,
    onResult: (Out?) -> Unit,
): PickerResultLauncher {
    // Get context
    val context = LocalContext.current

    // Keep track of the current mode, initialDirectory and onResult listener
    val currentMode by rememberUpdatedState(mode)
    val currentInitialDirectory by rememberUpdatedState(initialDirectory)
    val currentOnResult by rememberUpdatedState(onResult)

    // Create Picker launcher based on mode
    val launcher = when (val currentModeValue = currentMode) {
        is SingleFile -> {
            // Create Android launcher
            @Suppress("UNCHECKED_CAST")
            val launcher = rememberLauncherForActivityResult(OpenDocument()) { uri ->
                val platformFile = uri?.let { PlatformFile(it, context) }
                currentOnResult(platformFile as Out?)
            }

            remember {
                // Get mime types
                val mimeTypes = Picker.getMimeTypes(currentModeValue.extensions)

                // Return Picker launcher
                PickerResultLauncher { launcher.launch(mimeTypes) }
            }
        }

        is MultipleFiles -> {
            // Create Android launcher
            @Suppress("UNCHECKED_CAST")
            val launcher = rememberLauncherForActivityResult(OpenMultipleDocuments()) { uris ->
                val platformFiles = uris
                    .takeIf { it.isNotEmpty() }
                    ?.map { uri -> PlatformFile(uri, context) }

                currentOnResult(platformFiles as Out?)
            }

            remember {
                // Get mime types
                val mimeTypes = Picker.getMimeTypes(currentModeValue.extensions)

                // Return Picker launcher
                PickerResultLauncher { launcher.launch(mimeTypes) }
            }
        }

        is Directory -> {
            // Create Android launcher
            @Suppress("UNCHECKED_CAST")
            val launcher = rememberLauncherForActivityResult(OpenDocumentTree()) { uri ->
                val platformDirectory = uri?.let { PlatformDirectory(it) }
                currentOnResult(platformDirectory as Out?)
            }

            remember {
                // Convert initialDirectory to Uri
                val initialPath = currentInitialDirectory?.let { Uri.parse(it) }

                // Return Picker launcher
                PickerResultLauncher { launcher.launch(initialPath) }
            }
        }

        else -> throw IllegalArgumentException("Unsupported mode: $currentModeValue")
    }

    return launcher
}

@Composable
public actual fun rememberSaverLauncher(
    fileExtension: String,
    onResult: (PlatformFile?) -> Unit
): SaverResultLauncher {
    // Get context
    val context = LocalContext.current

    // Keep track of the current onResult listener
    val currentOnResult by rememberUpdatedState(onResult)

    // Get MIME type
    val mimeType = remember(fileExtension) {
        Picker.getMimeType(fileExtension)
    }

    var callback: ((Uri?) -> PlatformFile?)? by remember {
        mutableStateOf(null)
    }

    // Create Android launcher
    val launcher = rememberLauncherForActivityResult(CreateDocument(mimeType)) { uri ->
        val platformFile = callback?.invoke(uri)
        callback = null
        currentOnResult(platformFile)
    }

    // Return Picker launcher
    return remember {
        SaverResultLauncher { bytes, fileName, _ ->
            callback = { uri ->
                uri?.let {
                    // Write the bytes to the file
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        output.write(bytes)
                    }

                    // Return the file
                    PlatformFile(uri, context)
                }
            }
            launcher.launch(fileName)
        }
    }
}
