package io.github.vinceglb.picker.core

import org.w3c.files.File

public actual sealed class PickerSelectionMode<Out> {
	internal actual class SelectionResult(
		val files: List<File>?
	)

    internal actual abstract fun result(selection: SelectionResult): Out?

	public actual class SingleFile actual constructor(
		public val extensions: List<String>?
	) : PickerSelectionMode<PlatformFile>() {
		actual override fun result(selection: SelectionResult): PlatformFile? {
			return selection.files
				?.firstOrNull()
				?.let { PlatformFile(it) }
		}
	}

	public actual class MultipleFiles actual constructor(
		public val extensions: List<String>?
	) : PickerSelectionMode<PlatformFiles>() {
		override fun result(selection: SelectionResult): PlatformFiles? {
			return selection.files
				?.takeIf { it.isNotEmpty() }
				?.map { PlatformFile(it) }
		}
	}

	public actual data object Directory : PickerSelectionMode<PlatformDirectory>() {
		public actual val isSupported: Boolean = false

		override fun result(selection: SelectionResult): PlatformDirectory? {
			throw NotImplementedError("Directory selection is not supported on the web")
		}
	}
}
