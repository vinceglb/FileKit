package io.github.vinceglb.filekit

import com.sun.jna.Library
import com.sun.jna.Memory
import com.sun.jna.Native
import com.sun.jna.NativeLong
import com.sun.jna.Platform
import com.sun.jna.Pointer
import com.sun.jna.PointerType
import com.sun.jna.platform.mac.CoreFoundation
import com.sun.jna.ptr.ByteByReference
import com.sun.jna.ptr.PointerByReference
import io.github.vinceglb.filekit.exceptions.BookmarkResolutionException
import io.github.vinceglb.filekit.exceptions.BookmarkResolutionFailure
import java.io.File
import java.lang.ref.Cleaner

internal data class ResolvedMacOsBookmark(
    val file: File,
    val isStale: Boolean,
    val access: MacOsBookmarkAccess?,
)

internal interface MacOsBookmarkAccess {
    fun covers(file: File): Boolean

    fun start(): Boolean

    fun stop()

    fun release()
}

private class NativeMacOsBookmarkAccess(
    root: File,
    private val url: CFUrlRef,
) : MacOsBookmarkAccess {
    private val rootPath = root.canonicalFile.toPath()
    private var activeAccesses = 0
    private var released = false

    @Suppress("unused")
    private val cleanable = cleaner.register(this, NativeUrlReleaser(url))

    override fun covers(file: File): Boolean = file.canonicalFile.toPath().startsWith(rootPath)

    override fun start(): Boolean = synchronized(this) {
        check(!released) { "This security-scoped bookmark has been released" }
        val granted = CoreFoundationBookmarkApi.instance.CFURLStartAccessingSecurityScopedResource(url) != 0.toByte()
        if (granted) {
            activeAccesses += 1
        }
        granted
    }

    override fun stop() {
        synchronized(this) {
            if (activeAccesses == 0) return
            CoreFoundationBookmarkApi.instance.CFURLStopAccessingSecurityScopedResource(url)
            activeAccesses -= 1
            releaseNativeUrlIfDrained()
        }
    }

    override fun release() {
        synchronized(this) {
            if (released) return
            released = true
            releaseNativeUrlIfDrained()
        }
    }

    private fun releaseNativeUrlIfDrained() {
        if (released && activeAccesses == 0) {
            cleanable.clean()
        }
    }

    companion object {
        private val cleaner = Cleaner.create()
    }

    private class NativeUrlReleaser(
        private val url: CFUrlRef,
    ) : Runnable {
        override fun run() {
            url.release()
        }
    }
}

private fun readAppSandboxEntitlement(): Boolean {
    check(Platform.isMac())
    val task = SecurityApi.instance.SecTaskCreateFromSelf(null)
        ?: throw IllegalStateException("Could not inspect the App Sandbox entitlement")
    try {
        val entitlement = CoreFoundation.CFStringRef.createCFString(APP_SANDBOX_ENTITLEMENT)
            ?: throw IllegalStateException("Could not create the App Sandbox entitlement name")
        try {
            val error = PointerByReference()
            val value = SecurityApi.instance.SecTaskCopyValueForEntitlement(task, entitlement, error)
            if (value == null) {
                error.value?.let { throw CoreFoundation.CFTypeRef(it).toEntitlementException() }
                return false
            }
            try {
                if (!value.isTypeID(CoreFoundation.BOOLEAN_TYPE_ID)) {
                    throw IllegalStateException("The App Sandbox entitlement is not a Boolean")
                }
                return CoreFoundation.INSTANCE.CFBooleanGetValue(
                    CoreFoundation.CFBooleanRef(value.pointer),
                ) != 0.toByte()
            } finally {
                value.release()
            }
        } finally {
            entitlement.release()
        }
    } finally {
        task.release()
    }
}

private fun CoreFoundation.CFTypeRef.toEntitlementException(): IllegalStateException {
    try {
        val description = CoreFoundationBookmarkApi.instance.CFErrorCopyDescription(this)
        val message = try {
            description?.stringValue() ?: "Could not inspect the App Sandbox entitlement"
        } finally {
            description?.release()
        }
        return IllegalStateException(message)
    } finally {
        release()
    }
}

internal fun macOsBookmarkKind(isSandboxed: Boolean): MacOsBookmarkKind = if (isSandboxed) {
    MacOsBookmarkKind.SecurityScoped
} else {
    MacOsBookmarkKind.Regular
}

internal fun macOsBookmarkKindForCurrentProcess(): MacOsBookmarkKind =
    macOsBookmarkKind(isSandboxed = readAppSandboxEntitlement())

internal object MacOsBookmarks {
    fun create(file: File, kind: MacOsBookmarkKind): ByteArray {
        check(Platform.isMac())
        val path = CoreFoundation.CFStringRef.createCFString(file.canonicalPath)
        try {
            val url = CoreFoundationBookmarkApi.instance.CFURLCreateWithFileSystemPath(
                allocator = null,
                filePath = path,
                pathStyle = POSIX_PATH_STYLE,
                isDirectory = if (file.isDirectory) 1 else 0,
            ) ?: throw BookmarkResolutionException(
                reason = BookmarkResolutionFailure.RESOURCE_UNAVAILABLE,
                message = "Could not create a native URL for ${file.path}",
            )
            try {
                val bookmark = CoreFoundationBookmarkApi.instance.CFURLCreateBookmarkData(
                    allocator = null,
                    url = url,
                    options = NativeLong(kind.creationOptions),
                    resourcePropertiesToInclude = null,
                    relativeToUrl = null,
                    error = null,
                ) ?: throw BookmarkResolutionException(
                    reason = BookmarkResolutionFailure.RESOURCE_UNAVAILABLE,
                    message = "Could not create bookmark data for ${file.path}",
                )
                try {
                    return bookmark.bytePtr.getByteArray(0, bookmark.length)
                } finally {
                    bookmark.release()
                }
            } finally {
                url.release()
            }
        } finally {
            path.release()
        }
    }

    fun resolve(payload: ByteArray, kind: MacOsBookmarkKind): ResolvedMacOsBookmark {
        check(Platform.isMac())
        val payloadMemory = Memory(payload.size.toLong()).apply { write(0, payload, 0, payload.size) }
        val bookmarkData = CoreFoundation.INSTANCE.CFDataCreate(
            null,
            payloadMemory,
            CoreFoundation.CFIndex(payload.size.toLong()),
        )
        try {
            val isStale = ByteByReference()
            val error = PointerByReference()
            val url = CoreFoundationBookmarkApi.instance.CFURLCreateByResolvingBookmarkData(
                allocator = null,
                bookmark = bookmarkData,
                options = NativeLong(kind.resolutionOptions),
                relativeToUrl = null,
                resourcePropertiesToInclude = null,
                isStale = isStale,
                error = error,
            ) ?: throw error.toBookmarkResolutionException()
            var releaseUrl = true
            try {
                val path = CoreFoundationBookmarkApi.instance.CFURLCopyFileSystemPath(url, POSIX_PATH_STYLE)
                    ?: throw BookmarkResolutionException(
                        reason = BookmarkResolutionFailure.RESOURCE_UNAVAILABLE,
                        message = "The resolved macOS bookmark has no file-system path",
                    )
                try {
                    val file = File(path.stringValue())
                    val access = if (kind == MacOsBookmarkKind.SecurityScoped) {
                        releaseUrl = false
                        NativeMacOsBookmarkAccess(file, url)
                    } else {
                        null
                    }
                    return ResolvedMacOsBookmark(
                        file = file,
                        isStale = isStale.value != 0.toByte(),
                        access = access,
                    )
                } finally {
                    path.release()
                }
            } finally {
                if (releaseUrl) url.release()
            }
        } finally {
            bookmarkData.release()
        }
    }
}

private fun PointerByReference.toBookmarkResolutionException(): BookmarkResolutionException {
    val errorPointer = value ?: return BookmarkResolutionException(
        reason = BookmarkResolutionFailure.RESOURCE_UNAVAILABLE,
        message = "Could not resolve macOS bookmark data",
    )
    val error = CoreFoundation.CFTypeRef(errorPointer)
    try {
        val domain = CoreFoundationBookmarkApi.instance.CFErrorGetDomain(error)?.stringValue()
        val code = CoreFoundationBookmarkApi.instance.CFErrorGetCode(error).toLong()
        val description = CoreFoundationBookmarkApi.instance.CFErrorCopyDescription(error)
        val message = try {
            description?.stringValue() ?: "Could not resolve macOS bookmark data"
        } finally {
            description?.release()
        }
        return BookmarkResolutionException(
            reason = if (domain == COCOA_ERROR_DOMAIN && code == NS_FILE_READ_CORRUPT_ERROR_CODE) {
                BookmarkResolutionFailure.INVALID_DATA
            } else {
                BookmarkResolutionFailure.RESOURCE_UNAVAILABLE
            },
            message = message,
        )
    } finally {
        error.release()
    }
}

private val MacOsBookmarkKind.creationOptions: Long
    get() = when (this) {
        MacOsBookmarkKind.Regular -> 0
        MacOsBookmarkKind.SecurityScoped -> 1L shl 11
    }

private val MacOsBookmarkKind.resolutionOptions: Long
    get() = when (this) {
        MacOsBookmarkKind.Regular -> 0
        MacOsBookmarkKind.SecurityScoped -> 1L shl 10
    }

private val POSIX_PATH_STYLE = CoreFoundation.CFIndex(0)

internal class CFUrlRef : PointerType {
    constructor() : super()
    constructor(pointer: Pointer) : super(pointer)

    fun release() {
        CoreFoundation.INSTANCE.CFRelease(CoreFoundation.CFTypeRef(pointer))
    }
}

@Suppress("ktlint:standard:function-naming", "FunctionName")
internal interface CoreFoundationBookmarkApi : Library {
    fun CFURLCreateWithFileSystemPath(
        allocator: CoreFoundation.CFAllocatorRef?,
        filePath: CoreFoundation.CFStringRef,
        pathStyle: CoreFoundation.CFIndex,
        isDirectory: Byte,
    ): CFUrlRef?

    fun CFURLCreateBookmarkData(
        allocator: CoreFoundation.CFAllocatorRef?,
        url: CFUrlRef,
        options: NativeLong,
        resourcePropertiesToInclude: CoreFoundation.CFArrayRef?,
        relativeToUrl: CFUrlRef?,
        error: PointerByReference?,
    ): CoreFoundation.CFDataRef?

    fun CFURLCreateByResolvingBookmarkData(
        allocator: CoreFoundation.CFAllocatorRef?,
        bookmark: CoreFoundation.CFDataRef,
        options: NativeLong,
        relativeToUrl: CFUrlRef?,
        resourcePropertiesToInclude: CoreFoundation.CFArrayRef?,
        isStale: ByteByReference,
        error: PointerByReference?,
    ): CFUrlRef?

    fun CFURLCopyFileSystemPath(
        url: CFUrlRef,
        pathStyle: CoreFoundation.CFIndex,
    ): CoreFoundation.CFStringRef?

    fun CFURLStartAccessingSecurityScopedResource(url: CFUrlRef): Byte

    fun CFURLStopAccessingSecurityScopedResource(url: CFUrlRef)

    fun CFErrorGetCode(error: CoreFoundation.CFTypeRef): CoreFoundation.CFIndex

    fun CFErrorGetDomain(error: CoreFoundation.CFTypeRef): CoreFoundation.CFStringRef?

    fun CFErrorCopyDescription(error: CoreFoundation.CFTypeRef): CoreFoundation.CFStringRef?

    companion object {
        val instance: CoreFoundationBookmarkApi = Native.load("CoreFoundation", CoreFoundationBookmarkApi::class.java)
    }
}

internal class SecTaskRef : PointerType {
    constructor() : super()
    constructor(pointer: Pointer) : super(pointer)

    fun release() {
        CoreFoundation.INSTANCE.CFRelease(CoreFoundation.CFTypeRef(pointer))
    }
}

@Suppress("ktlint:standard:function-naming", "FunctionName")
internal interface SecurityApi : Library {
    fun SecTaskCreateFromSelf(allocator: CoreFoundation.CFAllocatorRef?): SecTaskRef?

    fun SecTaskCopyValueForEntitlement(
        task: SecTaskRef,
        entitlement: CoreFoundation.CFStringRef,
        error: PointerByReference?,
    ): CoreFoundation.CFTypeRef?

    companion object {
        val instance: SecurityApi = Native.load("Security", SecurityApi::class.java)
    }
}
