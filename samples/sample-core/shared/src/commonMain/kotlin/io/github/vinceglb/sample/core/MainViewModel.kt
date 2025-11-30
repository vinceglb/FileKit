package io.github.vinceglb.sample.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitPickerState
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val dialogSettings: FileKitDialogSettings,
) : ViewModel() {
    // Used for SwiftUI code
    @Suppress("unused")
    constructor() : this(FileKitDialogSettings.createDefault())

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState

    fun pickImage() = executeWithLoading {
        // Pick a file
        val file = FileKit.openFilePicker(
            type = FileKitType.ImageAndVideo,
            title = "Custom title here",
            directory = downloadDirectoryPath(),
            dialogSettings = dialogSettings,
        )

        // Add file to the state
        if (file != null) {
            val newFiles = _uiState.value.files + file
            _uiState.update { it.copy(files = newFiles) }
        }
    }

    fun pickImages() = executeWithLoading {
        // Pick files
        val files = FileKit.openFilePicker(
            type = FileKitType.Image,
            mode = FileKitMode.Multiple(),
            dialogSettings = dialogSettings,
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
        val file = FileKit.openFilePicker(
            type = FileKitType.File(extensions = listOf("png", "jpg")),
            dialogSettings = dialogSettings,
        )

        // Add file to the state
        if (file != null) {
            val newFiles = _uiState.value.files + file
            _uiState.update { it.copy(files = newFiles) }
        }
    }

    fun pickFiles() = executeWithLoading {
        // Pick files
        val files = FileKit.openFilePicker(
            type = FileKitType.File(extensions = listOf("png", "jpg")),
            mode = FileKitMode.Multiple(),
            dialogSettings = dialogSettings,
        )

        // Add files to the state
        if (files != null) {
            val newFiles = _uiState.value.files + files
            _uiState.update { it.copy(files = newFiles) }
        }
    }

    fun pickFilesWithState() = executeWithLoading {
        // Pick files with state
        val files = FileKit.openFilePicker(
            type = FileKitType.Image,
            mode = FileKitMode.MultipleWithState(),
            dialogSettings = dialogSettings,
        )

        viewModelScope.launch {
            files.collect { result ->
                when (result) {
                    FileKitPickerState.Cancelled -> {
                        println("File picker cancelled")
                    }

                    is FileKitPickerState.Started -> {
                        println("Started picking ${result.total} files")
                    }

                    is FileKitPickerState.Progress -> {
                        println("New files processed: ${result.processed.size} / ${result.total}")
                        _uiState.update { it.copy(files = it.files + result.processed) }
                    }

                    is FileKitPickerState.Completed -> {
                        println("File picker completed with ${result.result.size} files")
                        _uiState.update { it.copy(files = it.files + result.result) }
                    }
                }
            }
        }
    }

    fun pickDirectory() = executeWithLoading {
        // Pick a directory
        val directory = pickDirectoryIfSupported(dialogSettings)

        // Update the state
        if (directory != null) {
            _uiState.update { it.copy(directory = directory) }
        }
    }

    fun saveFile(file: PlatformFile) = executeWithLoading {
        // Save a file
        val newFile = saveFileOrDownload(file, dialogSettings)

        // Add file to the state
        if (newFile != null) {
            val newFiles = _uiState.value.files + newFile
            _uiState.update { it.copy(files = newFiles) }
        }
    }

    fun takePhoto() = executeWithLoading {
        val image = takePhotoIfSupported()

        // Add image to the state
        if (image != null) {
            val newFiles = _uiState.value.files + image
            _uiState.update { it.copy(files = newFiles) }
        }
    }

    fun compressImageAndSaveToGallery(file: PlatformFile) = executeWithLoading {
        compressImage(file.readBytes())
    }

    fun shareFile(file: PlatformFile) = executeWithLoading {
        shareFileIfSupported(file)
    }

    private fun executeWithLoading(block: suspend () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }
            block()
            _uiState.update { it.copy(loading = false) }
        }
    }
}

data class MainUiState(
    val files: Set<PlatformFile> = emptySet(), // Set instead of List to avoid duplicates
    val directory: PlatformFile? = null,
    val loading: Boolean = false,
) {
    // Used by SwiftUI code
    constructor() : this(emptySet(), null, false)
}

expect fun downloadDirectoryPath(): PlatformFile?

expect suspend fun pickDirectoryIfSupported(
    dialogSettings: FileKitDialogSettings,
): PlatformFile?

expect suspend fun takePhotoIfSupported(): PlatformFile?

expect suspend fun compressImage(bytes: ByteArray)

expect suspend fun shareFileIfSupported(file: PlatformFile)

expect suspend fun saveFileOrDownload(
    file: PlatformFile,
    dialogSettings: FileKitDialogSettings,
): PlatformFile?
