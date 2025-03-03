package io.github.vinceglb.filekit.utils

import org.khronos.webgl.Uint8Array

public fun ByteArray.toBitsArray(): Array<Uint8Array> {
    return arrayOf(Uint8Array(this.toTypedArray()))
}
