package io.github.vinceglb.filekit.dialogs

import android.net.Uri
import androidx.core.content.FileProvider
import io.github.vinceglb.filekit.AndroidFile
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.context

public fun PlatformFile.toAndroidUri(authority: String): Uri =
    when (val androidFile = androidFile) {
        is AndroidFile.UriWrapper -> androidFile.uri
        is AndroidFile.FileWrapper -> FileProvider.getUriForFile(
            FileKit.context,
            authority,
            androidFile.file
        )
    }
