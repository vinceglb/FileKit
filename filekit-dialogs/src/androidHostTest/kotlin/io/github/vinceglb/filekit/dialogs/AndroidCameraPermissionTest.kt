@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")
@file:OptIn(io.github.vinceglb.filekit.dialogs.FileKitDialogsInternalApi::class)

package io.github.vinceglb.filekit.dialogs

import android.os.Build
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AndroidCameraPermissionTest {
    @Test
    fun CameraPermission_sdkBelowM_doesNotRequestRuntimePermission() {
        val shouldRequest = FileKitAndroidCameraPermissionInternal.shouldRequestRuntimeCameraPermission(
            apiLevel = Build.VERSION_CODES.LOLLIPOP_MR1,
            isCameraPermissionDeclared = true,
            isCameraPermissionGranted = false,
        )

        assertFalse(shouldRequest)
    }

    @Test
    fun CameraPermission_declaredAndDenied_requestsRuntimePermission() {
        val shouldRequest = FileKitAndroidCameraPermissionInternal.shouldRequestRuntimeCameraPermission(
            apiLevel = Build.VERSION_CODES.M,
            isCameraPermissionDeclared = true,
            isCameraPermissionGranted = false,
        )

        assertTrue(shouldRequest)
    }

    @Test
    fun CameraPermission_notDeclared_doesNotRequestRuntimePermission() {
        val shouldRequest = FileKitAndroidCameraPermissionInternal.shouldRequestRuntimeCameraPermission(
            apiLevel = Build.VERSION_CODES.UPSIDE_DOWN_CAKE,
            isCameraPermissionDeclared = false,
            isCameraPermissionGranted = false,
        )

        assertFalse(shouldRequest)
    }

    @Test
    fun CameraPermission_declaredAndGranted_doesNotRequestRuntimePermission() {
        val shouldRequest = FileKitAndroidCameraPermissionInternal.shouldRequestRuntimeCameraPermission(
            apiLevel = Build.VERSION_CODES.UPSIDE_DOWN_CAKE,
            isCameraPermissionDeclared = true,
            isCameraPermissionGranted = true,
        )

        assertFalse(shouldRequest)
    }
}
