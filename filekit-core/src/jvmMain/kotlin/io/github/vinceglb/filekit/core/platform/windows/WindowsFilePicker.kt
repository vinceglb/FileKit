package io.github.vinceglb.filekit.core.platform.windows

import io.github.vinceglb.filekit.core.platform.PlatformFilePicker
import io.github.vinceglb.filekit.core.platform.windows.api.WindowsFileChooser
import io.github.vinceglb.filekit.core.platform.windows.api.WindowsFolderBrowser
import java.awt.Window
import java.io.File

internal class WindowsFilePicker : PlatformFilePicker {
	override suspend fun pickFile(
		initialDirectory: String?,
		fileExtensions: List<String>?,
		title: String?,
		parentWindow: Window?,
	): File? {
		val fileChooser = setup(false, title, initialDirectory, fileExtensions)

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
		val fileChooser = setup(true, title, initialDirectory, fileExtensions)

		// Show file chooser
		fileChooser.showOpenDialog(parentWindow)

		// Return selected files
		return fileChooser.selectedFiles?.ifEmpty { null }
	}

	override suspend fun pickDirectory(
		initialDirectory: String?,
		title: String?,
		parentWindow: Window?,
	): File? {
		val fileChooser = WindowsFolderBrowser()

		// Set title
		title?.let { fileChooser.setTitle(it) }

		// Show file chooser
		return fileChooser.showDialog(parentWindow)
	}

	private fun setup(
		isMultipleSelection: Boolean,
		title: String?,
		initialDirectory: String?,
		fileExtensions: List<String>?,
	): WindowsFileChooser {
		val fileChooser = WindowsFileChooser(initialDirectory)

		// Only allow single selection
		fileChooser.setMultipleSelection(isMultipleSelection)

		// Set title
		title?.let { fileChooser.setTitle(it) }

		if (!fileExtensions.isNullOrEmpty()) {
			val filterName = fileExtensions.joinToString(", ", "Supported Files (", ")")
			fileChooser.addFilter(filterName, *fileExtensions.toTypedArray())
		}

		return fileChooser
	}
}
