package io.github.vinceglb.filekit.coil

import io.github.vinceglb.filekit.PlatformFile

public actual val PlatformFile.coilModel: Any
    get() = file
