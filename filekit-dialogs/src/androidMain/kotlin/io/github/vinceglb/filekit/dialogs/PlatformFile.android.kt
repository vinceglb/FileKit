package io.github.vinceglb.filekit.dialogs

import android.net.Uri
import androidx.core.content.FileProvider
import io.github.vinceglb.filekit.AndroidFile
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.context

public val PlatformFile.uri: Uri
    get() = androidFile.let { androidFile ->
        when (androidFile) {
            is AndroidFile.UriWrapper -> androidFile.uri
            is AndroidFile.FileWrapper -> {
                val context = FileKit.context
                val authority = "${context.packageName}.filekit.fileprovider"
                FileProvider.getUriForFile(context, authority, androidFile.file)
            }
        }
    }
