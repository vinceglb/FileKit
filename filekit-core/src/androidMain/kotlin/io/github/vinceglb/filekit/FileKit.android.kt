package io.github.vinceglb.filekit

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultRegistry
import java.lang.ref.WeakReference

// TODO add a FileKitInitializer? Remove registry from FileKit?
public actual object FileKit {
    public var registry: ActivityResultRegistry? = null
        private set

    private var _context: WeakReference<Context?> = WeakReference(null)
    public val context: Context?
        get() = _context.get()

    public fun init(activity: ComponentActivity) {
        _context = WeakReference(activity.applicationContext)
        registry = activity.activityResultRegistry
    }

    public fun init(context: Context, registry: ActivityResultRegistry) {
        _context = WeakReference(context)
        FileKit.registry = registry
    }
}
