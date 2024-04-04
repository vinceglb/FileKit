package io.github.vinceglb.picker.core.platform

import io.github.vinceglb.picker.core.platform.mac.MacOSFilePicker
import io.github.vinceglb.picker.core.platform.util.Platform
import io.github.vinceglb.picker.core.platform.util.PlatformUtil
import io.github.vinceglb.picker.core.platform.windows.WindowsFilePicker
import java.io.File

internal interface PlatformFilePicker {
	fun pickFile(
		initialDirectory: String? = null,
		fileExtensions: List<String>? = null,
		title: String? = null,
	): File?

	fun pickFiles(
		initialDirectory: String? = null,
		fileExtensions: List<String>? = null,
		title: String? = null,
	): List<File>?

	fun pickDirectory(
		initialDirectory: String? = null,
		title: String? = null,
	): File?

	companion object {
		val current: PlatformFilePicker by lazy { createPlatformFilePicker() }

		private fun createPlatformFilePicker(): PlatformFilePicker {
			return when (PlatformUtil.current) {
				Platform.MacOS -> MacOSFilePicker()
				Platform.Windows -> WindowsFilePicker()
				Platform.Linux -> WindowsFilePicker()		// TODO: WindowsFilePicker is compatible with other platforms but we need to implement native Linux file picker
			}
		}
	}
}
