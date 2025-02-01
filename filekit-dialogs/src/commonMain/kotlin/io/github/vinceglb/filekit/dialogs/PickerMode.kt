package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.PlatformFile

public sealed class PickerMode<Out> {
    public abstract fun parseResult(value: List<PlatformFile>?): Out?

    public data object Single : PickerMode<PlatformFile>() {
        override fun parseResult(value: List<PlatformFile>?): PlatformFile? {
            return value?.firstOrNull()
        }
    }

    /**
     * @property maxItems sets the limit of how many items can be selected. NOTE: This is only
     * supported by Android / iOS and only when picking media files, not any kind of file.
     */
    public data class Multiple(val maxItems: Int? = null) : PickerMode<List<PlatformFile>>() {
        init {
            require(maxItems == null || maxItems in 1..50) {
                "maxItems must be contained between 1 <= maxItems <= 50 but current value is $maxItems"
            }
        }

        override fun parseResult(value: List<PlatformFile>?): List<PlatformFile>? {
            return value?.takeIf { it.isNotEmpty() }
        }
    }
}
