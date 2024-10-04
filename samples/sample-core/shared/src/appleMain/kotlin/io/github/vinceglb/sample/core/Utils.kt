package io.github.vinceglb.sample.core

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.path

fun getFileName(file: PlatformFile): String = file.name
fun getFilePath(file: PlatformFile?): String? = file?.path
