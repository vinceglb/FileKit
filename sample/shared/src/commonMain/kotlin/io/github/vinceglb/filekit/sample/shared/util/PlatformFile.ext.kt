package io.github.vinceglb.filekit.sample.shared.util

import io.github.vinceglb.filekit.PlatformFile

internal expect fun createPlatformFileForPreviews(name: String): PlatformFile

internal expect fun PlatformFile.isDirectory(): Boolean
