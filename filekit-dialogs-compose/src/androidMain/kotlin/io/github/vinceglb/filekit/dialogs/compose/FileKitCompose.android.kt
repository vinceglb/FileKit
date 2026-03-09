@file:OptIn(io.github.vinceglb.filekit.dialogs.FileKitDialogsInternalApi::class)

package io.github.vinceglb.filekit.dialogs.compose

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageAndVideo
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.VideoOnly
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.core.net.toUri
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitAndroidCameraPermissionInternal
import io.github.vinceglb.filekit.dialogs.FileKitAndroidDialogsInternal
import io.github.vinceglb.filekit.dialogs.FileKitCameraFacing
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitOpenCameraSettings
import io.github.vinceglb.filekit.dialogs.FileKitPickerState
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.TakePictureWithCameraFacing
import io.github.vinceglb.filekit.dialogs.init
import io.github.vinceglb.filekit.dialogs.toAndroidUri
import io.github.vinceglb.filekit.path

internal const val PICKER_MODE_SINGLE = "single"
internal const val PICKER_MODE_MULTIPLE = "multiple"
internal const val PICKER_MODE_SINGLE_WITH_STATE = "single_with_state"
internal const val PICKER_MODE_MULTIPLE_WITH_STATE = "multiple_with_state"

private const val LAUNCHER_VISUAL_SINGLE = "visual_single"
private const val LAUNCHER_VISUAL_MULTIPLE = "visual_multiple"
private const val LAUNCHER_FILE_SINGLE = "file_single"
private const val LAUNCHER_FILE_MULTIPLE = "file_multiple"

@Composable
internal actual fun InitFileKit() {
    if (!LocalInspectionMode.current) {
        val registry = LocalActivityResultRegistryOwner.current?.activityResultRegistry

        // if null then MainActivity is not an Activity that implements ActivityResultRegistryOwner e.g. ComponentActivity
        // This should not generally happen
        // Calls to launcher should fail with FileKitNotInitializedException if it wasn't previously initialized
        LaunchedEffect(registry) {
            if (registry != null) {
                FileKit.init(registry)
            }
        }
    }
}

@Composable
internal actual fun <PickerResult, ConsumedResult> rememberPlatformFilePickerLauncher(
    type: FileKitType,
    mode: FileKitMode<PickerResult, ConsumedResult>,
    directory: PlatformFile?,
    dialogSettings: FileKitDialogSettings,
    onResult: (ConsumedResult) -> Unit,
): PickerResultLauncher {
    val currentType by rememberUpdatedState(type)
    val currentMode by rememberUpdatedState(mode)
    val currentOnConsumed by rememberUpdatedState(onResult)

    var pendingModeId by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingMaxItems by rememberSaveable { mutableStateOf<Int?>(null) }
    var pendingLauncherId by rememberSaveable { mutableStateOf<String?>(null) }

    fun dispatchPendingResult(launcherId: String, files: List<PlatformFile>?) {
        dispatchPendingPickerResult(
            expectedLauncherId = launcherId,
            pendingLauncherId = pendingLauncherId,
            pendingModeId = pendingModeId,
            pendingMaxItems = pendingMaxItems,
            files = files,
            clearPendingState = {
                pendingModeId = null
                pendingMaxItems = null
                pendingLauncherId = null
            },
            onConsumed = { consumed ->
                @Suppress("UNCHECKED_CAST")
                currentOnConsumed(consumed as ConsumedResult)
            },
        )
    }

    fun dispatchCancelledResult(launcherId: String) {
        pendingLauncherId = launcherId
        dispatchPendingResult(
            launcherId = launcherId,
            files = null,
        )
    }

    val visualSingleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        dispatchPendingResult(
            launcherId = LAUNCHER_VISUAL_SINGLE,
            files = uri?.let { listOf(PlatformFile(it)) },
        )
    }

    val visualMultipleLauncher = rememberLauncherForActivityResult(DynamicPickMultipleVisualMediaContract()) { uris ->
        dispatchPendingResult(
            launcherId = LAUNCHER_VISUAL_MULTIPLE,
            files = uris.map(::PlatformFile),
        )
    }

    val fileSingleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        dispatchPendingResult(
            launcherId = LAUNCHER_FILE_SINGLE,
            files = uri?.let { listOf(PlatformFile(it)) },
        )
    }

    val fileMultipleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        dispatchPendingResult(
            launcherId = LAUNCHER_FILE_MULTIPLE,
            files = uris.map(::PlatformFile),
        )
    }

    return remember(
        visualSingleLauncher,
        visualMultipleLauncher,
        fileSingleLauncher,
        fileMultipleLauncher,
    ) {
        PickerResultLauncher {
            val modeSnapshot = currentMode.toPendingModeSnapshot()
            pendingModeId = modeSnapshot.modeId
            pendingMaxItems = modeSnapshot.maxItems

            when (val pickerType = currentType) {
                FileKitType.Image,
                FileKitType.Video,
                FileKitType.ImageAndVideo,
                -> {
                    val fallbackMimeTypes = pickerType.toVisualFallbackMimeTypes()
                    val request = when (pickerType) {
                        FileKitType.Image -> PickVisualMediaRequest(ImageOnly)
                        FileKitType.Video -> PickVisualMediaRequest(VideoOnly)
                        FileKitType.ImageAndVideo -> PickVisualMediaRequest(ImageAndVideo)
                    }

                    when {
                        shouldUseSingleVisualLauncher(
                            modeId = modeSnapshot.modeId,
                            maxItems = modeSnapshot.maxItems,
                        ) -> {
                            when (
                                resolvePickerLaunchOutcome(
                                    launchPrimary = {
                                        pendingLauncherId = LAUNCHER_VISUAL_SINGLE
                                        launchPickerSafely {
                                            visualSingleLauncher.launch(request)
                                        }
                                    },
                                    launchFallback = {
                                        pendingLauncherId = LAUNCHER_FILE_SINGLE
                                        launchPickerSafely {
                                            fileSingleLauncher.launch(fallbackMimeTypes)
                                        }
                                    },
                                )
                            ) {
                                PickerLaunchOutcome.PrimaryLaunched,
                                PickerLaunchOutcome.FallbackLaunched,
                                -> {
                                    Unit
                                }

                                PickerLaunchOutcome.Cancelled -> {
                                    dispatchCancelledResult(LAUNCHER_FILE_SINGLE)
                                }
                            }
                        }

                        else -> {
                            when (
                                resolvePickerLaunchOutcome(
                                    launchPrimary = {
                                        pendingLauncherId = LAUNCHER_VISUAL_MULTIPLE
                                        launchPickerSafely {
                                            visualMultipleLauncher.launch(
                                                DynamicPickMultipleVisualMediaInput(
                                                    request = request,
                                                    maxItems = modeSnapshot.maxItems,
                                                ),
                                            )
                                        }
                                    },
                                    launchFallback = {
                                        pendingLauncherId = LAUNCHER_FILE_MULTIPLE
                                        launchPickerSafely {
                                            fileMultipleLauncher.launch(fallbackMimeTypes)
                                        }
                                    },
                                )
                            ) {
                                PickerLaunchOutcome.PrimaryLaunched,
                                PickerLaunchOutcome.FallbackLaunched,
                                -> {
                                    Unit
                                }

                                PickerLaunchOutcome.Cancelled -> {
                                    dispatchCancelledResult(LAUNCHER_FILE_MULTIPLE)
                                }
                            }
                        }
                    }
                }

                is FileKitType.File -> {
                    val mimeTypes = FileKitAndroidDialogsInternal.getMimeTypes(pickerType.extensions)
                    when {
                        modeSnapshot.isSingleMode() -> {
                            pendingLauncherId = LAUNCHER_FILE_SINGLE
                            val isLaunched = launchPickerSafely {
                                fileSingleLauncher.launch(mimeTypes)
                            }
                            if (!isLaunched) {
                                dispatchCancelledResult(LAUNCHER_FILE_SINGLE)
                            }
                        }

                        else -> {
                            pendingLauncherId = LAUNCHER_FILE_MULTIPLE
                            val isLaunched = launchPickerSafely {
                                fileMultipleLauncher.launch(mimeTypes)
                            }
                            if (!isLaunched) {
                                dispatchCancelledResult(LAUNCHER_FILE_MULTIPLE)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Creates and remembers a [PickerResultLauncher] for picking a directory.
 *
 * @param directory The initial directory. Supported on desktop platforms.
 * @param dialogSettings Platform-specific settings for the dialog.
 * @param onResult Callback invoked with the picked directory, or null if cancelled.
 * @return A [PickerResultLauncher] that can be used to launch the picker.
 */
@Composable
public actual fun rememberDirectoryPickerLauncher(
    directory: PlatformFile?,
    dialogSettings: FileKitDialogSettings,
    onResult: (PlatformFile?) -> Unit,
): PickerResultLauncher {
    // Init FileKit
    InitFileKit()

    val currentOnResult by rememberUpdatedState(onResult)
    val currentDirectory by rememberUpdatedState(directory)

    var hasPendingLaunch by rememberSaveable { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { treeUri ->
        if (!hasPendingLaunch) return@rememberLauncherForActivityResult

        hasPendingLaunch = false
        val platformDirectory = treeUri?.let(::PlatformFile)
        currentOnResult(platformDirectory)
    }

    return remember(launcher) {
        PickerResultLauncher {
            val initialUri = currentDirectory?.path?.toUri()
            hasPendingLaunch = true
            val isLaunched = launchPickerSafely {
                launcher.launch(initialUri)
            }
            if (!isLaunched) {
                hasPendingLaunch = false
                currentOnResult(null)
            }
        }
    }
}

@Composable
internal actual fun rememberPlatformFileSaverLauncher(
    dialogSettings: FileKitDialogSettings,
    onResult: (PlatformFile?) -> Unit,
): SaverResultLauncher {
    val currentOnResult by rememberUpdatedState(onResult)

    var hasPendingLaunch by rememberSaveable { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(CreateDocumentDynamicContract()) { uri ->
        if (!hasPendingLaunch) return@rememberLauncherForActivityResult

        hasPendingLaunch = false
        currentOnResult(uri?.let(::PlatformFile))
    }

    return remember(launcher) {
        SaverResultLauncher { suggestedName, extension, directory ->
            val normalizedExtension = FileKitAndroidDialogsInternal.normalizeFileSaverExtension(extension)
            val fileName = FileKitAndroidDialogsInternal.buildFileSaverSuggestedName(
                suggestedName = suggestedName,
                extension = normalizedExtension,
            )
            val mimeType = FileKitAndroidDialogsInternal.getMimeType(normalizedExtension)

            hasPendingLaunch = true
            launcher.launch(
                CreateDocumentInput(
                    mimeType = mimeType,
                    fileName = fileName,
                ),
            )
        }
    }
}

/**
 * Creates and remembers a [PhotoResultLauncher] for taking a picture or video with the camera.
 *
 * @param openCameraSettings Platform-specific settings for the camera.
 * @param onResult Callback invoked with the saved file, or null if cancelled.
 * @return A [PhotoResultLauncher] that can be used to launch the camera.
 */
@Composable
public actual fun rememberCameraPickerLauncher(
    openCameraSettings: FileKitOpenCameraSettings,
    onResult: (PlatformFile?) -> Unit,
): PhotoResultLauncher {
    // Init FileKit
    InitFileKit()

    // Store the destination file URI string to survive process death.
    // If the user launches again before a callback, latest launch wins.
    var pendingDestinationUri by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingCameraFacingName by rememberSaveable { mutableStateOf(FileKitCameraFacing.System.name) }
    var hasPendingPermissionRequest by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current

    // Updated callback
    val currentOnResult by rememberUpdatedState(onResult)

    // Create a stable contract instance (reused across recompositions)
    val contract = remember { TakePictureWithCameraFacing() }

    // Create the launcher using the Activity Result API
    val launcher = rememberLauncherForActivityResult(contract) { success ->
        val pendingUri = pendingDestinationUri ?: return@rememberLauncherForActivityResult
        pendingDestinationUri = null
        currentOnResult(resolveCameraResult(success, pendingUri))
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { permissionGranted ->
            if (!hasPendingPermissionRequest) return@rememberLauncherForActivityResult
            hasPendingPermissionRequest = false

            when (
                val resolution = resolveCameraPermissionResult(
                    permissionGranted = permissionGranted,
                    pendingDestinationUri = pendingDestinationUri,
                )
            ) {
                CameraPermissionResolution.NoOp -> {
                    Unit
                }

                CameraPermissionResolution.ReturnNullResult -> {
                    pendingDestinationUri = null
                    currentOnResult(null)
                }

                is CameraPermissionResolution.LaunchCamera -> {
                    val cameraFacing = runCatching {
                        FileKitCameraFacing.valueOf(pendingCameraFacingName)
                    }.getOrDefault(FileKitCameraFacing.System)

                    contract.setCameraFacing(cameraFacing)
                    val isLaunched = launchCameraSafely(
                        uri = resolution.uri,
                        launch = launcher::launch,
                    )
                    if (!isLaunched) {
                        pendingDestinationUri = null
                        currentOnResult(null)
                    }
                }
            }
        }

    // Return the PhotoResultLauncher wrapper
    return remember(launcher, permissionLauncher, contract, context) {
        PhotoResultLauncher { _, cameraFacing, destinationFile ->
            // Store the destination URI for retrieval after potential activity recreation
            val uri = destinationFile.toAndroidUri(openCameraSettings.authority)
            pendingDestinationUri = uri.toString()
            pendingCameraFacingName = cameraFacing.name

            if (FileKitAndroidCameraPermissionInternal.needsRuntimeCameraPermission(context)) {
                hasPendingPermissionRequest = true
                permissionLauncher.launch(Manifest.permission.CAMERA)
                return@PhotoResultLauncher
            }

            // Set the camera facing on the contract before launching
            contract.setCameraFacing(cameraFacing)

            // Launch the camera
            val isLaunched = launchCameraSafely(
                uri = uri,
                launch = launcher::launch,
            )
            if (!isLaunched) {
                pendingDestinationUri = null
                currentOnResult(null)
            }
        }
    }
}

internal sealed interface CameraPermissionResolution {
    data object NoOp : CameraPermissionResolution

    data object ReturnNullResult : CameraPermissionResolution

    data class LaunchCamera(
        val uri: Uri,
    ) : CameraPermissionResolution
}

internal fun resolveCameraPermissionResult(
    permissionGranted: Boolean,
    pendingDestinationUri: String?,
): CameraPermissionResolution {
    if (!permissionGranted) return CameraPermissionResolution.ReturnNullResult

    val pendingUri = pendingDestinationUri ?: return CameraPermissionResolution.NoOp
    return CameraPermissionResolution.LaunchCamera(pendingUri.toUri())
}

internal fun launchCameraSafely(
    uri: Uri,
    launch: (Uri) -> Unit,
): Boolean = try {
    launch(uri)
    true
} catch (_: SecurityException) {
    false
}

internal fun launchPickerSafely(
    launch: () -> Unit,
): Boolean = try {
    launch()
    true
} catch (_: ActivityNotFoundException) {
    false
}

internal enum class PickerLaunchOutcome {
    PrimaryLaunched,
    FallbackLaunched,
    Cancelled,
}

internal fun resolvePickerLaunchOutcome(
    launchPrimary: () -> Boolean,
    launchFallback: () -> Boolean,
): PickerLaunchOutcome = when {
    launchPrimary() -> PickerLaunchOutcome.PrimaryLaunched
    launchFallback() -> PickerLaunchOutcome.FallbackLaunched
    else -> PickerLaunchOutcome.Cancelled
}

internal fun resolveCameraResult(
    success: Boolean,
    pendingDestinationUri: String?,
): PlatformFile? {
    val uri = pendingDestinationUri ?: return null
    return if (success) PlatformFile(uri.toUri()) else null
}

private data class PendingModeSnapshot(
    val modeId: String,
    val maxItems: Int?,
)

private fun PendingModeSnapshot.isSingleMode(): Boolean =
    modeId == PICKER_MODE_SINGLE || modeId == PICKER_MODE_SINGLE_WITH_STATE

internal fun FileKitType.toVisualFallbackMimeTypes(): Array<String> = when (this) {
    FileKitType.Image -> arrayOf("image/*")
    FileKitType.Video -> arrayOf("video/*")
    FileKitType.ImageAndVideo -> arrayOf("image/*", "video/*")
    is FileKitType.File -> error("File type does not use visual fallback MIME types")
}

internal fun shouldUseSingleVisualLauncher(
    modeId: String,
    maxItems: Int?,
): Boolean = when (modeId) {
    PICKER_MODE_SINGLE,
    PICKER_MODE_SINGLE_WITH_STATE,
    -> {
        true
    }

    PICKER_MODE_MULTIPLE,
    PICKER_MODE_MULTIPLE_WITH_STATE,
    -> {
        maxItems == 1
    }

    else -> {
        false
    }
}

private fun <PickerResult, ConsumedResult> FileKitMode<PickerResult, ConsumedResult>.toPendingModeSnapshot(): PendingModeSnapshot =
    when (this) {
        FileKitMode.Single -> PendingModeSnapshot(PICKER_MODE_SINGLE, null)
        is FileKitMode.Multiple -> PendingModeSnapshot(PICKER_MODE_MULTIPLE, maxItems)
        FileKitMode.SingleWithState -> PendingModeSnapshot(PICKER_MODE_SINGLE_WITH_STATE, null)
        is FileKitMode.MultipleWithState -> PendingModeSnapshot(PICKER_MODE_MULTIPLE_WITH_STATE, maxItems)
    }

internal fun dispatchPendingPickerResult(
    expectedLauncherId: String,
    pendingLauncherId: String?,
    pendingModeId: String?,
    pendingMaxItems: Int?,
    files: List<PlatformFile>?,
    clearPendingState: () -> Unit,
    onConsumed: (Any?) -> Unit,
) {
    if (pendingLauncherId != expectedLauncherId) return

    val modeId = pendingModeId ?: return
    val maxItems = pendingMaxItems

    // Clear stale launch metadata before callbacks to avoid wiping a re-launch made in onConsumed.
    clearPendingState()

    dispatchPickerConsumedResult(
        modeId = modeId,
        maxItems = maxItems,
        files = files,
        onConsumed = onConsumed,
    )
}

internal fun dispatchPickerConsumedResult(
    modeId: String,
    maxItems: Int?,
    files: List<PlatformFile>?,
    onConsumed: (Any?) -> Unit,
) {
    val states = files.toPickerStates()

    when (modeId) {
        PICKER_MODE_SINGLE -> {
            val result = when (val lastState = states.last()) {
                is FileKitPickerState.Completed -> lastState.result.firstOrNull()
                else -> null
            }
            onConsumed(result)
        }

        PICKER_MODE_MULTIPLE -> {
            val result = when (val lastState = states.last()) {
                is FileKitPickerState.Completed -> {
                    maxItems?.let { max -> lastState.result.take(max) } ?: lastState.result
                }

                else -> {
                    null
                }
            }
            onConsumed(result)
        }

        PICKER_MODE_SINGLE_WITH_STATE -> {
            states.forEach { state ->
                when (state) {
                    is FileKitPickerState.Cancelled -> {
                        onConsumed(FileKitPickerState.Cancelled)
                    }

                    is FileKitPickerState.Failed -> {
                        onConsumed(FileKitPickerState.Failed(cause = state.cause))
                    }

                    is FileKitPickerState.Started -> {
                        onConsumed(FileKitPickerState.Started(total = state.total))
                    }

                    is FileKitPickerState.Progress -> {
                        val file = state.processed.firstOrNull()
                        if (file != null) {
                            onConsumed(
                                FileKitPickerState.Progress(
                                    processed = file,
                                    total = state.total,
                                ),
                            )
                        }
                    }

                    is FileKitPickerState.Completed -> {
                        val file = state.result.firstOrNull()
                        if (file != null) {
                            onConsumed(FileKitPickerState.Completed(result = file))
                        } else {
                            onConsumed(FileKitPickerState.Cancelled)
                        }
                    }
                }
            }
        }

        PICKER_MODE_MULTIPLE_WITH_STATE -> {
            states.forEach { state ->
                when (state) {
                    is FileKitPickerState.Cancelled -> {
                        onConsumed(FileKitPickerState.Cancelled)
                    }

                    is FileKitPickerState.Failed -> {
                        onConsumed(FileKitPickerState.Failed(cause = state.cause))
                    }

                    is FileKitPickerState.Started -> {
                        onConsumed(
                            FileKitPickerState.Started(
                                total = maxItems?.let { max -> minOf(state.total, max) } ?: state.total,
                            ),
                        )
                    }

                    is FileKitPickerState.Progress -> {
                        onConsumed(
                            FileKitPickerState.Progress(
                                processed = maxItems?.let { max -> state.processed.take(max) } ?: state.processed,
                                total = maxItems?.let { max -> minOf(state.total, max) } ?: state.total,
                            ),
                        )
                    }

                    is FileKitPickerState.Completed -> {
                        onConsumed(
                            FileKitPickerState.Completed(
                                result = maxItems?.let { max -> state.result.take(max) } ?: state.result,
                            ),
                        )
                    }
                }
            }
        }
    }
}

internal fun List<PlatformFile>?.toPickerStates(): List<FileKitPickerState<List<PlatformFile>>> {
    val files = this
    return when {
        files.isNullOrEmpty() -> listOf(FileKitPickerState.Cancelled)

        else -> buildList {
            add(FileKitPickerState.Started(total = files.size))
            files.forEachIndexed { index, _ ->
                add(
                    FileKitPickerState.Progress(
                        processed = files.subList(0, index + 1),
                        total = files.size,
                    ),
                )
            }
            add(FileKitPickerState.Completed(result = files))
        }
    }
}

private data class DynamicPickMultipleVisualMediaInput(
    val request: PickVisualMediaRequest,
    val maxItems: Int?,
)

private class DynamicPickMultipleVisualMediaContract : ActivityResultContract<DynamicPickMultipleVisualMediaInput, List<Uri>>() {
    override fun createIntent(context: Context, input: DynamicPickMultipleVisualMediaInput): Intent {
        val delegate = input.maxItems
            ?.takeIf { it > 1 }
            ?.let { ActivityResultContracts.PickMultipleVisualMedia(it) }
            ?: ActivityResultContracts.PickMultipleVisualMedia()
        return delegate.createIntent(context, input.request)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): List<Uri> = ActivityResultContracts.PickMultipleVisualMedia().parseResult(
        resultCode,
        intent,
    )
}

private data class CreateDocumentInput(
    val mimeType: String,
    val fileName: String,
)

private class CreateDocumentDynamicContract : ActivityResultContract<CreateDocumentInput, Uri?>() {
    override fun createIntent(
        context: Context,
        input: CreateDocumentInput,
    ): Intent = ActivityResultContracts.CreateDocument(input.mimeType).createIntent(context, input.fileName)

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? = ActivityResultContracts
        .CreateDocument(
            "*/*",
        ).parseResult(resultCode, intent)
}
