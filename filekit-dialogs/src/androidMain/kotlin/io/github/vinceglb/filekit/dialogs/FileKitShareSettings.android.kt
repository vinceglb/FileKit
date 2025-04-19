package io.github.vinceglb.filekit.dialogs

import android.content.Intent
import io.github.vinceglb.filekit.FileKit

public actual open class FileKitShareSettings(
    public val authority: String,
    public val addOptionChooseIntent: (Intent) -> Unit
) {
    public constructor(addOptionChooseIntent: (Intent) -> Unit) : this(
        "${FileKit.context.packageName}.fileprovider",
        addOptionChooseIntent
    )

    public constructor() : this(
        addOptionChooseIntent = {}
    )

}

public class FileKitAndroidDefaultShareSettings() : FileKitShareSettings()