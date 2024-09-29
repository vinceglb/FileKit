package io.github.vinceglb.filekit

public expect val PlatformFile.path: String

public expect fun PlatformFile.getStream(): PlatformInputStream
