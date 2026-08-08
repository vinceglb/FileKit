@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit.dialogs

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.net.Uri
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.manualFileKitCoreInitialization
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AndroidCameraPickerFailureTest {
    private lateinit var context: Context
    private lateinit var registry: ActivityResultRegistry
    private val cameraDestination = PlatformFile(Uri.parse("content://example.provider/camera/photo.jpg"))

    @Before
    fun setup() {
        context = RuntimeEnvironment.getApplication()
        FileKit.manualFileKitCoreInitialization(context)
        shadowOf(context.packageManager)
            .getInternalMutablePackageInfo(context.packageName)
            .requestedPermissions = emptyArray()
    }

    @Test
    fun AndroidCameraPicker_missingCameraActivity_throwsDialogOperationalFailureWithCause() {
        val platformFailure = ActivityNotFoundException("No activity for camera")
        registry = throwingActivityResultRegistry(TakePictureWithCameraFacing::class.java, platformFailure)
        FileKit.init(registry)

        val failure = assertFailsWith<FileKitDialogException> {
            runBlocking { openCameraPickerAtTestDestination() }
        }

        assertSame(platformFailure, failure.cause)
    }

    @Test
    fun AndroidCameraPicker_unauthorizedCameraLaunch_throwsDialogOperationalFailureWithCause() {
        val platformFailure = SecurityException("Camera launch is not authorized")
        registry = throwingActivityResultRegistry(TakePictureWithCameraFacing::class.java, platformFailure)
        FileKit.init(registry)

        val failure = assertFailsWith<FileKitDialogException> {
            runBlocking { openCameraPickerAtTestDestination() }
        }

        val cause = assertIs<SecurityException>(failure.cause)
        assertEquals(platformFailure.message, cause.message)
    }

    @Test
    fun AndroidCameraPicker_missingPermissionActivity_throwsDialogOperationalFailureWithCause() {
        declareCameraPermission()
        val platformFailure = ActivityNotFoundException("No activity for camera permission")
        registry = throwingActivityResultRegistry(ActivityResultContracts.RequestPermission::class.java, platformFailure)
        FileKit.init(registry)

        val failure = assertFailsWith<FileKitDialogException> {
            runBlocking { openCameraPickerAtTestDestination() }
        }

        assertSame(platformFailure, failure.cause)
    }

    @Test
    fun AndroidCameraPicker_unauthorizedPermissionLaunch_throwsDialogOperationalFailureWithCause() {
        declareCameraPermission()
        val platformFailure = SecurityException("Camera permission launch is not authorized")
        registry = throwingActivityResultRegistry(ActivityResultContracts.RequestPermission::class.java, platformFailure)
        FileKit.init(registry)

        val failure = assertFailsWith<FileKitDialogException> {
            runBlocking { openCameraPickerAtTestDestination() }
        }

        val cause = assertIs<SecurityException>(failure.cause)
        assertEquals(platformFailure.message, cause.message)
    }

    @Test
    fun AndroidCameraPicker_permissionDenied_returnsNull() {
        declareCameraPermission()
        registry = completingActivityResultRegistry(
            expectedContract = ActivityResultContracts.RequestPermission::class.java,
            output = false,
        )
        FileKit.init(registry)

        val result = runBlocking { openCameraPickerAtTestDestination() }

        assertNull(result)
    }

    @Test
    fun AndroidCameraPicker_cameraDismissed_returnsNull() {
        registry = completingActivityResultRegistry(
            expectedContract = TakePictureWithCameraFacing::class.java,
            output = false,
        )
        FileKit.init(registry)

        val result = runBlocking { openCameraPickerAtTestDestination() }

        assertNull(result)
    }

    @Test
    fun AndroidCameraPicker_cancellation_propagatesUnchanged() {
        val cancellation = CancellationException("Camera picker cancelled")
        registry = throwingActivityResultRegistry(TakePictureWithCameraFacing::class.java, cancellation)
        FileKit.init(registry)

        val failure = assertFailsWith<CancellationException> {
            runBlocking { openCameraPickerAtTestDestination() }
        }

        assertEquals(cancellation.message, failure.message)
    }

    @Test
    fun AndroidCameraPicker_unexpectedFailure_propagatesUnchanged() {
        val defect = IllegalStateException("Unexpected camera picker defect")
        registry = throwingActivityResultRegistry(TakePictureWithCameraFacing::class.java, defect)
        FileKit.init(registry)

        val failure = assertFailsWith<IllegalStateException> {
            runBlocking { openCameraPickerAtTestDestination() }
        }

        assertEquals(defect.message, failure.message)
    }

    private fun declareCameraPermission() {
        shadowOf(context.packageManager)
            .getInternalMutablePackageInfo(context.packageName)
            .requestedPermissions = arrayOf(Manifest.permission.CAMERA)
    }

    private suspend fun openCameraPickerAtTestDestination(): PlatformFile? =
        FileKit.openCameraPicker(destinationFile = cameraDestination)

    private fun throwingActivityResultRegistry(
        expectedContract: Class<out ActivityResultContract<*, *>>,
        failure: Throwable,
    ): ActivityResultRegistry = object : ActivityResultRegistry() {
        override fun <I, O> onLaunch(
            requestCode: Int,
            contract: ActivityResultContract<I, O>,
            input: I,
            options: ActivityOptionsCompat?,
        ) {
            check(expectedContract.isInstance(contract))
            throw failure
        }
    }

    private fun <O> completingActivityResultRegistry(
        expectedContract: Class<out ActivityResultContract<*, *>>,
        output: O,
    ): ActivityResultRegistry = object : ActivityResultRegistry() {
        @Suppress("UNCHECKED_CAST")
        override fun <I, T> onLaunch(
            requestCode: Int,
            contract: ActivityResultContract<I, T>,
            input: I,
            options: ActivityOptionsCompat?,
        ) {
            check(expectedContract.isInstance(contract))
            dispatchResult(requestCode, output as T)
        }
    }
}
