package io.github.vinceglb.filekit.core.platform.windows

import io.github.vinceglb.filekit.core.platform.PlatformFilePicker
import io.github.vinceglb.filekit.core.platform.windows.api.JnaFileChooser
import java.awt.Window
import java.io.File

internal class WindowsFilePicker : PlatformFilePicker {
	override suspend fun pickFile(
		initialDirectory: String?,
		fileExtensions: List<String>?,
		title: String?,
		parentWindow: Window?,
	): File? {
		val fileChooser = JnaFileChooser()

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
		fileChooser.showOpenDialog(parentWindow)

		// Return selected file
		return fileChooser.selectedFile
	}

	override suspend fun pickFiles(
		initialDirectory: String?,
		fileExtensions: List<String>?,
		title: String?,
		parentWindow: Window?,
	): List<File>? {
		val fileChooser = JnaFileChooser()

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
		fileChooser.showOpenDialog(parentWindow)

		// Return selected files
		return fileChooser.selectedFiles
			.mapNotNull { it }
			.ifEmpty { null }
	}

	override suspend fun pickDirectory(
		initialDirectory: String?,
		title: String?,
		parentWindow: Window?,
	): File? {
		val fileChooser = JnaFileChooser()

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
		fileChooser.showOpenDialog(parentWindow)

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
