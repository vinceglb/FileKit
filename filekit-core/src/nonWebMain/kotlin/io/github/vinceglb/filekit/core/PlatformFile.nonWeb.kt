package io.github.vinceglb.filekit.core

public expect val PlatformFile.path: String

public expect fun PlatformFile.getStream(): PlatformInputStream
