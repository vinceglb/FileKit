package io.github.vinceglb.filekit.dialogs

import android.content.ClipData
import android.content.Intent
import android.provider.DocumentsContract
import android.webkit.MimeTypeMap
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageAndVideo
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.VideoOnly
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import io.github.vinceglb.filekit.AndroidFile
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.context
import io.github.vinceglb.filekit.dialogs.FilePickerResult.FilePickerAborted
import io.github.vinceglb.filekit.dialogs.FilePickerResult.FilePickerCompleted
import io.github.vinceglb.filekit.exceptions.FileKitNotInitializedException
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

public actual suspend fun <Out> FileKit.openFilePicker(
    type: FileKitType,
    mode: FileKitMode<Out>,
    title: String?,
    directory: PlatformFile?,
    dialogSettings: FileKitDialogSettings,
): Out? {
    val picked: List<PlatformFile>? = callFilePicker(type = type, mode = mode)
    return mode.parseResult(picked)
}

public actual suspend fun <Out> FileKit.openFilePickerWithProgressUpdate(
    type: FileKitType,
    mode: FileKitMode<Out>,
    title: String?,
    directory: PlatformFile?,
    dialogSettings: FileKitDialogSettings
): Flow<FilePickerResult> = flow {
    val picked = callFilePicker(type = type, mode = mode)
    if (picked == null) {
        emit(FilePickerAborted)
    } else {
        emit(FilePickerCompleted(picked))
    }
}

public actual suspend fun FileKit.openFileSaver(
    suggestedName: String,
    extension: String?,
    directory: PlatformFile?,
    dialogSettings: FileKitDialogSettings,
): PlatformFile? = withContext(Dispatchers.IO) {
    suspendCoroutine { continuation ->
        // Throw exception if registry is not initialized
        val registry = FileKit.registry

        // It doesn't really matter what the key is, just that it is unique
        val key = UUID.randomUUID().toString()

        // Get MIME type
        val mimeType = getMimeType(extension)

        // Create Launcher
        val contract = ActivityResultContracts.CreateDocument(mimeType)
        val launcher = registry.register(key, contract) { uri ->
            val platformFile = uri?.let { PlatformFile(it) }
            continuation.resume(platformFile)
        }

        // Launch
        launcher.launch("$suggestedName.$extension")
    }
}

public actual suspend fun FileKit.openDirectoryPicker(
    title: String?,
    directory: PlatformFile?,
    dialogSettings: FileKitDialogSettings,
): PlatformFile? = withContext(Dispatchers.IO) {
    // Throw exception if registry is not initialized
    val registry = FileKit.registry

    // It doesn't really matter what the key is, just that it is unique
    val key = UUID.randomUUID().toString()

    suspendCoroutine { continuation ->
        val contract = ActivityResultContracts.OpenDocumentTree()
        val launcher = registry.register(key, contract) { treeUri ->
            val platformDirectory = treeUri?.let {
                // Transform the treeUri to a documentUri
                val documentUri = DocumentsContract.buildDocumentUriUsingTree(
                    treeUri,
                    DocumentsContract.getTreeDocumentId(treeUri)
                )
                PlatformFile(documentUri)
            }
            continuation.resume(platformDirectory)
        }
        val initialUri = directory?.path?.toUri()
        launcher.launch(initialUri)
    }
}

public actual suspend fun FileKit.openCameraPicker(
    type: FileKitCameraType,
    destinationFile: PlatformFile
): PlatformFile? = withContext(Dispatchers.IO) {
    // Throw exception if registry is not initialized
    val registry = FileKit.registry

    // It doesn't really matter what the key is, just that it is unique
    val key = UUID.randomUUID().toString()

    val isSaved = suspendCoroutine { continuation ->
        val contract = ActivityResultContracts.TakePicture()
        val launcher = registry.register(key, contract) { isSaved ->
            continuation.resume(isSaved)
        }
        launcher.launch(destinationFile.uri)
    }

    when (isSaved) {
        true -> destinationFile
        else -> null
    }
}

public actual suspend fun FileKit.shareFile(
    file: PlatformFile,
    shareSettings: FileKitShareSettings
) {
    val uri = when (val androidFile = file.androidFile) {
        is AndroidFile.UriWrapper -> androidFile.uri
        is AndroidFile.FileWrapper -> {
            FileProvider.getUriForFile(context, shareSettings.authority, androidFile.file)
        }
    }
    val mimeType = getMimeType(file.extension)

    // make intent share
    val intentShareSend = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
    }
    intentShareSend.clipData = ClipData.newUri(context.contentResolver, null, uri)
    intentShareSend.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    val chooseIntent = Intent.createChooser(intentShareSend, null).apply {
        setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    shareSettings.addOptionChooseIntent(chooseIntent)

    context.startActivity(chooseIntent)
}

private suspend fun <Out> callFilePicker(
    type: FileKitType,
    mode: FileKitMode<Out>
): List<PlatformFile>? = withContext(Dispatchers.IO) {
    // Throw exception if registry is not initialized
    val registry = FileKit.registry

    // It doesn't really matter what the key is, just that it is unique
    val key = UUID.randomUUID().toString()

    val result: List<PlatformFile>? = suspendCoroutine { continuation ->
        when (type) {
            FileKitType.Image,
            FileKitType.Video,
            FileKitType.ImageAndVideo -> {
                val request = when (type) {
                    FileKitType.Image -> PickVisualMediaRequest(ImageOnly)
                    FileKitType.Video -> PickVisualMediaRequest(VideoOnly)
                    FileKitType.ImageAndVideo -> PickVisualMediaRequest(ImageAndVideo)
                    else -> throw IllegalArgumentException("Unsupported type: $type")
                }

                val launcher = when {
                    mode is FileKitMode.Single || mode is FileKitMode.Multiple && mode.maxItems == 1 -> {
                        val contract = PickVisualMedia()
                        registry.register(key, contract) { uri ->
                            val result = uri?.let { listOf(PlatformFile(it)) }
                            continuation.resume(result)
                        }
                    }

                    mode is FileKitMode.Multiple -> {
                        val contract = when {
                            mode.maxItems != null -> PickMultipleVisualMedia(mode.maxItems)
                            else -> PickMultipleVisualMedia()
                        }
                        registry.register(key, contract) { uri ->
                            val result = uri.map { PlatformFile(it) }
                            continuation.resume(result)
                        }
                    }

                    else -> throw IllegalArgumentException("Unsupported mode: $mode")
                }
                launcher.launch(request)
            }

            is FileKitType.File -> {
                when (mode) {
                    is FileKitMode.Single -> {
                        val contract = ActivityResultContracts.OpenDocument()
                        val launcher = registry.register(key, contract) { uri ->
                            val result = uri?.let { listOf(PlatformFile(it)) }
                            continuation.resume(result)
                        }
                        launcher.launch(getMimeTypes(type.extensions))
                    }

                    is FileKitMode.Multiple -> {
                        // TODO there might be a way to limit the amount of documents, but
                        //  I haven't found it yet.
                        val contract = ActivityResultContracts.OpenMultipleDocuments()
                        val launcher = registry.register(key, contract) { uris ->
                            val result = uris.map { PlatformFile(it) }
                            continuation.resume(result)
                        }
                        launcher.launch(getMimeTypes(type.extensions))
                    }
                }
            }
        }
    }
    result
}

private fun getMimeTypes(fileExtensions: Set<String>?): Array<String> {
    val mimeTypeMap = MimeTypeMap.getSingleton()
    return fileExtensions
        ?.takeIf { it.isNotEmpty() }
        ?.mapNotNull { mimeTypeMap.getMimeTypeFromExtension(it) }
        ?.toTypedArray()
        ?.ifEmpty { arrayOf("*/*") }
        ?: arrayOf("*/*")
}

private fun getMimeType(fileExtension: String?): String {
    if (fileExtension == null) { return "*/*" }
    val mimeTypeMap = MimeTypeMap.getSingleton()
    return mimeTypeMap.getMimeTypeFromExtension(fileExtension) ?: "*/*"
}

internal object FileKitDialog {
    private var _registry: ActivityResultRegistry? = null
    val registry: ActivityResultRegistry
        get() = _registry
            ?: throw FileKitNotInitializedException()

    fun init(registry: ActivityResultRegistry) {
        _registry = registry
    }
}

@Suppress("UnusedReceiverParameter")
internal val FileKit.registry: ActivityResultRegistry
    get() = FileKitDialog.registry

@Suppress("UnusedReceiverParameter")
public fun FileKit.init(registry: ActivityResultRegistry) {
    FileKitDialog.init(registry)
}

@Suppress("UnusedReceiverParameter")
public fun FileKit.init(activity: ComponentActivity) {
    FileKitDialog.init(activity.activityResultRegistry)
}
