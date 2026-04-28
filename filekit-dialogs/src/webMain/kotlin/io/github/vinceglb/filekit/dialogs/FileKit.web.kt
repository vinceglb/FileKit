package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.FileExt
import io.github.vinceglb.filekit.FileHandleFile
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.WebFileHandle
import io.github.vinceglb.filekit.toPlatformFile
import kotlinx.coroutines.flow.Flow
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.time.Instant

@OptIn(ExperimentalWasmJsInterop::class)
internal actual suspend fun FileKit.platformOpenFilePicker(
    type: FileKitType,
    mode: PickerMode,
    directory: PlatformFile?,
    dialogSettings: FileKitDialogSettings,
): Flow<FileKitPickerState<List<PlatformFile>>> =
    platformOpenFilePickerWeb(
        type = type,
        multipleMode = mode is PickerMode.Multiple,
        directoryMode = false,
    )?.map { it.toPlatformFile() }.toPickerStateFlow()

public actual suspend fun FileKit.openDirectoryPicker(
    directory: PlatformFile?,
    dialogSettings: FileKitDialogSettings,
): PlatformFile? {
    val fileList = platformOpenFilePickerWeb(
        type = FileKitType.File(),
        multipleMode = true,
        directoryMode = true,
    )
    if (fileList.isNullOrEmpty()) {
        // Unfortunately, when the fileList is empty (Empty directory selected), we do not know the directory name.
        return null
    } else {
        val directoryName = fileList.first().path.substringBefore('/')
        val rootDirectory = FileHandleVirtualDirectory(
            name = directoryName,
            path = directoryName,
            lastModified = Instant.fromEpochMilliseconds(0),
            parent = null,
            list = mutableListOf(),
        )
        fileList.forEach { file ->
            val pathOnlyPart = file.path.substringBeforeLast(delimiter = '/', missingDelimiterValue = "") // Exclude the file name
            val directory = rootDirectory.findOrCreateRelativeDirectory(pathOnlyPart)
            val file = FileHandleFile(file = file.getFile(), parent = directory)
            directory.list.add(file)
        }
        return PlatformFile(rootDirectory)
    }
}

private fun FileHandleVirtualDirectory.findOrCreateRelativeDirectory(absolutePath: String): FileHandleVirtualDirectory {
    val self = this
    val childPath = absolutePath.substringAfter('/', "")
    return when {
        childPath.isBlank() -> {
            self
        }

        childPath.contains('/') -> {
            findOrCreateRelativeDirectory(childPath)
        }

        else -> {
            val dirName = childPath
            list.filterIsInstance<FileHandleVirtualDirectory>().find { item ->
                item.name == dirName
            } ?: FileHandleVirtualDirectory(
                name = dirName,
                path = "${self.path}/$dirName",
                lastModified = Instant.fromEpochMilliseconds(0),
                parent = self,
                list = mutableListOf(),
            ).also { newDir ->
                list.add(newDir)
            }
        }
    }
}

internal expect suspend fun platformOpenFilePickerWeb(
    type: FileKitType,
    multipleMode: Boolean, // select multiple files
    directoryMode: Boolean, // select a directory
): List<FileHandleFile>?

/**
 * Virtual directory
 */
internal class FileHandleVirtualDirectory(
    override val name: String,
    override val path: String,
    override val lastModified: Instant,
    val parent: FileHandleVirtualDirectory?,
    val list: MutableList<WebFileHandle>,
) : WebFileHandle {
    override val type: String = ""
    override val size: Long = 0
    override val isDirectory: Boolean = true
    override val isRegularFile: Boolean = false

    override fun getFile(): FileExt {
        TODO("Not supported!")
    }

    override fun getParent(): WebFileHandle? =
        parent

    override fun list(): List<WebFileHandle> =
        list
}
