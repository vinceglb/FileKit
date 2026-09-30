package io.github.vinceglb.filekit

import org.khronos.webgl.Uint8Array
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.unsafeCast

@OptIn(ExperimentalWasmJsInterop::class)
internal actual fun ByteArray.toWebBytes(): JsAny =
    Uint8Array(toTypedArray()).unsafeCast<JsAny>()
