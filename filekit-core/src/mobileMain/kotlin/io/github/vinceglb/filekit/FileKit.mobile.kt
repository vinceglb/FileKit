package io.github.vinceglb.filekit

public expect suspend fun FileKit.saveImageToGallery(
    bytes: ByteArray,
    baseName: String,
    extension: String
): Boolean
