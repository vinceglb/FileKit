@file:Suppress("ktlint:standard:function-naming", "FunctionName")

package io.github.vinceglb.filekit.dialogs.platform.windows

import com.sun.jna.platform.win32.WinNT.HRESULT
import io.github.vinceglb.filekit.dialogs.platform.windows.jna.IFileSaveDialog
import java.lang.reflect.Proxy
import kotlin.test.Test
import kotlin.test.assertEquals

class WindowsFileSaverConfigurationTest {
    @Test
    fun WindowsFileSaver_configureWithTitle_setsDialogTitle() {
        val dialog = RecordingFileSaveDialog()

        dialog.proxy.configureFileSaver(
            suggestedName = "filename",
            defaultExtension = "txt",
            allowedExtensions = null,
            title = "My custom title",
        )

        assertEquals(listOf("My custom title"), dialog.stringArgumentsOf("SetTitle"))
        assertEquals(listOf("filename"), dialog.stringArgumentsOf("SetFileName"))
        assertEquals(listOf("txt"), dialog.stringArgumentsOf("SetDefaultExtension"))
    }

    @Test
    fun WindowsFileSaver_configureWithoutTitle_keepsDefaultTitle() {
        val dialog = RecordingFileSaveDialog()

        dialog.proxy.configureFileSaver(
            suggestedName = "filename",
            defaultExtension = "txt",
            allowedExtensions = null,
            title = null,
        )

        assertEquals(emptyList(), dialog.stringArgumentsOf("SetTitle"))
    }
}

private class RecordingFileSaveDialog {
    private val calls = mutableListOf<Pair<String, List<Any?>>>()

    val proxy: IFileSaveDialog = Proxy.newProxyInstance(
        IFileSaveDialog::class.java.classLoader,
        arrayOf(IFileSaveDialog::class.java),
    ) { proxy, method, args ->
        when (method.name) {
            "toString" -> "RecordingFileSaveDialog"
            "hashCode" -> System.identityHashCode(proxy)
            "equals" -> proxy === args?.firstOrNull()
            else -> HRESULT(0).also { calls += method.name to args.orEmpty().toList() }
        }
    } as IFileSaveDialog

    fun stringArgumentsOf(methodName: String): List<String> = calls
        .filter { (name, _) -> name == methodName }
        .map { (_, args) -> args.single().toString() }
}
