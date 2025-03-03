package io.github.vinceglb.filekit.coil

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile

public actual val PlatformFile.coilModel: Any
    get() = file

@Composable
internal actual fun AsyncImagePlatformEffects(file: PlatformFile?) {}
