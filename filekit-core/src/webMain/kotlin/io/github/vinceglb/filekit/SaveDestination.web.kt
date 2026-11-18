@file:OptIn(ExperimentalFileKitApi::class)

package io.github.vinceglb.filekit

import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.unsafeCast

/**
 * Creates a web save destination for use by FileKit modules.
 *
 * This is public only to allow `filekit-dialogs` to pass the browser's opaque file handle to core.
 * Applications should obtain save destinations through the dialogs API.
 *
 * @param handle The `FileSystemFileHandle`, or `null` to use browser download behavior.
 * @param name The suggested file name.
 */
@FileKitCoreInternalApi
@OptIn(ExperimentalWasmJsInterop::class)
public fun createSaveDestination(handle: JsAny?, name: String): PlatformFile =
    PlatformFile(
        WebFile.SaveDestination(
            handle = handle?.unsafeCast<FileSystemFileHandle>(),
            name = name,
        ),
    )