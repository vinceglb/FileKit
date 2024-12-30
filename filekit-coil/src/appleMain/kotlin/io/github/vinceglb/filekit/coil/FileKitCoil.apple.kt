package io.github.vinceglb.filekit.coil

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import io.github.vinceglb.filekit.PlatformFile

public actual val PlatformFile.coilModel: Any
    get() = nsUrl

@Composable
internal actual fun AsyncImagePlatformEffects(file: PlatformFile?) {
    DisposableEffect(file) {
        file?.nsUrl?.startAccessingSecurityScopedResource()

        onDispose {
            file?.nsUrl?.stopAccessingSecurityScopedResource()
        }
    }
}
