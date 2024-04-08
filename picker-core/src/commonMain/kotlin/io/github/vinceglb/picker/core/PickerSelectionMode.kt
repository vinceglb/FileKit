package io.github.vinceglb.picker.core

public expect sealed class PickerSelectionMode<Out> {
	public class SelectionResult

	public abstract fun result(selection: SelectionResult): Out?

	public class SingleFile(
		extensions: List<String>? = null
	) : PickerSelectionMode<PlatformFile> {
		override fun result(selection: SelectionResult): PlatformFile?
	}

	public class MultipleFiles(
		extensions: List<String>? = null
	) : PickerSelectionMode<PlatformFiles>

	@Suppress("ConvertObjectToDataObject")
	public object Directory : PickerSelectionMode<PlatformDirectory> {
		public val isSupported: Boolean
	}
}
