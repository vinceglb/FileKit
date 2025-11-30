package io.github.vinceglb.filekit.utils

import org.khronos.webgl.Uint8Array

public fun ByteArray.toBitsArray(): Array<Uint8Array> = arrayOf(Uint8Array(this.toTypedArray()))
