package io.github.vinceglb.picker.core

public expect class PlatformFile {
	public val name: String
	public val path: String?

	public suspend fun readBytes(): ByteArray
}

public expect class PlatformDirectory {
	public val path: String?
}

public typealias PlatformFiles = List<PlatformFile>
