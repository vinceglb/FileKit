package io.github.vinceglb.filekit.utils

import org.khronos.webgl.toInt8Array
import kotlin.js.JsAny
import kotlin.js.JsArray
import kotlin.js.set

public fun ByteArray.toJsArray(): JsArray<JsAny?> {
    // Create a JS array
    val jsArray = JsArray<JsAny?>()
    jsArray[0] = this.toInt8Array()

    return jsArray
}
