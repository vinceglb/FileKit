package io.github.vinceglb.sample.core

import com.rickclephas.kmp.observableviewmodel.MutableStateFlow
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.coroutineScope
import io.github.vinceglb.picker.core.Picker
import io.github.vinceglb.picker.core.PickerSelectionMode
import io.github.vinceglb.picker.core.PickerSelectionType
import io.github.vinceglb.picker.core.PlatformDirectory
import io.github.vinceglb.picker.core.PlatformFile
import io.github.vinceglb.picker.core.baseName
import io.github.vinceglb.picker.core.extension
import io.github.vinceglb.picker.core.pickFile
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(viewModelScope, MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState

    fun pickImage() = executeWithLoading {
        // Pick a file
        val file = Picker.pickFile(
            type = PickerSelectionType.Image,
            title = "Custom title here",
            initialDirectory = downloadDirectoryPath()
        )

        // Add file to the state
        if (file != null) {
            val newFiles = _uiState.value.files + file
            _uiState.update { it.copy(files = newFiles) }
        }
    }

    fun pickImages() = executeWithLoading {
        // Pick files
        val files = Picker.pickFile(
            type = PickerSelectionType.Image,
            mode = PickerSelectionMode.Multiple
        )

        // Add files to the state
        if (files != null) {
            // Add files to the state
            val newFiles = _uiState.value.files + files
            _uiState.update { it.copy(files = newFiles) }
        }
    }

    fun pickFile() = executeWithLoading {
        // Pick a file
        val file = Picker.pickFile(
            type = PickerSelectionType.File(extensions = listOf("png")),
        )

        // Add file to the state
        if (file != null) {
            val newFiles = _uiState.value.files + file
            _uiState.update { it.copy(files = newFiles) }
        }
    }

    fun pickFiles() = executeWithLoading {
        // Pick files
        val files = Picker.pickFile(
            type = PickerSelectionType.File(extensions = listOf("png")),
            mode = PickerSelectionMode.Multiple
        )

        // Add files to the state
        if (files != null) {
            val newFiles = _uiState.value.files + files
            _uiState.update { it.copy(files = newFiles) }
        }
    }

    fun pickDirectory() = executeWithLoading {
        // Pick a directory
        val directory = Picker.pickDirectory()

        // Update the state
        if (directory != null) {
            _uiState.update { it.copy(directory = directory) }
        }
    }

    fun saveFile(file: PlatformFile) = executeWithLoading {
        // Save a file
        val newFile = Picker.saveFile(
            bytes = file.readBytes(),
            baseName = file.baseName,
            extension = file.extension
        )

        // Add file to the state
        if (newFile != null) {
            val newFiles = _uiState.value.files + newFile
            _uiState.update { it.copy(files = newFiles) }
        }
    }

    private fun executeWithLoading(block: suspend () -> Unit) {
        viewModelScope.coroutineScope.launch {
            _uiState.update { it.copy(loading = true) }
            block()
            _uiState.update { it.copy(loading = false) }
        }
    }
}

data class MainUiState(
    val files: Set<PlatformFile> = emptySet(),    // Set instead of List to avoid duplicates
    val directory: PlatformDirectory? = null,
    val loading: Boolean = false
) {
    // Used by SwiftUI code
    constructor() : this(emptySet(), null, false)
}

expect fun downloadDirectoryPath(): String?
