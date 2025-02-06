package io.github.vinceglb.filekit.initializer

import android.content.Context
import androidx.startup.Initializer
import io.github.vinceglb.filekit.FileKit

@Suppress("unused")
public class FileKitInitializer : Initializer<FileKit> {
    override fun create(context: Context): FileKit =
        FileKit.apply { init(context) }

    override fun dependencies(): List<Class<out Initializer<*>>> =
        emptyList()
}
