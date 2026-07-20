package io.github.vinceglb.filekit

internal actual fun appleBookmarkCreationConfiguration(): AppleBookmarkCreationConfiguration =
    AppleBookmarkCreationConfiguration(options = 0u, kind = null)

internal actual fun encodeAppleBookmarkPayload(
    payload: ByteArray,
    configuration: AppleBookmarkCreationConfiguration,
): ByteArray = payload

internal actual fun decodeAppleBookmarkPayload(bytes: ByteArray): AppleBookmarkPayload =
    AppleBookmarkPayload(bytes = bytes, resolutionOptions = 0u, isLegacy = false)
