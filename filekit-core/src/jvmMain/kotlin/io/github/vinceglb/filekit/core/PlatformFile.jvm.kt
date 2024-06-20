package io.github.vinceglb.filekit.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files

public actual data class PlatformFile(
	val file: File,
) {
	public actual val name: String =
		file.name

	public actual val path: String? =
		file.absolutePath

	public actual suspend fun readBytes(): ByteArray =
		withContext(Dispatchers.IO) { file.readBytes() }

	public actual fun getSize(): Long? = Files.size(file.toPath())
}

public actual data class PlatformDirectory(
	val file: File,
) {
	public actual val path: String? =
		file.absolutePath
}
