package io.github.vinceglb.filekit.sample.shared.ui.screens.debug

import io.github.vinceglb.filekit.PlatformFile

internal actual suspend fun debugPlatformTest(folder: PlatformFile) {
    // Web does not support directory-based file operations in the same way
    // This is a no-op on web platforms
}
