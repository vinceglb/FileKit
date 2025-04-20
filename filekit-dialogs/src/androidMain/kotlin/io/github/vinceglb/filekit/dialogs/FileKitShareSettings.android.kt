package io.github.vinceglb.filekit.dialogs

import android.content.Intent
import io.github.vinceglb.filekit.FileKit

public actual open class FileKitShareSettings(
    public val authority: String = "${FileKit.context.packageName}.fileprovider",
    public val addOptionChooseIntent: (Intent) -> Unit = {},
) {
    public actual companion object {
        public actual fun createDefault(): FileKitShareSettings = FileKitShareSettings()
    }
}
