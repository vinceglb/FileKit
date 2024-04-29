package io.github.vinceglb.picker.core.platform.windows

import io.github.vinceglb.picker.core.platform.PlatformFilePicker
import io.github.vinceglb.picker.core.platform.windows.api.JnaFileChooser
import java.io.File

internal class WindowsFilePicker : PlatformFilePicker {
	private val fileChooser = JnaFileChooser()

	override suspend fun pickFile(
		initialDirectory: String?,
		fileExtensions: List<String>?,
		title: String?
	): File? {
		// Setup file chooser
		fileChooser.apply {
			// Set mode
			mode = JnaFileChooser.Mode.Files

			// Only allow single selection
			isMultiSelectionEnabled = false

			// Set initial directory, title and file extensions
			setup(initialDirectory, fileExtensions, title)
		}

		// Show file chooser
		fileChooser.showOpenDialog(null)

		// Return selected file
		return fileChooser.selectedFile
	}

	override suspend fun pickFiles(
		initialDirectory: String?,
		fileExtensions: List<String>?,
		title: String?
	): List<File>? {
		// Setup file chooser
		fileChooser.apply {
			// Set mode
			mode = JnaFileChooser.Mode.Files

			// Allow multiple selection
			isMultiSelectionEnabled = true

			// Set initial directory, title and file extensions
			setup(initialDirectory, fileExtensions, title)
		}

		// Show file chooser
		fileChooser.showOpenDialog(null)

		// Return selected files
		return fileChooser.selectedFiles
			.mapNotNull { it }
			.ifEmpty { null }
	}

	override fun pickDirectory(initialDirectory: String?, title: String?): File? {
		// Setup file chooser
		fileChooser.apply {
			// Set mode
			mode = JnaFileChooser.Mode.Directories

			// Only allow single selection
			isMultiSelectionEnabled = false

			// Set initial directory and title
			setup(initialDirectory, null, title)
		}

		// Show file chooser
		fileChooser.showOpenDialog(null)

		// Return selected directory
		return fileChooser.selectedFile
	}

	private fun JnaFileChooser.setup(
		initialDirectory: String?,
		fileExtensions: List<String>?,
		title: String?
	) {
		// Set title
		title?.let(::setTitle)

		// Set initial directory
		initialDirectory?.let(::setCurrentDirectory)

		// Set file extension
		if (!fileExtensions.isNullOrEmpty()) {
			val filterName = fileExtensions.joinToString(", ", "Supported Files (", ")")
			addFilter(filterName, *fileExtensions.toTypedArray())
		}
	}
}
