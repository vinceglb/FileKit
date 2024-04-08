package io.github.vinceglb.picker.core

import android.net.Uri

public actual sealed class PickerSelectionMode<Out> {
	public actual class SelectionResult(
		public val files: List<Uri>?
	)

	public actual abstract fun result(selection: SelectionResult): Out?

	public actual class SingleFile actual constructor(
		public val extensions: List<String>?
	) : PickerSelectionMode<PlatformFile>() {
		actual override fun result(selection: SelectionResult): PlatformFile? {
			val context = Picker.context.get()
				?: throw IllegalStateException("Picker context is not set")

			return selection.files
				?.firstOrNull()
				?.let { PlatformFile(it, context) }
		}
	}

	public actual class MultipleFiles actual constructor(
		public val extensions: List<String>?
	) : PickerSelectionMode<PlatformFiles>() {
		override fun result(selection: SelectionResult): PlatformFiles? {
			val context = Picker.context.get()
				?: throw IllegalStateException("Picker context is not set")

			return selection.files
				?.takeIf { it.isNotEmpty() }
				?.map { PlatformFile(it, context) }
		}
	}

	public actual data object Directory : PickerSelectionMode<PlatformDirectory>() {
		public actual val isSupported: Boolean = true

		override fun result(selection: SelectionResult): PlatformDirectory? {
			return selection.files
				?.firstOrNull()
				?.let { PlatformDirectory(it) }
		}
	}
}
