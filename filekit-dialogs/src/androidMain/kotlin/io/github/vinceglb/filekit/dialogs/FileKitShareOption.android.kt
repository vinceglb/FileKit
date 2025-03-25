package io.github.vinceglb.filekit.dialogs

import android.content.Intent
import io.github.vinceglb.filekit.FileKit

public actual open class FileKitShareOption(
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

public class FileKitAndroidDefaultShareOption() : FileKitShareOption()