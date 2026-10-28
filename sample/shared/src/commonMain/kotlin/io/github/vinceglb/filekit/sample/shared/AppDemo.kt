package io.github.vinceglb.filekit.sample.shared

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * A sample screen that can be opened directly, for example from a `?demo=` URL parameter
 * when the web sample is embedded in the documentation.
 */
public enum class AppDemo(
    public val id: String,
) {
    FilePicker("file-picker"),
    GalleryPicker("gallery-picker"),
    DirectoryPicker("directory-picker"),
    CameraPicker("camera-picker"),
    FileSaver("file-saver"),
    ShareFile("share-file"),
    ;

    public companion object {
        public fun fromId(id: String?): AppDemo? = entries.firstOrNull { it.id == id }
    }
}

/**
 * Whether the sample runs embedded in another page (e.g. a documentation iframe).
 * A CompositionLocal avoids threading this display-only flag through every screen.
 */
internal val LocalAppEmbedded = staticCompositionLocalOf { false }
