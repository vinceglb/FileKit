package io.github.vinceglb.filekit.core

public sealed class PickerMode<Out> {
    public abstract fun parseResult(value: PlatformFiles?): Out?

    public data object Single : PickerMode<PlatformFile>() {
        override fun parseResult(value: PlatformFiles?): PlatformFile? {
            return value?.firstOrNull()
        }
    }

    public data class Multiple(val maxItems: Int = Int.MAX_VALUE) : PickerMode<PlatformFiles>() {
        override fun parseResult(value: PlatformFiles?): PlatformFiles? {
            return value?.takeIf { it.isNotEmpty() }
        }
    }
}
