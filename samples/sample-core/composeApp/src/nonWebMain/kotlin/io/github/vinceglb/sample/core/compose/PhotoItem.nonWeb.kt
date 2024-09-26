package io.github.vinceglb.sample.core.compose

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.core.PlatformFile
import io.github.vinceglb.filekit.core.underlyingFile

@Composable
actual fun rememberFileCoilModel(file: PlatformFile): Any? = file.underlyingFile
