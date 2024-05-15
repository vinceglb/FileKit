package io.github.vinceglb.picker.core.platform

import io.github.vinceglb.picker.core.platform.awt.AwtFilePicker
import io.github.vinceglb.picker.core.platform.mac.MacOSFilePicker
import io.github.vinceglb.picker.core.platform.util.Platform
import io.github.vinceglb.picker.core.platform.util.PlatformUtil
import io.github.vinceglb.picker.core.platform.windows.WindowsFilePicker
import java.awt.Frame
import java.awt.Window
import java.io.File

internal interface PlatformFilePicker {
	suspend fun pickFile(
		initialDirectory: String?,
		fileExtensions: List<String>?,
		title: String?,
		parentWindow: Frame?,
	): File?

	suspend fun pickFiles(
		initialDirectory: String?,
		fileExtensions: List<String>?,
		title: String?,
		parentWindow: Frame?,
	): List<File>?

	fun pickDirectory(
		initialDirectory: String?,
		title: String?,
		parentWindow: Frame?,
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
