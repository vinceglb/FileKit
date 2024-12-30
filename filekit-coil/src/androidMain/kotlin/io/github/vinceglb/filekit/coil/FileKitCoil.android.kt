package io.github.vinceglb.filekit.coil

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.AndroidFile
import io.github.vinceglb.filekit.PlatformFile

public actual val PlatformFile.coilModel: Any
    get() = androidFile.let {
        when (it) {
            is AndroidFile.FileWrapper -> it.file
            is AndroidFile.UriWrapper -> it.uri
        }
    }

@Composable
internal actual fun AsyncImagePlatformEffects(file: PlatformFile?) {}
