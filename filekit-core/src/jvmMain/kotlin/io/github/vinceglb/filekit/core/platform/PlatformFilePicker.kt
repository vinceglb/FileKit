package io.github.vinceglb.filekit.core.platform

import io.github.vinceglb.filekit.core.platform.awt.AwtFilePicker
import io.github.vinceglb.filekit.core.platform.mac.MacOSFilePicker
import io.github.vinceglb.filekit.core.platform.util.Platform
import io.github.vinceglb.filekit.core.platform.util.PlatformUtil
import io.github.vinceglb.filekit.core.platform.windows.WindowsFilePicker
import java.awt.Window
import java.io.File

internal interface PlatformFilePicker {
	suspend fun pickFile(
		initialDirectory: String?,
		fileExtensions: List<String>?,
		title: String?,
		parentWindow: Window?,
	): File?

	suspend fun pickFiles(
		initialDirectory: String?,
		fileExtensions: List<String>?,
		title: String?,
		parentWindow: Window?,
	): List<File>?

	fun pickDirectory(
		initialDirectory: String?,
		title: String?,
		parentWindow: Window?,
	): File?

	companion object {
		val current: PlatformFilePicker by lazy { createPlatformFilePicker() }

		private fun createPlatformFilePicker(): PlatformFilePicker {
			return when (PlatformUtil.current) {
				Platform.MacOS -> MacOSFilePicker()
				Platform.Windows -> WindowsFilePicker()
				Platform.Linux -> AwtFilePicker()
			}
		}
	}
}
