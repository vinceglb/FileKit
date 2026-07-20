package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.exceptions.BookmarkResolutionFailure
import platform.Foundation.NSError

internal actual fun appleBookmarkCreationConfiguration(): AppleBookmarkCreationConfiguration =
    AppleBookmarkCreationConfiguration(options = 0u, kind = null)

internal actual fun encodeAppleBookmarkPayload(
    payload: ByteArray,
    configuration: AppleBookmarkCreationConfiguration,
): ByteArray = payload

internal actual fun decodeAppleBookmarkPayload(bytes: ByteArray): AppleBookmarkPayload =
    AppleBookmarkPayload(bytes = bytes, resolutionOptions = 0u, isLegacy = false)

internal actual fun classifyAppleBookmarkResolutionError(error: NSError?): BookmarkResolutionFailure =
    BookmarkResolutionFailure.RESOURCE_UNAVAILABLE
