package io.github.vinceglb.sample.core

import com.rickclephas.kmm.viewmodel.KMMViewModel
import com.rickclephas.kmm.viewmodel.coroutineScope
import io.github.vinceglb.picker.core.Picker
import io.github.vinceglb.picker.core.PickerSelectionMode
import io.github.vinceglb.picker.core.PlatformDirectory
import io.github.vinceglb.picker.core.PlatformFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel : KMMViewModel() {
	private val _uiState: MutableStateFlow<MainUiState> = MutableStateFlow(MainUiState())
	val uiState: StateFlow<MainUiState> = _uiState

	fun pickImage() = executeWithLoading {
		// Single file mode
		val mode = PickerSelectionMode.SingleFile(extensions = listOf("jpg", "jpeg", "png"))

		// Pick a file
		val file = Picker.pick(
			mode = mode,
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
		// Multiple files mode
		val mode = PickerSelectionMode.MultipleFiles(extensions = listOf("jpg", "jpeg", "png"))

		// Pick files
		val files = Picker.pick(mode = mode)

		// Add files to the state
		if (files != null) {
			// Add files to the state
			val newFiles = _uiState.value.files + files
			_uiState.update { it.copy(files = newFiles) }
		}
	}

	fun pickDirectory() = executeWithLoading {
		// Directory mode
		val mode = PickerSelectionMode.Directory

		// Pick a directory
		val directory = Picker.pick(mode)

		// Update the state
		if (directory != null) {
			_uiState.update { it.copy(directory = directory) }
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
	constructor() : this(emptySet(), null, false)
}

expect fun downloadDirectoryPath(): String?
