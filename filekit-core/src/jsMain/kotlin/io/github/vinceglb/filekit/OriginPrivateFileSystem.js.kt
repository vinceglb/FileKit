package io.github.vinceglb.filekit

import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.unsafeCast

// Kotlin/JS represents ByteArray as an Int8Array, which the browser accepts as-is.
@OptIn(ExperimentalWasmJsInterop::class)
internal actual fun ByteArray.toWebBytes(): JsAny = unsafeCast<JsAny>()
