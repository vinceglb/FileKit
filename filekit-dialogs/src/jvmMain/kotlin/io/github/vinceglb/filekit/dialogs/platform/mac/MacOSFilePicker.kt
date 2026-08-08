package io.github.vinceglb.filekit.dialogs.platform.mac

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogException
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.FileKitMacOSSettings
import io.github.vinceglb.filekit.dialogs.FileKitPickerException
import io.github.vinceglb.filekit.dialogs.MACOS_DIRECTORY_PICKER_FAILURE_MESSAGE
import io.github.vinceglb.filekit.dialogs.MACOS_FILE_PICKER_FAILURE_MESSAGE
import io.github.vinceglb.filekit.dialogs.MACOS_FILE_SAVER_FAILURE_MESSAGE
import io.github.vinceglb.filekit.dialogs.buildFileSaverAllowedFileTypes
import io.github.vinceglb.filekit.dialogs.platform.PlatformFilePicker
import io.github.vinceglb.filekit.dialogs.platform.mac.foundation.Foundation
import io.github.vinceglb.filekit.dialogs.platform.mac.foundation.FoundationRunnableBootstrapException
import io.github.vinceglb.filekit.dialogs.platform.mac.foundation.ID
import io.github.vinceglb.filekit.dialogs.requireMacOSCompatible
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

internal class MacOSFilePicker : PlatformFilePicker {
    override suspend fun openFilePicker(
        fileExtensions: Set<String>?,
        directory: PlatformFile?,
        dialogSettings: FileKitDialogSettings,
    ): File? {
        dialogSettings.parent.requireMacOSCompatible()
        return callNativeMacOSPicker(
            mode = MacOSFilePickerMode.SingleFile,
            directory = directory,
            fileExtensions = fileExtensions,
            title = dialogSettings.title,
            macOSSettings = dialogSettings.macOS,
        )
    }

    override suspend fun openFilesPicker(
        fileExtensions: Set<String>?,
        directory: PlatformFile?,
        dialogSettings: FileKitDialogSettings,
    ): List<File>? {
        dialogSettings.parent.requireMacOSCompatible()
        return callNativeMacOSPicker(
            mode = MacOSFilePickerMode.MultipleFiles,
            directory = directory,
            fileExtensions = fileExtensions,
            title = dialogSettings.title,
            macOSSettings = dialogSettings.macOS,
        )
    }

    override suspend fun openDirectoryPicker(
        directory: PlatformFile?,
        dialogSettings: FileKitDialogSettings,
    ): File? {
        dialogSettings.parent.requireMacOSCompatible()
        return callNativeMacOSPicker(
            mode = MacOSFilePickerMode.Directories,
            directory = directory,
            fileExtensions = null,
            title = dialogSettings.title,
            macOSSettings = dialogSettings.macOS,
        )
    }

    override suspend fun openFileSaver(
        suggestedName: String,
        defaultExtension: String?,
        allowedExtensions: Set<String>?,
        directory: PlatformFile?,
        dialogSettings: FileKitDialogSettings,
    ): File? = withContext(Dispatchers.IO) {
        dialogSettings.parent.requireMacOSCompatible()
        val pool = Foundation.NSAutoreleasePool()
        try {
            var response: File? = null
            var modalFailure: FileKitDialogException? = null

            normalizeRunnableBootstrapFailure(
                operationalFailure = { cause ->
                    FileKitDialogException(MACOS_FILE_SAVER_FAILURE_MESSAGE, cause)
                },
            ) {
                Foundation.executeOnMainThread(
                    withAutoreleasePool = false,
                    waitUntilDone = true,
                ) {
                    val savePanel = Foundation.invoke("NSSavePanel", "new")

                    dialogSettings.title?.let {
                        Foundation.invoke(savePanel, "setMessage:", Foundation.nsString(it))
                    }

                    directory?.let {
                        Foundation.invoke(savePanel, "setDirectoryURL:", Foundation.nsURL(it.path))
                    }

                    // Set the file name without extension, NSSavePanel appends it from allowedFileTypes
                    Foundation.invoke(
                        savePanel,
                        "setNameFieldStringValue:",
                        Foundation.nsString(suggestedName),
                    )

                    // Default extension first so it is the one appended
                    val fileTypes = buildFileSaverAllowedFileTypes(defaultExtension, allowedExtensions)
                    savePanel.setAllowedFileTypes(fileTypes)

                    Foundation.invoke(
                        savePanel,
                        "setCanCreateDirectories:",
                        dialogSettings.macOS.canCreateDirectories,
                    )

                    val result = Foundation.invoke(savePanel, "runModal")
                    when (result.toInt()) {
                        NS_MODAL_RESPONSE_OK -> response = singlePath(savePanel)
                        NS_MODAL_RESPONSE_CANCEL -> Unit
                        else -> modalFailure = FileKitDialogException(MACOS_FILE_SAVER_FAILURE_MESSAGE)
                    }
                }
            }

            modalFailure?.let { throw it }
            response
        } finally {
            pool.drain()
        }
    }

    private suspend fun <T> callNativeMacOSPicker(
        mode: MacOSFilePickerMode<T>,
        directory: PlatformFile?,
        fileExtensions: Set<String>?,
        title: String?,
        macOSSettings: FileKitMacOSSettings,
    ): T? = withContext(Dispatchers.IO) {
        val pool = Foundation.NSAutoreleasePool()
        try {
            var response: T? = null
            var modalFailure: FileKitDialogException? = null

            normalizeRunnableBootstrapFailure(mode::operationalFailure) {
                Foundation.executeOnMainThread(
                    withAutoreleasePool = false,
                    waitUntilDone = true,
                ) {
                    // Create the file picker
                    val openPanel = Foundation.invoke("NSOpenPanel", "new")

                    // Setup single, multiple selection or directory mode
                    mode.setupPickerMode(openPanel, macOSSettings.canCreateDirectories)

                    // Set the title
                    title?.let {
                        Foundation.invoke(openPanel, "setMessage:", Foundation.nsString(it))
                    }

                    // Set initial directory
                    directory?.let {
                        Foundation.invoke(openPanel, "setDirectoryURL:", Foundation.nsURL(it.path))
                    }

                    // Set file extensions
                    openPanel.setAllowedFileTypes(fileExtensions)

                    // Set resolvesAliases
                    macOSSettings.resolvesAliases?.let { resolvesAliases ->
                        Foundation.invoke(openPanel, "setResolvesAliases:", resolvesAliases)
                    }

                    // Open the file picker
                    val result = Foundation.invoke(openPanel, "runModal")

                    when (result.toInt()) {
                        NS_MODAL_RESPONSE_OK -> response = mode.getResult(openPanel)
                        NS_MODAL_RESPONSE_CANCEL -> Unit
                        else -> modalFailure = mode.operationalFailure()
                    }
                }
            }

            modalFailure?.let { throw it }
            response
        } finally {
            pool.drain()
        }
    }

    private companion object {
        const val NS_MODAL_RESPONSE_OK = 1
        const val NS_MODAL_RESPONSE_CANCEL = 0

        fun Collection<String>.toNsStringArray(): ID? {
            if (isEmpty()) {
                return null
            }

            val items = map { Foundation.nsString(it) }
            return Foundation.invokeVarArg(
                "NSArray",
                "arrayWithObjects:",
                *items.toTypedArray(),
            )
        }

        fun ID.setAllowedFileTypes(extensions: Collection<String>?) {
            val fileTypes = extensions?.toNsStringArray() ?: return
            Foundation.invoke(this, "setAllowedFileTypes:", fileTypes)
        }

        fun singlePath(openPanel: ID): File? {
            val url = Foundation.invoke(openPanel, "URL")
            if (Foundation.isNil(url)) {
                return null
            }

            val nsPath = Foundation.invoke(url, "path")
            if (Foundation.isNil(nsPath)) {
                return null
            }

            val path = Foundation.toStringViaUTF8(nsPath)
            return path?.let { File(it) }
        }

        fun multiplePaths(openPanel: ID): List<File>? {
            val urls = Foundation.invoke(openPanel, "URLs")
            val urlCount = Foundation.invoke(urls, "count").toInt()

            return (0 until urlCount)
                .mapNotNull { index ->
                    val url = Foundation.invoke(urls, "objectAtIndex:", index)
                    val nsPath = Foundation.invoke(url, "path")
                    val path = Foundation.toStringViaUTF8(nsPath)
                    path?.let { File(it) }
                }.ifEmpty { null }
        }
    }

    private sealed class MacOSFilePickerMode<T> {
        abstract fun setupPickerMode(openPanel: ID, canCreateDirectories: Boolean)

        abstract fun getResult(openPanel: ID): T?

        abstract fun operationalFailure(): FileKitDialogException

        abstract fun operationalFailure(cause: Throwable): FileKitDialogException

        data object SingleFile : MacOSFilePickerMode<File?>() {
            override fun setupPickerMode(openPanel: ID, canCreateDirectories: Boolean) {
                Foundation.invoke(openPanel, "setCanChooseFiles:", true)
                Foundation.invoke(openPanel, "setCanChooseDirectories:", false)
                Foundation.invoke(openPanel, "setCanCreateDirectories:", canCreateDirectories)
            }

            override fun getResult(openPanel: ID): File? = singlePath(openPanel)

            override fun operationalFailure(): FileKitDialogException = FileKitPickerException(
                MACOS_FILE_PICKER_FAILURE_MESSAGE,
            )

            override fun operationalFailure(cause: Throwable): FileKitDialogException = FileKitPickerException(
                MACOS_FILE_PICKER_FAILURE_MESSAGE,
                cause,
            )
        }

        data object MultipleFiles : MacOSFilePickerMode<List<File>>() {
            override fun setupPickerMode(openPanel: ID, canCreateDirectories: Boolean) {
                Foundation.invoke(openPanel, "setCanChooseFiles:", true)
                Foundation.invoke(openPanel, "setCanChooseDirectories:", false)
                Foundation.invoke(openPanel, "setAllowsMultipleSelection:", true)
                Foundation.invoke(openPanel, "setCanCreateDirectories:", canCreateDirectories)
                // MaxItems is not supported by MacOSFilePicker
            }

            override fun getResult(openPanel: ID): List<File>? = multiplePaths(openPanel)

            override fun operationalFailure(): FileKitDialogException = FileKitPickerException(
                MACOS_FILE_PICKER_FAILURE_MESSAGE,
            )

            override fun operationalFailure(cause: Throwable): FileKitDialogException = FileKitPickerException(
                MACOS_FILE_PICKER_FAILURE_MESSAGE,
                cause,
            )
        }

        data object Directories : MacOSFilePickerMode<File>() {
            override fun setupPickerMode(openPanel: ID, canCreateDirectories: Boolean) {
                Foundation.invoke(openPanel, "setCanChooseFiles:", false)
                Foundation.invoke(openPanel, "setCanChooseDirectories:", true)
                Foundation.invoke(openPanel, "setCanCreateDirectories:", canCreateDirectories)
            }

            override fun getResult(openPanel: ID): File? = singlePath(openPanel)

            override fun operationalFailure(): FileKitDialogException = FileKitDialogException(
                MACOS_DIRECTORY_PICKER_FAILURE_MESSAGE,
            )

            override fun operationalFailure(cause: Throwable): FileKitDialogException = FileKitDialogException(
                MACOS_DIRECTORY_PICKER_FAILURE_MESSAGE,
                cause,
            )
        }
    }
}

internal inline fun <T> normalizeRunnableBootstrapFailure(
    operationalFailure: (FoundationRunnableBootstrapException) -> FileKitDialogException,
    operation: () -> T,
): T = try {
    operation()
} catch (cause: FoundationRunnableBootstrapException) {
    throw operationalFailure(cause)
}
