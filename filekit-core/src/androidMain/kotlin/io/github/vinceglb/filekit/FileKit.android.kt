package io.github.vinceglb.filekit

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultRegistry
import java.lang.ref.WeakReference

// TODO add a FileKitInitializer? Remove registry from FileKit?
public actual object FileKit {
    private var _registry: ActivityResultRegistry? = null
    public val registry: ActivityResultRegistry
        get() = _registry
            ?: throw FileKitNotInitializedException()

    private var _context: WeakReference<Context?> = WeakReference(null)
    public val context: Context
        get() = _context.get()
            ?: throw FileKitNotInitializedException()

    public fun init(activity: ComponentActivity) {
        _context = WeakReference(activity.applicationContext)
        _registry = activity.activityResultRegistry
    }

    public fun init(context: Context, registry: ActivityResultRegistry) {
        _context = WeakReference(context)
        _registry = registry
    }
}

public actual val FileKit.filesDir: PlatformFile
    get() = context.filesDir.let(::PlatformFile)

public actual val FileKit.cacheDir: PlatformFile
    get() = context.cacheDir.let(::PlatformFile)
