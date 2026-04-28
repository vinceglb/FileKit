package io.github.vinceglb.filekit.sample.shared.util

import io.github.vinceglb.filekit.PlatformFile

internal actual fun createPlatformFileForPreviews(name: String): PlatformFile =
    PlatformFile(name)
