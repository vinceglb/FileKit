package io.github.vinceglb.picker.core

import platform.Foundation.NSURL

public actual sealed class PickerSelectionMode<Out> {
	internal actual class SelectionResult(
		val nsUrls: List<NSURL>
	)

	internal actual abstract fun result(selection: SelectionResult): Out?

	public actual class SingleFile actual constructor(
		public val extensions: List<String>?
	) : PickerSelectionMode<PlatformFile>() {
		actual override fun result(selection: SelectionResult): PlatformFile? {
			return selection.nsUrls
				.firstOrNull()
				?.let { PlatformFile(it) }
		}
	}

	public actual class MultipleFiles actual constructor(
		public val extensions: List<String>?
	) : PickerSelectionMode<PlatformFiles>() {
		override fun result(selection: SelectionResult): PlatformFiles? {
			return selection.nsUrls
				.takeIf { it.isNotEmpty() }
				?.map { PlatformFile(it) }
		}
	}

	public actual data object Directory : PickerSelectionMode<PlatformDirectory>() {
		public actual val isSupported: Boolean = true

		override fun result(selection: SelectionResult): PlatformDirectory? {
			return selection.nsUrls
				.firstOrNull()
				?.let { PlatformDirectory(it) }
		}
	}
}
