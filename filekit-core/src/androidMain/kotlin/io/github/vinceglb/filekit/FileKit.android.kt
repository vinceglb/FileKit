package io.github.vinceglb.filekit

import android.content.Context
import java.lang.ref.WeakReference

public actual object FileKit {
    private var _context: WeakReference<Context?> = WeakReference(null)
    public val context: Context
        get() = _context.get()
            ?: throw FileKitNotInitializedException()

    internal fun init(context: Context) {
        _context = WeakReference(context)
    }
}

public actual val FileKit.filesDir: PlatformFile
    get() = context.filesDir.let(::PlatformFile)

public actual val FileKit.cacheDir: PlatformFile
    get() = context.cacheDir.let(::PlatformFile)
