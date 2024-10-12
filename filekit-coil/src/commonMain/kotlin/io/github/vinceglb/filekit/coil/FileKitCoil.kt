package io.github.vinceglb.filekit.coil

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile

@Composable
public expect fun rememberPlatformFileCoilModel(file: PlatformFile): Any?
