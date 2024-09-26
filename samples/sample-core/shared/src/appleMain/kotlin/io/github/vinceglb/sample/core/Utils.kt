package io.github.vinceglb.sample.core

import io.github.vinceglb.filekit.core.PlatformFile
import io.github.vinceglb.filekit.core.name
import io.github.vinceglb.filekit.core.path

fun getFileName(file: PlatformFile): String = file.name
fun getFilePath(file: PlatformFile?): String? = file?.path
