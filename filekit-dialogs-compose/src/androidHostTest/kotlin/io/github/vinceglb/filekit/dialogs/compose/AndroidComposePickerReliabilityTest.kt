@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")
@file:OptIn(io.github.vinceglb.filekit.dialogs.FileKitDialogsInternalApi::class)

package io.github.vinceglb.filekit.dialogs.compose

import android.content.ActivityNotFoundException
import android.net.Uri
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitAndroidDialogsInternal
import io.github.vinceglb.filekit.dialogs.FileKitDialogException
import io.github.vinceglb.filekit.dialogs.FileKitPickerException
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
import kotlin.test.assertSame
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
    fun CameraPermission_denied_clearsPendingStateBeforeReturningNull_andAllowsImmediateRelaunch() {
        var hasPendingLaunch = true
        val results = mutableListOf<PlatformFile?>()

        dispatchCameraPermissionResolution(
            resolution = CameraPermissionResolution.ReturnNullResult,
            launchCamera = { error("Camera must not launch after permission denial") },
            clearPendingState = { hasPendingLaunch = false },
            onError = { error("Permission denial must not be reported as an error") },
            onResult = { result ->
                assertFalse(hasPendingLaunch)
                results += result
                hasPendingLaunch = true
            },
        )

        assertEquals(listOf<PlatformFile?>(null), results)
        assertTrue(hasPendingLaunch)

        dispatchCameraResult(
            success = false,
            pendingDestinationUri = "content://example.provider/camera/relaunch.jpg".takeIf { hasPendingLaunch },
            clearPendingState = { hasPendingLaunch = false },
            onResult = results::add,
        )

        assertEquals(listOf<PlatformFile?>(null, null), results)
        assertFalse(hasPendingLaunch)
    }

    @Test
    fun CameraLaunchFailure_clearsPendingStateBeforeReportingError_andAllowsImmediateRelaunch() {
        var hasPendingLaunch = true
        val launchFailure = FileKitDialogException("Camera unavailable")
        val failures = mutableListOf<FileKitDialogException>()
        val results = mutableListOf<PlatformFile?>()

        dispatchCameraLaunchResult(
            result = CameraLaunchResult.Failed(launchFailure),
            clearPendingState = { hasPendingLaunch = false },
            onError = { failure ->
                assertFalse(hasPendingLaunch)
                failures += failure
                hasPendingLaunch = true
            },
        )

        assertEquals(listOf(launchFailure), failures)
        assertTrue(hasPendingLaunch)

        dispatchCameraResult(
            success = true,
            pendingDestinationUri = "content://example.provider/camera/relaunch.jpg".takeIf { hasPendingLaunch },
            clearPendingState = { hasPendingLaunch = false },
            onResult = results::add,
        )

        assertEquals("content://example.provider/camera/relaunch.jpg", results.single()?.path)
        assertFalse(hasPendingLaunch)
    }

    @Test
    fun CameraResult_success_clearsPendingStateBeforeReturningFile_andAllowsImmediateRelaunch() {
        var hasPendingLaunch = true
        val results = mutableListOf<PlatformFile?>()

        dispatchCameraResult(
            success = true,
            pendingDestinationUri = "content://example.provider/camera/photo.jpg",
            clearPendingState = { hasPendingLaunch = false },
            onResult = { result ->
                assertFalse(hasPendingLaunch)
                results += result
                hasPendingLaunch = true
            },
        )

        assertEquals(1, results.size)
        assertEquals("content://example.provider/camera/photo.jpg", results.single()?.path)
        assertTrue(hasPendingLaunch)
    }

    @Test
    fun CameraLaunchSafely_whenSecurityException_returnsOperationalFailureWithCause() {
        val launchFailure = SecurityException("camera permission denied")

        val result = launchCameraSafely(Uri.parse("content://example.provider/camera/photo.jpg")) {
            throw launchFailure
        }

        val failure = assertIs<CameraLaunchResult.Failed>(result).failure
        assertIs<FileKitDialogException>(failure)
        assertSame(launchFailure, failure.cause)
    }

    @Test
    fun CameraLaunchSafely_whenActivityNotFound_returnsOperationalFailureWithCause() {
        val launchFailure = ActivityNotFoundException("No activity found")

        val result = launchCameraSafely(Uri.parse("content://example.provider/camera/photo.jpg")) {
            throw launchFailure
        }

        val failure = assertIs<CameraLaunchResult.Failed>(result).failure
        assertIs<FileKitDialogException>(failure)
        assertSame(launchFailure, failure.cause)
    }

    @Test
    fun CameraLaunchSafely_whenNoError_returnsLaunched() {
        val expectedUri = Uri.parse("content://example.provider/camera/photo.jpg")
        var launchedUri: Uri? = null

        val result = launchCameraSafely(expectedUri) { uri ->
            launchedUri = uri
        }

        assertIs<CameraLaunchResult.Launched>(result)
        assertEquals(expectedUri, launchedUri)
    }

    @Test
    fun CameraLaunchSafely_whenUnexpectedFailure_propagates() {
        val failure = IllegalStateException("Unexpected camera launcher defect")

        val thrown = kotlin.test.assertFailsWith<IllegalStateException> {
            launchCameraSafely(Uri.parse("content://example.provider/camera/photo.jpg")) {
                throw failure
            }
        }

        assertSame(failure, thrown)
    }

    @Test
    fun CameraPermissionLaunchSafely_whenActivityNotFound_returnsOperationalFailureWithCause() {
        val launchFailure = ActivityNotFoundException("No permission activity")

        val result = launchCameraPermissionSafely {
            throw launchFailure
        }

        val failure = assertIs<CameraLaunchResult.Failed>(result).failure
        assertSame(launchFailure, failure.cause)
    }

    @Test
    fun LegacyCameraLauncher_androidBinarySignature_remainsAvailable() {
        val composeFileClass = Class.forName(
            "io.github.vinceglb.filekit.dialogs.compose.FileKitCompose_androidKt",
        )

        composeFileClass.getDeclaredMethod(
            "rememberCameraPickerLauncher",
            io.github.vinceglb.filekit.dialogs.FileKitOpenCameraSettings::class.java,
            Class.forName("kotlin.jvm.functions.Function1"),
            androidx.compose.runtime.Composer::class.java,
            Int::class.javaPrimitiveType,
            Int::class.javaPrimitiveType,
        )
    }

    @Test
    fun PickerLaunchSafely_whenActivityNotFound_returnsOperationalFailureWithCause() {
        val launchFailure = ActivityNotFoundException("No activity found")

        val result = launchFilePickerSafely {
            throw launchFailure
        }

        val failure = assertIs<PickerLaunchResult.Failed>(result).failure
        assertIs<FileKitPickerException>(failure)
        assertSame(launchFailure, failure.cause)
    }

    @Test
    fun PickerLaunchSafely_whenSecurityException_returnsOperationalFailureWithCause() {
        val launchFailure = SecurityException("Picker launch rejected")

        val result = launchFilePickerSafely {
            throw launchFailure
        }

        val failure = assertIs<PickerLaunchResult.Failed>(result).failure
        assertIs<FileKitPickerException>(failure)
        assertSame(launchFailure, failure.cause)
    }

    @Test
    fun PickerLaunchSafely_whenNoError_returnsLaunched() {
        var launched = false

        val result = launchFilePickerSafely {
            launched = true
        }

        assertIs<PickerLaunchResult.Launched>(result)
        assertTrue(launched)
    }

    @Test
    fun DirectoryLaunchSafely_whenActivityNotFound_returnsOperationalFailureWithCause() {
        val launchFailure = ActivityNotFoundException("No directory picker activity")

        val result = launchDirectoryPickerSafely {
            throw launchFailure
        }

        val failure = assertIs<DirectoryLaunchResult.Failed>(result).failure
        assertIs<FileKitDialogException>(failure)
        assertSame(launchFailure, failure.cause)
    }

    @Test
    fun DirectoryLaunchSafely_whenSecurityException_returnsOperationalFailureWithCause() {
        val launchFailure = SecurityException("Directory picker launch rejected")

        val result = launchDirectoryPickerSafely {
            throw launchFailure
        }

        val failure = assertIs<DirectoryLaunchResult.Failed>(result).failure
        assertIs<FileKitDialogException>(failure)
        assertSame(launchFailure, failure.cause)
    }

    @Test
    fun DirectoryLaunchSafely_whenUnexpectedFailure_propagates() {
        val failure = IllegalStateException("Unexpected launcher defect")

        val thrown = kotlin.test.assertFailsWith<IllegalStateException> {
            launchDirectoryPickerSafely { throw failure }
        }

        assertSame(failure, thrown)
    }

    @Test
    fun DirectoryLaunchSafely_whenNoError_returnsLaunched() {
        var launched = false

        val result = launchDirectoryPickerSafely {
            launched = true
        }

        assertIs<DirectoryLaunchResult.Launched>(result)
        assertTrue(launched)
    }

    @Test
    fun FileSaverLaunchSafely_whenActivityNotFound_returnsOperationalFailureWithCause() {
        val launchFailure = ActivityNotFoundException("No file saver activity")

        val result = launchFileSaverSafely {
            throw launchFailure
        }

        val failure = assertIs<SaverLaunchResult.Failed>(result).failure
        assertIs<FileKitDialogException>(failure)
        assertSame(launchFailure, failure.cause)
    }

    @Test
    fun FileSaverLaunchSafely_whenSecurityException_returnsOperationalFailureWithCause() {
        val launchFailure = SecurityException("File saver launch rejected")

        val result = launchFileSaverSafely {
            throw launchFailure
        }

        val failure = assertIs<SaverLaunchResult.Failed>(result).failure
        assertIs<FileKitDialogException>(failure)
        assertSame(launchFailure, failure.cause)
    }

    @Test
    fun FileSaverLaunchSafely_whenUnexpectedFailure_propagates() {
        val failure = IllegalStateException("Unexpected saver defect")

        val thrown = kotlin.test.assertFailsWith<IllegalStateException> {
            launchFileSaverSafely { throw failure }
        }

        assertSame(failure, thrown)
    }

    @Test
    fun FileSaverLaunchSafely_whenNoError_returnsLaunched() {
        var launched = false

        val result = launchFileSaverSafely {
            launched = true
        }

        assertIs<SaverLaunchResult.Launched>(result)
        assertTrue(launched)
    }

    @Test
    fun PickerLaunchOutcome_primaryFailsAndFallbackSucceeds_returnsFallbackLaunched() {
        var fallbackCalls = 0

        val outcome = resolvePickerLaunchOutcome(
            launchPrimary = {
                PickerLaunchResult.Failed(
                    failure = FileKitPickerException("Primary failed"),
                    isFallbackEligible = true,
                )
            },
            launchFallback = {
                fallbackCalls++
                PickerLaunchResult.Launched
            },
        )

        assertEquals(PickerLaunchOutcome.FallbackLaunched, outcome)
        assertEquals(1, fallbackCalls)
    }

    @Test
    fun PickerLaunchOutcome_primarySecurityFailure_doesNotLaunchFallback() {
        val launchFailure = SecurityException("Visual picker launch rejected")
        var fallbackCalls = 0

        val outcome = resolvePickerLaunchOutcome(
            launchPrimary = {
                launchFilePickerSafely {
                    throw launchFailure
                }
            },
            launchFallback = {
                fallbackCalls++
                PickerLaunchResult.Launched
            },
        )

        val failure = assertIs<PickerLaunchOutcome.Failed>(outcome).failure
        assertSame(launchFailure, failure.cause)
        assertEquals(0, fallbackCalls)
    }

    @Test
    fun PickerLaunchOutcome_primaryAndFallbackFail_returnsFallbackOperationalFailure() {
        val fallbackFailure = FileKitPickerException("Fallback failed")

        val outcome = resolvePickerLaunchOutcome(
            launchPrimary = {
                PickerLaunchResult.Failed(
                    failure = FileKitPickerException("Primary failed"),
                    isFallbackEligible = true,
                )
            },
            launchFallback = {
                PickerLaunchResult.Failed(
                    failure = fallbackFailure,
                    isFallbackEligible = false,
                )
            },
        )

        val failure = assertIs<PickerLaunchOutcome.Failed>(outcome)
        assertSame(fallbackFailure, failure.failure)
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
    fun PickerResult_multipleModeWhenCancelled_emitsNullResult() {
        val consumed = mutableListOf<Any?>()

        dispatchPickerConsumedResult(
            modeId = PICKER_MODE_MULTIPLE,
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
    fun PickerResult_singleWithStateWhenCancelled_emitsCancelled() {
        val consumed = mutableListOf<Any?>()

        dispatchPickerConsumedResult(
            modeId = PICKER_MODE_SINGLE_WITH_STATE,
            maxItems = null,
            files = null,
            onConsumed = { consumed += it },
        )

        assertEquals(expected = 1, actual = consumed.size)
        assertIs<FileKitPickerState.Cancelled>(consumed.single())
    }

    @Test
    fun PickerResult_multipleWithStateWhenNull_emitsCancelled() {
        val consumed = mutableListOf<Any?>()

        dispatchPickerConsumedResult(
            modeId = PICKER_MODE_MULTIPLE_WITH_STATE,
            maxItems = 3,
            files = null,
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
            setOf("txt", "md"),
            FileKitAndroidDialogsInternal.normalizeFileSaverExtensions(setOf(" .txt ", ".md")),
        )
        assertEquals(
            expected = "report.pdf",
            actual = FileKitAndroidDialogsInternal.buildFileSaverSuggestedName(
                suggestedName = "report",
                extension = " .pdf ",
            ),
        )
    }
}
