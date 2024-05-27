package io.github.vinceglb.filekit.core

public sealed class PickerSelectionMode<Out> {
    public abstract fun parseResult(value: PlatformFiles?): Out?

    public data object Single : PickerSelectionMode<PlatformFile>() {
        override fun parseResult(value: PlatformFiles?): PlatformFile? {
            return value?.firstOrNull()
        }
    }

    public data object Multiple : PickerSelectionMode<PlatformFiles>() {
        override fun parseResult(value: PlatformFiles?): PlatformFiles? {
            return value?.takeIf { it.isNotEmpty() }
        }
    }
}
