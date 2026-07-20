package io.github.vinceglb.filekit

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.reinterpret
import platform.CoreFoundation.CFBooleanGetValue
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFStringCreateWithCString
import platform.CoreFoundation.kCFAllocatorDefault
import platform.CoreFoundation.kCFStringEncodingUTF8
import platform.Foundation.NSURLBookmarkCreationWithSecurityScope
import platform.Foundation.NSURLBookmarkResolutionWithSecurityScope
import platform.Security.SecTaskCopyValueForEntitlement
import platform.Security.SecTaskCreateFromSelf

@OptIn(ExperimentalForeignApi::class)
internal actual fun appleBookmarkCreationConfiguration(): AppleBookmarkCreationConfiguration =
    if (AppleBookmarkEnvironment.isAppSandboxEnabled()) {
        AppleBookmarkCreationConfiguration(
            options = NSURLBookmarkCreationWithSecurityScope,
            kind = MacOsBookmarkKind.SecurityScoped,
        )
    } else {
        AppleBookmarkCreationConfiguration(
            options = 0u,
            kind = MacOsBookmarkKind.Regular,
        )
    }

internal actual fun encodeAppleBookmarkPayload(
    payload: ByteArray,
    configuration: AppleBookmarkCreationConfiguration,
): ByteArray = MacOsBookmarkEnvelope(
    kind = requireNotNull(configuration.kind),
    payload = payload,
).encode()

internal actual fun decodeAppleBookmarkPayload(bytes: ByteArray): AppleBookmarkPayload {
    val envelope = MacOsBookmarkEnvelope.decodeOrNull(bytes)
    if (envelope == null) {
        return AppleBookmarkPayload(
            bytes = bytes,
            resolutionOptions = 0u,
            isLegacy = true,
        )
    }
    return AppleBookmarkPayload(
        bytes = envelope.payload,
        resolutionOptions = envelope.kind.resolutionOptions,
        isLegacy = false,
    )
}

private val MacOsBookmarkKind?.resolutionOptions: ULong
    get() = when (this) {
        MacOsBookmarkKind.SecurityScoped -> NSURLBookmarkResolutionWithSecurityScope
        MacOsBookmarkKind.Regular, null -> 0u
    }

@OptIn(ExperimentalForeignApi::class)
internal object AppleBookmarkEnvironment {
    internal var entitlementReader: () -> Boolean = ::readAppSandboxEntitlement

    fun isAppSandboxEnabled(): Boolean = entitlementReader()
}

@OptIn(ExperimentalForeignApi::class)
private fun readAppSandboxEntitlement(): Boolean {
    val task = SecTaskCreateFromSelf(kCFAllocatorDefault) ?: return false
    val entitlement = CFStringCreateWithCString(
        alloc = kCFAllocatorDefault,
        cStr = APP_SANDBOX_ENTITLEMENT,
        encoding = kCFStringEncodingUTF8,
    ) ?: run {
        CFRelease(task)
        return false
    }
    return try {
        val value = SecTaskCopyValueForEntitlement(task, entitlement, null) ?: return false
        try {
            CFBooleanGetValue(value.reinterpret())
        } finally {
            CFRelease(value)
        }
    } finally {
        CFRelease(entitlement)
        CFRelease(task)
    }
}

private const val APP_SANDBOX_ENTITLEMENT = "com.apple.security.app-sandbox"
