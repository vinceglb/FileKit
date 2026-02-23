@file:OptIn(FileKitDialogsInternalApi::class)

package io.github.vinceglb.filekit.dialogs

import android.Manifest
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageAndVideo
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.VideoOnly
import androidx.core.net.toUri
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.context
import io.github.vinceglb.filekit.exceptions.FileKitNotInitializedException
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal actual suspend fun FileKit.platformOpenFilePicker(
    type: FileKitType,
    mode: PickerMode,
    directory: PlatformFile?,
    dialogSettings: FileKitDialogSettings,
): Flow<FileKitPickerState<List<PlatformFile>>> {
    val files = callFilePicker(type = type, mode = mode)
    return files.toPickerStateFlow()
}

/**
 * Opens a file saver dialog.
 *
 * @param suggestedName The suggested name for the file.
 * @param extension The file extension (optional).
 * @param directory The initial directory. Supported on desktop platforms.
 * @param dialogSettings Platform-specific settings for the dialog.
 * @return The path where the file should be saved as a [PlatformFile], or null if cancelled.
 */
public actual suspend fun FileKit.openFileSaver(
    suggestedName: String,
    extension: String?,
    directory: PlatformFile?,
    dialogSettings: FileKitDialogSettings,
): PlatformFile? {
    val registry = FileKit.registry
    val normalizedExtension = FileKitAndroidDialogsInternal.normalizeFileSaverExtension(extension)
    val mimeType = FileKitAndroidDialogsInternal.getMimeType(normalizedExtension)
    val contract = ActivityResultContracts.CreateDocument(mimeType)
    val fileName = FileKitAndroidDialogsInternal.buildFileSaverSuggestedName(
        suggestedName = suggestedName,
        extension = normalizedExtension,
    )
    val uri = awaitActivityResult(
        registry = registry,
        contract = contract,
        input = fileName,
    )
    return uri?.let(::PlatformFile)
}

/**
 * Opens a directory picker dialog.
 *
 * @param directory The initial directory. Supported on desktop platforms.
 * @param dialogSettings Platform-specific settings for the dialog.
 * @return The picked directory as a [PlatformFile], or null if cancelled.
 */
public actual suspend fun FileKit.openDirectoryPicker(
    directory: PlatformFile?,
    dialogSettings: FileKitDialogSettings,
): PlatformFile? {
    val registry = FileKit.registry
    val contract = ActivityResultContracts.OpenDocumentTree()
    val initialUri = directory?.path?.toUri()
    val treeUri = awaitActivityResult(
        registry = registry,
        contract = contract,
        input = initialUri,
    )
    return treeUri?.let(::PlatformFile)
}

/**
 * Opens a camera picker dialog.
 *
 * @param type The type of media to capture (Image or Video).
 * @param cameraFacing The camera facing (System, Back or Front).
 * @param destinationFile The file where the captured media will be saved.
 * @param openCameraSettings Platform-specific settings for the camera.
 * @return The saved file as a [PlatformFile], or null if cancelled.
 */
public actual suspend fun FileKit.openCameraPicker(
    type: FileKitCameraType,
    cameraFacing: FileKitCameraFacing,
    destinationFile: PlatformFile,
    openCameraSettings: FileKitOpenCameraSettings,
): PlatformFile? {
    val registry = FileKit.registry
    if (!FileKitAndroidCameraPermissionInternal.requestCameraPermissionIfNeeded(registry, context)) {
        return null
    }

    val contract = TakePictureWithCameraFacing(cameraFacing)
    val uri = destinationFile.toAndroidUri(openCameraSettings.authority)
    val isSaved = try {
        awaitActivityResult(
            registry = registry,
            contract = contract,
            input = uri,
        )
    } catch (_: SecurityException) {
        return null
    }
    return if (isSaved) destinationFile else null
}

@FileKitDialogsInternalApi
public object FileKitAndroidCameraPermissionInternal {
    public fun isPermissionDeclared(
        context: Context,
        permission: String,
    ): Boolean {
        val requestedPermissions = runCatching {
            @Suppress("DEPRECATION")
            context.packageManager
                .getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
                .requestedPermissions
                .orEmpty()
        }.getOrElse {
            return false
        }

        return requestedPermissions.contains(permission)
    }

    public fun needsRuntimeCameraPermission(context: Context): Boolean {
        val cameraPermissionGranted =
            context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        return shouldRequestRuntimeCameraPermission(
            apiLevel = Build.VERSION.SDK_INT,
            isCameraPermissionDeclared = isPermissionDeclared(context, Manifest.permission.CAMERA),
            isCameraPermissionGranted = cameraPermissionGranted,
        )
    }

    public fun shouldRequestRuntimeCameraPermission(
        apiLevel: Int,
        isCameraPermissionDeclared: Boolean,
        isCameraPermissionGranted: Boolean,
    ): Boolean = apiLevel >= Build.VERSION_CODES.M && isCameraPermissionDeclared && !isCameraPermissionGranted

    public suspend fun requestCameraPermissionIfNeeded(
        registry: ActivityResultRegistry,
        context: Context,
    ): Boolean {
        if (!needsRuntimeCameraPermission(context)) {
            return true
        }

        return awaitActivityResult(
            registry = registry,
            contract = ActivityResultContracts.RequestPermission(),
            input = Manifest.permission.CAMERA,
        )
    }
}

/**
 * Contract for taking a picture with camera facing support.
 *
 * Can be used in two ways:
 * 1. With constructor parameter for one-time use (e.g., with suspendCoroutine)
 * 2. With [setCameraFacing] for reusable contracts (e.g., with rememberLauncherForActivityResult)
 */
public class TakePictureWithCameraFacing(
    cameraFacing: FileKitCameraFacing = FileKitCameraFacing.System,
) : ActivityResultContracts.TakePicture() {
    private var currentCameraFacing: FileKitCameraFacing = cameraFacing

    /**
     * Updates the camera facing for the next launch.
     * Useful when reusing the contract with rememberLauncherForActivityResult.
     */
    public fun setCameraFacing(cameraFacing: FileKitCameraFacing) {
        currentCameraFacing = cameraFacing
    }

    override fun createIntent(context: Context, input: Uri): Intent = super.createIntent(context, input).apply {
        if (currentCameraFacing == FileKitCameraFacing.System) {
            return@apply
        }
        applyCameraFacingExtras(currentCameraFacing)
    }

    private fun Intent.applyCameraFacingExtras(cameraFacing: FileKitCameraFacing) {
        val isFront = cameraFacing == FileKitCameraFacing.Front

        // Intent extras adapted from community implementations (expo-image-picker / Flutter).
        // They are undocumented, so we apply them only when the caller explicitly requested a facing.
        if (isFront) {
            // https://github.com/expo/expo/blob/c54eb1e0cbc0f09af9e4308ff76ed9dca457d90e/packages/expo-image-picker/android/src/main/java/expo/modules/imagepicker/contracts/CameraContract.kt#L32
            putExtra("android.intent.extras.LENS_FACING_FRONT", 1)
            putExtra("android.intent.extras.CAMERA_FACING", 1)
            putExtra("android.intent.extra.USE_FRONT_CAMERA", true)

            // Required for Samsung according to https://stackoverflow.com/questions/64263476/android-camera-intent-open-front-camera-instead-of-back-camera
            putExtra("camerafacing", "front")
            putExtra("previous_mode", "front")
        } else {
            // https://github.com/expo/expo/blob/c54eb1e0cbc0f09af9e4308ff76ed9dca457d90e/packages/expo-image-picker/android/src/main/java/expo/modules/imagepicker/contracts/CameraContract.kt#L32
            putExtra("android.intent.extras.LENS_FACING_BACK", 1)
            putExtra("android.intent.extras.CAMERA_FACING", 0)
            putExtra("android.intent.extra.USE_FRONT_CAMERA", false)

            // Required for Samsung according to https://stackoverflow.com/questions/64263476/android-camera-intent-open-front-camera-instead-of-back-camera
            putExtra("camerafacing", "rear")
            putExtra("previous_mode", "rear")
        }
    }
}

/**
 * Shares a file using the Android share sheet.
 *
 * @param file The file to share.
 * @param shareSettings Platform-specific settings for sharing.
 */
public actual suspend fun FileKit.shareFile(
    file: PlatformFile,
    shareSettings: FileKitShareSettings,
) {
    shareFile(
        files = listOf(file),
        shareSettings = shareSettings,
    )
}

/**
 * Shares multiple files using the Android share sheet.
 *
 * @param files The list of files to share.
 * @param shareSettings Platform-specific settings for sharing.
 */
public actual suspend fun FileKit.shareFile(
    files: List<PlatformFile>,
    shareSettings: FileKitShareSettings,
) {
    if (files.isEmpty()) return

    val uris = files.map { platformFile ->
        platformFile.toAndroidUri(shareSettings.authority)
    }

    val mimeTypes = files
        .map { platformFile ->
            FileKitAndroidDialogsInternal.getMimeType(platformFile.extension)
        }.distinct()
        .let { types ->
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
    intentShareSend.flags = FLAG_GRANT_READ_URI_PERMISSION
    val chooseIntent = Intent.createChooser(intentShareSend, null).apply {
        flags = FLAG_ACTIVITY_NEW_TASK
        addFlags(FLAG_GRANT_READ_URI_PERMISSION)
    }
    shareSettings.addOptionChooseIntent(chooseIntent)

    context.startActivity(chooseIntent)
}

/**
 * Opens a file with the default application associated with its file type.
 *
 * @param file The file to open.
 * @param openFileSettings Platform-specific settings for opening the file.
 */
public actual fun FileKit.openFileWithDefaultApplication(
    file: PlatformFile,
    openFileSettings: FileKitOpenFileSettings,
) {
    val uri = file.toAndroidUri(openFileSettings.authority)
    val mimeType = FileKitAndroidDialogsInternal.getMimeType(file.extension)
    val intent = Intent(ACTION_VIEW)
    intent.setDataAndType(uri, mimeType)
    intent.flags = FLAG_GRANT_READ_URI_PERMISSION or FLAG_ACTIVITY_NEW_TASK
    context.startActivity(intent)
}

private suspend fun callFilePicker(
    type: FileKitType,
    mode: PickerMode,
): List<PlatformFile>? {
    val registry = FileKit.registry

    return when (type) {
        FileKitType.Image,
        FileKitType.Video,
        FileKitType.ImageAndVideo,
        -> {
            val request = when (type) {
                FileKitType.Image -> PickVisualMediaRequest(ImageOnly)
                FileKitType.Video -> PickVisualMediaRequest(VideoOnly)
                FileKitType.ImageAndVideo -> PickVisualMediaRequest(ImageAndVideo)
            }

            when {
                mode is PickerMode.Single || (mode is PickerMode.Multiple && mode.maxItems == 1) -> {
                    val contract = PickVisualMedia()
                    awaitActivityResult(
                        registry = registry,
                        contract = contract,
                        input = request,
                    )?.let { listOf(PlatformFile(it)) }
                }

                mode is PickerMode.Multiple -> {
                    val contract = when {
                        mode.maxItems != null -> PickMultipleVisualMedia(mode.maxItems)
                        else -> PickMultipleVisualMedia()
                    }
                    awaitActivityResult(
                        registry = registry,
                        contract = contract,
                        input = request,
                    ).map(::PlatformFile)
                }

                else -> {
                    throw IllegalArgumentException("Unsupported mode: $mode")
                }
            }
        }

        is FileKitType.File -> {
            when (mode) {
                is PickerMode.Single -> {
                    val contract = ActivityResultContracts.OpenDocument()
                    awaitActivityResult(
                        registry = registry,
                        contract = contract,
                        input = FileKitAndroidDialogsInternal.getMimeTypes(type.extensions),
                    )?.let { listOf(PlatformFile(it)) }
                }

                is PickerMode.Multiple -> {
                    // TODO there might be a way to limit the amount of documents, but
                    //  I haven't found it yet.
                    val contract = ActivityResultContracts.OpenMultipleDocuments()
                    awaitActivityResult(
                        registry = registry,
                        contract = contract,
                        input = FileKitAndroidDialogsInternal.getMimeTypes(type.extensions),
                    ).map(::PlatformFile)
                }
            }
        }
    }
}

private suspend fun <I, O> awaitActivityResult(
    registry: ActivityResultRegistry,
    contract: ActivityResultContract<I, O>,
    input: I,
): O = withContext(Dispatchers.Main.immediate) {
    suspendCancellableCoroutine { continuation ->
        var launcher: ActivityResultLauncher<I>? = null
        val isCleanedUp = AtomicBoolean(false)

        fun cleanup() {
            if (isCleanedUp.compareAndSet(false, true)) {
                launcher?.runCatching { unregister() }
            }
        }

        val key = UUID.randomUUID().toString()

        launcher = registry.register(key, contract) { output ->
            cleanup()
            if (continuation.isActive) {
                continuation.resume(output)
            }
        }

        continuation.invokeOnCancellation { cleanup() }

        try {
            launcher.launch(input)
        } catch (error: Throwable) {
            cleanup()
            if (continuation.isActive) {
                continuation.resumeWithException(error)
            }
        }
    }
}

internal object FileKitDialog {
    private var _registry: WeakReference<ActivityResultRegistry?> = WeakReference(null)
    val registry: ActivityResultRegistry
        get() = _registry.get()
            ?: throw FileKitNotInitializedException()

    fun init(registry: ActivityResultRegistry) {
        _registry = WeakReference(registry)
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
