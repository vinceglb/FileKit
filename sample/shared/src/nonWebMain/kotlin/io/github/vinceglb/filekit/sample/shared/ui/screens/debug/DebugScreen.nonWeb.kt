package io.github.vinceglb.filekit.sample.shared.ui.screens.debug

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.writeString

internal actual suspend fun debugPlatformTest(folder: PlatformFile) {
    val file = folder / "debug-test-file.txt"
    file.writeString("Vince")
}
