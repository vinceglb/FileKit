package io.github.vinceglb.picker.core

public expect object Picker {
	public suspend fun <Out> pick(
		mode: PickerSelectionMode<Out>,
		title: String? = null,
		initialDirectory: String? = null,
	): Out?
}
