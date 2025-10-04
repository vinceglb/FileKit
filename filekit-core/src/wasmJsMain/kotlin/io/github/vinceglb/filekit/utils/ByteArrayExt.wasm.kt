package io.github.vinceglb.filekit.utils

import org.khronos.webgl.toInt8Array

@OptIn(ExperimentalWasmJsInterop::class)
public fun ByteArray.toJsArray(): JsArray<JsAny?> {
    // Create a JS array
    val jsArray = JsArray<JsAny?>()
    jsArray[0] = this.toInt8Array()

    return jsArray
}
