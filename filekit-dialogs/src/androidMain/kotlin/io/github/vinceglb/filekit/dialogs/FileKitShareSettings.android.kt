package io.github.vinceglb.filekit.dialogs

import android.content.Intent
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.context

/**
 * Android implementation of [FileKitShareSettings].
 *
 * @property authority The content authority string used for creating a [android.net.Uri].
 * Defaults to "{applicationId}.FileKitFileProvider".
 * @property addOptionChooseIntent Callback to customize the choose intent.
 */
public actual class FileKitShareSettings(
    public val authority: String = "${FileKit.context.packageName}.FileKitFileProvider",
    public val addOptionChooseIntent: (Intent) -> Unit = {},
) {
    public actual companion object {
        /**
         * Creates a default instance of [FileKitShareSettings].
         */
        public actual fun createDefault(): FileKitShareSettings = FileKitShareSettings()
    }
}
