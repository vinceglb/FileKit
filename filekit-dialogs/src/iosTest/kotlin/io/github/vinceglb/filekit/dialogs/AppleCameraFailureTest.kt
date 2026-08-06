@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.PlatformFile
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.UIKit.UIImage
import platform.UIKit.UIImagePickerControllerCameraDevice
import platform.UIKit.UIViewController
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AppleCameraFailureTest {
    @Test
    fun AppleCamera_unavailableSource_throwsDialogOperationalFailure() {
        val failure = assertFailsWith<FileKitDialogException> {
            prepareAppleCameraPresentation(
                sourceAvailable = false,
                presenter = UIViewController(),
                requestedCamera = null,
            )
        }

        assertEquals("The camera is not available on this device.", failure.message)
    }

    @Test
    fun AppleCamera_unavailableRequestedDevice_throwsDialogOperationalFailure() {
        val failure = assertFailsWith<FileKitDialogException> {
            prepareAppleCameraPresentation(
                sourceAvailable = true,
                presenter = UIViewController(),
                requestedCamera = AppleCameraDeviceRequest(
                    device = UIImagePickerControllerCameraDevice.UIImagePickerControllerCameraDeviceFront,
                    available = false,
                    unavailableMessage = "The requested front camera is not available on this device.",
                ),
            )
        }

        assertEquals("The requested front camera is not available on this device.", failure.message)
    }

    @Test
    fun AppleCamera_missingPresenter_throwsDialogOperationalFailure() {
        val failure = assertFailsWith<FileKitDialogException> {
            prepareAppleCameraPresentation(
                sourceAvailable = true,
                presenter = null,
                requestedCamera = null,
            )
        }

        assertEquals("No active view controller is available to present the camera.", failure.message)
    }

    @Test
    fun AppleCamera_missingCapturedImage_throwsDialogOperationalFailure() {
        val failure = assertFailsWith<FileKitDialogException> {
            requireAppleCameraImage(null)
        }

        assertEquals("The camera completed without returning a captured image.", failure.message)
    }

    @Test
    fun AppleCamera_failedImageEncoding_throwsDialogOperationalFailure() {
        val destination = PlatformFile(NSURL.fileURLWithPath("/tmp/filekit-camera.jpg"))

        val failure = assertFailsWith<FileKitDialogException> {
            completeAppleCameraCapture(
                image = UIImage(),
                destinationFile = destination,
                encodeImage = { null },
                writeImage = { _, _ -> error("Write must not run when encoding fails") },
            )
        }

        assertEquals("Failed to encode the captured image.", failure.message)
    }

    @Test
    fun AppleCamera_failedDestinationWrite_throwsDialogOperationalFailure() {
        val destination = PlatformFile(NSURL.fileURLWithPath("/tmp/filekit-camera.jpg"))

        val failure = assertFailsWith<FileKitDialogException> {
            completeAppleCameraCapture(
                image = UIImage(),
                destinationFile = destination,
                encodeImage = { NSData() },
                writeImage = { _, _ -> false },
            )
        }

        assertEquals("Failed to write the captured image to its destination.", failure.message)
    }
}
