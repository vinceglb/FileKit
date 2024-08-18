package io.github.vinceglb.filekit.core

public sealed class PickerMode<Out> {
    public abstract fun parseResult(value: PlatformFiles?): Out?

    public data object Single : PickerMode<IPlatformFile>() {
        override fun parseResult(value: PlatformFiles?): IPlatformFile? {
            return value?.firstOrNull()
        }
    }

    /**
     * @property maxItems sets the limit of how many items can be selected. NOTE: This is only
     * supported by Android / iOS and only when picking media files, not any kind of file.
     */
    public data class Multiple(val maxItems: Int? = null) : PickerMode<PlatformFiles>() {
        init {
            require(maxItems == null || maxItems in 1..50) {
                "maxItems must be contained between 1 <= maxItems <= 50 but current value is $maxItems"
            }
        }

        override fun parseResult(value: PlatformFiles?): PlatformFiles? {
            return value?.takeIf { it.isNotEmpty() }
        }
    }
}
