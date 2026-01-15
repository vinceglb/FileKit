package io.github.vinceglb.filekit.sample.shared.util

import io.github.vinceglb.filekit.PlatformFile

internal actual fun createPlatformFileForPreviews(name: String): PlatformFile =
    throw IllegalStateException("Should only be used in preview")

internal actual fun PlatformFile.isDirectory(): Boolean =
    false
