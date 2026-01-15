package io.github.vinceglb.filekit.sample.shared.util

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.isDirectory

internal actual fun createPlatformFileForPreviews(name: String): PlatformFile =
    PlatformFile(name)

internal actual fun PlatformFile.isDirectory(): Boolean =
    this.isDirectory()
