package io.github.vinceglb.filekit.dialog

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile

public expect suspend fun FileKit.takePhoto(): PlatformFile?
