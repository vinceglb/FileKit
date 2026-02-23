@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")
@file:OptIn(io.github.vinceglb.filekit.dialogs.FileKitDialogsInternalApi::class)

package io.github.vinceglb.filekit.dialogs.compose

import android.net.Uri
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitAndroidDialogsInternal
import io.github.vinceglb.filekit.dialogs.FileKitPickerState
import io.github.vinceglb.filekit.path
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AndroidComposePickerReliabilityTest {
    @Test
    fun CameraResult_successWithPendingUri_returnsPlatformFile() {
        val result = resolveCameraResult(
            success = true,
            pendingDestinationUri = "content://example.provider/camera/photo.jpg",
        )

        assertEquals(
            expected = "content://example.provider/camera/photo.jpg",
            actual = result?.path,
        )
    }

    @Test
    fun CameraResult_withoutPendingUri_returnsNull() {
        val result = resolveCameraResult(
            success = true,
            pendingDestinationUri = null,
        )

        assertNull(result)
    }

    @Test
    fun CameraResult_cancelledWithPendingUri_returnsNull() {
        val result = resolveCameraResult(
            success = false,
            pendingDestinationUri = "content://example.provider/camera/photo.jpg",
        )

        assertNull(result)
    }

    @Test
    fun CameraPermission_denied_returnsNullResult() {
        val resolution = resolveCameraPermissionResult(
            permissionGranted = false,
            pendingDestinationUri = "content://example.provider/camera/photo.jpg",
        )

        assertIs<CameraPermissionResolution.ReturnNullResult>(resolution)
    }

    @Test
    fun CameraPermission_grantedWithPendingUri_requestsCameraLaunch() {
        val resolution = resolveCameraPermissionResult(
            permissionGranted = true,
            pendingDestinationUri = "content://example.provider/camera/photo.jpg",
        )

        val launch = assertIs<CameraPermissionResolution.LaunchCamera>(resolution)
        assertEquals("content://example.provider/camera/photo.jpg", launch.uri.toString())
    }

    @Test
    fun CameraPermission_grantedWithoutPendingUri_returnsNoOp() {
        val resolution = resolveCameraPermissionResult(
            permissionGranted = true,
            pendingDestinationUri = null,
        )

        assertIs<CameraPermissionResolution.NoOp>(resolution)
    }

    @Test
    fun CameraLaunchSafely_whenSecurityException_returnsFalse() {
        val launched = launchCameraSafely(Uri.parse("content://example.provider/camera/photo.jpg")) {
            throw SecurityException("camera permission denied")
        }

        assertFalse(launched)
    }

    @Test
    fun CameraLaunchSafely_whenNoError_returnsTrue() {
        val expectedUri = Uri.parse("content://example.provider/camera/photo.jpg")
        var launchedUri: Uri? = null

        val launched = launchCameraSafely(expectedUri) { uri ->
            launchedUri = uri
        }

        assertTrue(launched)
        assertEquals(expectedUri, launchedUri)
    }

    @Test
    fun PickerResult_singleModeWhenCancelled_emitsNullResult() {
        val consumed = mutableListOf<Any?>()

        dispatchPickerConsumedResult(
            modeId = PICKER_MODE_SINGLE,
            maxItems = null,
            files = null,
            onConsumed = { consumed += it },
        )

        assertEquals(expected = 1, actual = consumed.size)
        assertNull(consumed.single())
    }

    @Test
    fun PickerResult_multipleModeWithMaxItems_truncatesResult() {
        val consumed = mutableListOf<Any?>()
        val files = listOf(
            PlatformFile(Uri.parse("content://example.provider/file/1")),
            PlatformFile(Uri.parse("content://example.provider/file/2")),
            PlatformFile(Uri.parse("content://example.provider/file/3")),
        )

        dispatchPickerConsumedResult(
            modeId = PICKER_MODE_MULTIPLE,
            maxItems = 2,
            files = files,
            onConsumed = { consumed += it },
        )

        val result = assertIs<List<*>>(consumed.single())
        assertEquals(expected = 2, actual = result.size)
        assertEquals(
            expected = listOf(
                "content://example.provider/file/1",
                "content://example.provider/file/2",
            ),
            actual = result
                .map { it as PlatformFile }
                .map { it.path },
        )
    }

    @Test
    fun PickerResult_singleWithState_emitsStartedProgressAndCompleted() {
        val consumed = mutableListOf<Any?>()
        val file = PlatformFile(Uri.parse("content://example.provider/image/42"))

        dispatchPickerConsumedResult(
            modeId = PICKER_MODE_SINGLE_WITH_STATE,
            maxItems = null,
            files = listOf(file),
            onConsumed = { consumed += it },
        )

        assertEquals(expected = 3, actual = consumed.size)
        assertIs<FileKitPickerState.Started>(consumed[0])

        val progress = assertIs<FileKitPickerState.Progress<*>>(consumed[1])
        assertEquals(expected = file.path, actual = (progress.processed as PlatformFile).path)

        val completed = assertIs<FileKitPickerState.Completed<*>>(consumed[2])
        assertEquals(expected = file.path, actual = (completed.result as PlatformFile).path)
    }

    @Test
    fun PickerResult_multipleWithStateWhenEmpty_emitsCancelled() {
        val consumed = mutableListOf<Any?>()

        dispatchPickerConsumedResult(
            modeId = PICKER_MODE_MULTIPLE_WITH_STATE,
            maxItems = 3,
            files = emptyList(),
            onConsumed = { consumed += it },
        )

        assertEquals(expected = 1, actual = consumed.size)
        assertIs<FileKitPickerState.Cancelled>(consumed.single())
    }

    @Test
    fun VisualLauncher_multipleMaxItemsOne_routesToSingleLauncher() {
        assertTrue(
            shouldUseSingleVisualLauncher(
                modeId = PICKER_MODE_MULTIPLE,
                maxItems = 1,
            ),
        )
        assertTrue(
            shouldUseSingleVisualLauncher(
                modeId = PICKER_MODE_MULTIPLE_WITH_STATE,
                maxItems = 1,
            ),
        )
        assertFalse(
            shouldUseSingleVisualLauncher(
                modeId = PICKER_MODE_MULTIPLE,
                maxItems = 2,
            ),
        )
    }

    @Test
    fun PendingDispatch_clearsBeforeCallback_keepsRelaunchState() {
        var pendingModeId: String? = PICKER_MODE_SINGLE
        var pendingMaxItems: Int? = null
        var pendingLauncherId: String? = "initial"

        val consumed = mutableListOf<Any?>()

        dispatchPendingPickerResult(
            expectedLauncherId = "initial",
            pendingLauncherId = pendingLauncherId,
            pendingModeId = pendingModeId,
            pendingMaxItems = pendingMaxItems,
            files = listOf(PlatformFile(Uri.parse("content://example.provider/file/1"))),
            clearPendingState = {
                pendingModeId = null
                pendingMaxItems = null
                pendingLauncherId = null
            },
            onConsumed = { result ->
                consumed += result

                // Simulate a new launch triggered synchronously from onResult callback.
                pendingModeId = PICKER_MODE_MULTIPLE
                pendingMaxItems = 3
                pendingLauncherId = "relaunch"
            },
        )

        val singleResult = assertIs<PlatformFile>(consumed.single())
        assertEquals("content://example.provider/file/1", singleResult.path)
        assertEquals(PICKER_MODE_MULTIPLE, pendingModeId)
        assertEquals(3, pendingMaxItems)
        assertEquals("relaunch", pendingLauncherId)
    }

    @Test
    fun FileSaverName_normalizesAndBuildsSuggestedName() {
        assertEquals("pdf", FileKitAndroidDialogsInternal.normalizeFileSaverExtension(" .pdf "))
        assertEquals(
            expected = "report.pdf",
            actual = FileKitAndroidDialogsInternal.buildFileSaverSuggestedName(
                suggestedName = "report",
                extension = " .pdf ",
            ),
        )
    }
}
