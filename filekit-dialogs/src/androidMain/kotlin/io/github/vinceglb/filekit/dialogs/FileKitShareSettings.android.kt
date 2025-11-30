package io.github.vinceglb.filekit.dialogs

import android.content.Intent
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.context

public actual class FileKitShareSettings(
    public val authority: String = "${FileKit.context.packageName}.FileKitFileProvider",
    public val addOptionChooseIntent: (Intent) -> Unit = {},
) {
    public actual companion object {
        public actual fun createDefault(): FileKitShareSettings = FileKitShareSettings()
    }
}
