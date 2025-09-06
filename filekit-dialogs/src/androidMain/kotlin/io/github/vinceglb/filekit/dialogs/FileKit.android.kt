package io.github.vinceglb.filekit.dialogs

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.hardware.camera2.CameraCharacteristics
import android.net.Uri
import android.os.Build
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
import io.github.vinceglb.filekit.exceptions.FileKitNotInitializedException
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal actual suspend fun FileKit.platformOpenFilePicker(
    type: FileKitType,
    mode: PickerMode,
    title: String?,
    directory: PlatformFile?,
    dialogSettings: FileKitDialogSettings,
): Flow<FileKitPickerState<List<PlatformFile>>> {
    val files = callFilePicker(type = type, mode = mode)
    return files.toPickerStateFlow()
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
    destinationFile: PlatformFile,
    cameraFacing: FileKitCameraFacing
): PlatformFile? = withContext(Dispatchers.IO) {
    // Throw exception if registry is not initialized
    val registry = FileKit.registry

    // It doesn't really matter what the key is, just that it is unique
    val key = UUID.randomUUID().toString()

    val isSaved = suspendCoroutine { continuation ->
        val contract = CustomTakePicture(cameraFacing)
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

public class CustomTakePicture(
    private val cameraFacing: FileKitCameraFacing
) : ActivityResultContracts.TakePicture() {
    override fun createIntent(context: Context, input: Uri): Intent {
        return super.createIntent(context, input).apply {
            val cameraCharacteristic = when (cameraFacing) {
                FileKitCameraFacing.Front -> CameraCharacteristics.LENS_FACING_FRONT
                FileKitCameraFacing.Back -> CameraCharacteristics.LENS_FACING_BACK
            }

            // intent names taken from the flutter codebase because they are known to work and battle-tested
            // https://github.com/flutter/packages/blob/27a2302a3d716e7ee3abbb08e57c5dfa729c9e2e/packages/image_picker/image_picker_android/android/src/main/java/io/flutter/plugins/imagepicker/ImagePickerDelegate.java#L990
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                putExtra("android.intent.extras.CAMERA_FACING", cameraCharacteristic)
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    putExtra("android.intent.extras.USE_FRONT_CAMERA", cameraFacing == FileKitCameraFacing.Front)
                }
            } else {
                if(cameraFacing == FileKitCameraFacing.Front) {
                    // We don't know what the back camera is - is it 0? 2?
                    putExtra("android.intent.extras.CAMERA_FACING", 1)
                }
            }

            // Required for Samsung according to https://stackoverflow.com/questions/64263476/android-camera-intent-open-front-camera-instead-of-back-camera
            val facing = when (cameraFacing) {
                FileKitCameraFacing.Front -> "front"
                FileKitCameraFacing.Back -> "rear"
            }
            putExtra("camerafacing", facing)
            putExtra("previous_mode", facing)
        }
    }
}

public actual suspend fun FileKit.shareFile(
    file: PlatformFile,
    shareSettings: FileKitShareSettings
) {
    shareFile(
        files = listOf(file),
        shareSettings = shareSettings
    )
}


public actual suspend fun FileKit.shareFile(
    files: List<PlatformFile>,
    shareSettings: FileKitShareSettings
) {
    if (files.isEmpty()) return

    val uris = files.map { platformFile ->
        when (val androidFile = platformFile.androidFile) {
            is AndroidFile.UriWrapper -> androidFile.uri
            is AndroidFile.FileWrapper -> FileProvider.getUriForFile(
                context,
                shareSettings.authority,
                androidFile.file
            )
        }
    }

    val mimeTypes = files.map { platformFile ->
        getMimeType(platformFile.extension)
    }.distinct().let { types ->
        if (types.size == 1) types.first() else "*/*"
    }

    // make intent share
    val intentShareSend = Intent().apply {
        action = if (uris.size == 1) Intent.ACTION_SEND else Intent.ACTION_SEND_MULTIPLE
        type = mimeTypes
        if (uris.size == 1) {
            putExtra(Intent.EXTRA_STREAM, uris.first())
        } else {
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
        }
    }

    // Create ClipData with all URIs to ensure proper permissions
    intentShareSend.clipData = if (uris.size == 1) {
        ClipData.newUri(context.contentResolver, null, uris.first())
    } else {
        ClipData.newUri(context.contentResolver, null, uris.first()).apply {
            uris.drop(1).forEach { uri ->
                addItem(ClipData.Item(uri))
            }
        }
    }
    intentShareSend.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    val chooseIntent = Intent.createChooser(intentShareSend, null).apply {
        setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    shareSettings.addOptionChooseIntent(chooseIntent)

    context.startActivity(chooseIntent)
}

public actual fun FileKit.openFileWithDefaultApplication(
    file: PlatformFile,
    openFileSettings: FileKitOpenFileSettings
) {
    val uri = when (val androidFile = file.androidFile) {
        is AndroidFile.UriWrapper -> androidFile.uri
        is AndroidFile.FileWrapper -> FileProvider.getUriForFile(
            context,
            openFileSettings.authority,
            androidFile.file
        )
    }

    val mimeType = getMimeType(file.extension)
    val intent = Intent(ACTION_VIEW)
    intent.setDataAndType(uri, mimeType)
    intent.flags = FLAG_GRANT_READ_URI_PERMISSION or FLAG_ACTIVITY_NEW_TASK
    context.startActivity(intent)
}

private suspend fun callFilePicker(
    type: FileKitType,
    mode: PickerMode
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
                    mode is PickerMode.Single || mode is PickerMode.Multiple && mode.maxItems == 1 -> {
                        val contract = PickVisualMedia()
                        registry.register(key, contract) { uri ->
                            val result = uri?.let { listOf(PlatformFile(it)) }
                            continuation.resume(result)
                        }
                    }

                    mode is PickerMode.Multiple -> {
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
                    is PickerMode.Single -> {
                        val contract = ActivityResultContracts.OpenDocument()
                        val launcher = registry.register(key, contract) { uri ->
                            val result = uri?.let { listOf(PlatformFile(it)) }
                            continuation.resume(result)
                        }
                        launcher.launch(getMimeTypes(type.extensions))
                    }

                    is PickerMode.Multiple -> {
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
        ?.mapNotNull { mimeTypeMap.getMimeTypeFromExtension(it) }
        ?.takeIf { it.isNotEmpty() }
        ?.toTypedArray()
        ?: arrayOf("*/*")
}

private fun getMimeType(fileExtension: String?): String {
    val mimeTypeMap = MimeTypeMap.getSingleton()
    return fileExtension
        ?.let { mimeTypeMap.getMimeTypeFromExtension(it) }
        ?: "*/*"
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
