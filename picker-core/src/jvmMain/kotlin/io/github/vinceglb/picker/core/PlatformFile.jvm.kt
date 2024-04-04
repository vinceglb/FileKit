package io.github.vinceglb.picker.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

public actual data class PlatformFile(
	val file: File,
) {
	public actual val name: String =
		file.name

	public actual val path: String? =
		file.absolutePath

	public actual suspend fun readBytes(): ByteArray =
		withContext(Dispatchers.IO) { file.readBytes() }
}

public actual data class PlatformDirectory(
	val file: File,
) {
	public actual val path: String? =
		file.absolutePath
}
