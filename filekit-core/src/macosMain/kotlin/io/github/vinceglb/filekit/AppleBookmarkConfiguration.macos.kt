package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.exceptions.BookmarkResolutionFailure
import io.github.vinceglb.filekit.exceptions.FileKitException
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.value
import platform.CoreFoundation.CFBooleanGetTypeID
import platform.CoreFoundation.CFBooleanGetValue
import platform.CoreFoundation.CFErrorRefVar
import platform.CoreFoundation.CFGetTypeID
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFStringCreateWithCString
import platform.CoreFoundation.kCFAllocatorDefault
import platform.CoreFoundation.kCFStringEncodingUTF8
import platform.Foundation.NSError
import platform.Foundation.NSURLBookmarkCreationWithSecurityScope
import platform.Foundation.NSURLBookmarkResolutionWithSecurityScope
import platform.Security.SecTaskCopyValueForEntitlement
import platform.Security.SecTaskCreateFromSelf

@OptIn(ExperimentalForeignApi::class)
internal actual fun appleBookmarkCreationConfiguration(): AppleBookmarkCreationConfiguration =
    appleBookmarkCreationConfiguration(isAppSandboxEnabled = readAppSandboxEntitlement())

internal fun appleBookmarkCreationConfiguration(isAppSandboxEnabled: Boolean): AppleBookmarkCreationConfiguration =
    if (isAppSandboxEnabled) {
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
        kind = envelope.kind,
    )
}

internal actual fun classifyAppleBookmarkResolutionError(error: NSError?): BookmarkResolutionFailure =
    if (error?.domain == COCOA_ERROR_DOMAIN && error.code == NS_FILE_READ_CORRUPT_ERROR_CODE) {
        BookmarkResolutionFailure.INVALID_DATA
    } else {
        BookmarkResolutionFailure.RESOURCE_UNAVAILABLE
    }

private val MacOsBookmarkKind?.resolutionOptions: ULong
    get() = when (this) {
        MacOsBookmarkKind.SecurityScoped -> NSURLBookmarkResolutionWithSecurityScope
        MacOsBookmarkKind.Regular, null -> 0u
    }

@OptIn(ExperimentalForeignApi::class)
private fun readAppSandboxEntitlement(): Boolean = memScoped {
    val task = SecTaskCreateFromSelf(kCFAllocatorDefault)
        ?: throw FileKitException("Could not inspect the App Sandbox entitlement")
    val entitlement = CFStringCreateWithCString(
        alloc = kCFAllocatorDefault,
        cStr = APP_SANDBOX_ENTITLEMENT,
        encoding = kCFStringEncodingUTF8,
    ) ?: throw FileKitException("Could not create the App Sandbox entitlement name")
    return try {
        val error = alloc<CFErrorRefVar>()
        error.value = null
        val value = SecTaskCopyValueForEntitlement(task, entitlement, error.ptr)
        if (value == null) {
            error.value?.let { failure ->
                CFRelease(failure.reinterpret())
                throw FileKitException("Could not inspect the App Sandbox entitlement")
            }
            return false
        }
        try {
            if (CFGetTypeID(value) != CFBooleanGetTypeID()) {
                throw FileKitException("The App Sandbox entitlement is not a Boolean")
            }
            CFBooleanGetValue(value.reinterpret())
        } finally {
            CFRelease(value)
        }
    } finally {
        CFRelease(entitlement)
        CFRelease(task)
    }
}
