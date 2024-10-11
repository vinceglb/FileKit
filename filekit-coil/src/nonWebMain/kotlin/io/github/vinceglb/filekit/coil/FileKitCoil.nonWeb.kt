package io.github.vinceglb.filekit.coil

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile

public expect val PlatformFile.coilModel: Any

@Composable
public actual fun rememberPlatformFileCoilModel(file: PlatformFile): Any? =
    file.coilModel
