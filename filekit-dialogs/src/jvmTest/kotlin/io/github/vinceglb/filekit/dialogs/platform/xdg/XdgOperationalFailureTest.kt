@file:Suppress("ktlint:standard:function-naming", "FunctionName")

package io.github.vinceglb.filekit.dialogs.platform.xdg

import io.github.vinceglb.filekit.dialogs.FileKitDialogException
import io.github.vinceglb.filekit.dialogs.FileKitDialogParent
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.FileKitPickerException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.freedesktop.dbus.exceptions.DBusException
import org.freedesktop.dbus.exceptions.DBusExecutionException
import org.freedesktop.dbus.types.UInt32
import org.freedesktop.dbus.types.Variant
import java.net.URI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class XdgOperationalFailureTest {
    @Test
    fun XdgResponseDispatcher_unexpectedResponse_completesWaitingRequestExceptionallyOnce() = runTest {
        val result = CompletableDeferred<List<URI>?>()
        val unexpectedResponse = arrayOf(
            UInt32(99),
            emptyMap<String, Variant<*>>(),
        )

        dispatchXdgPortalResponse(
            parameters = unexpectedResponse,
            result = result,
        )

        assertTrue(result.isCompleted, "The response dispatcher left the waiting request suspended")

        dispatchXdgPortalResponse(
            parameters = arrayOf(UInt32(1), emptyMap<String, Variant<*>>()),
            result = result,
        )

        val failure = assertFailsWith<IllegalStateException> { result.await() }
        assertEquals("Unexpected XDG portal response code: 99", failure.message)
    }

    @Test
    fun XdgFilePickerPortal_filePickerDbusExecutionFailure_throwsPickerOperationalFailureWithCause() = runTest {
        val cause = DBusExecutionException("Portal request failed")
        val picker = XdgFilePickerPortal(ThrowingXdgFileChooserTransport(cause))

        val failure = assertFailsWith<FileKitPickerException> {
            picker.openFilePicker(
                fileExtensions = null,
                directory = null,
                dialogSettings = FileKitDialogSettings(),
            )
        }

        assertSame(cause, failure.cause)
    }

    @Test
    fun XdgFilePickerPortal_directoryPickerDbusFailure_throwsDialogOperationalFailureWithCause() = runTest {
        val cause = DBusExecutionException("Portal request failed")
        val picker = XdgFilePickerPortal(ThrowingXdgFileChooserTransport(cause))

        val failure = assertFailsWith<FileKitDialogException> {
            picker.openDirectoryPicker(
                directory = null,
                dialogSettings = FileKitDialogSettings(),
            )
        }

        assertSame(cause, failure.cause)
    }

    @Test
    fun XdgFilePickerPortal_fileSaverDbusFailure_throwsDialogOperationalFailureWithCause() = runTest {
        val cause = DBusExecutionException("Portal request failed")
        val picker = XdgFilePickerPortal(ThrowingXdgFileChooserTransport(cause))

        val failure = assertFailsWith<FileKitDialogException> {
            picker.openFileSaver(
                suggestedName = "document",
                defaultExtension = "txt",
                allowedExtensions = setOf("txt"),
                directory = null,
                dialogSettings = FileKitDialogSettings(),
            )
        }

        assertSame(cause, failure.cause)
    }

    @Test
    fun XdgFilePickerPortal_sessionBusFailure_throwsPickerOperationalFailureWithCause() = runTest {
        val cause = DBusException("Session bus unavailable")
        val picker = XdgFilePickerPortal(ThrowingXdgFileChooserTransport(cause))

        val failure = assertFailsWith<FileKitPickerException> {
            picker.openFilePicker(
                fileExtensions = null,
                directory = null,
                dialogSettings = FileKitDialogSettings(),
            )
        }

        assertSame(cause, failure.cause)
    }

    @Test
    fun XdgFilePickerPortal_otherPortalResponse_throwsPickerOperationalFailureWithCause() = runTest {
        val picker = XdgFilePickerPortal(RespondingXdgFileChooserTransport(response = 2))

        val failure = assertFailsWith<FileKitPickerException> {
            picker.openFilePicker(
                fileExtensions = null,
                directory = null,
                dialogSettings = FileKitDialogSettings(),
            )
        }

        assertEquals(2, (failure.cause as XdgPortalResponseException).response)
    }

    @Test
    fun XdgFilePickerPortal_otherDirectoryResponse_throwsDialogOperationalFailureWithCause() = runTest {
        val picker = XdgFilePickerPortal(RespondingXdgFileChooserTransport(response = 2))

        val failure = assertFailsWith<FileKitDialogException> {
            picker.openDirectoryPicker(
                directory = null,
                dialogSettings = FileKitDialogSettings(),
            )
        }

        assertEquals(2, (failure.cause as XdgPortalResponseException).response)
    }

    @Test
    fun XdgFilePickerPortal_otherSaverResponse_throwsDialogOperationalFailureWithCause() = runTest {
        val picker = XdgFilePickerPortal(RespondingXdgFileChooserTransport(response = 2))

        val failure = assertFailsWith<FileKitDialogException> {
            picker.openFileSaver(
                suggestedName = "document",
                defaultExtension = "txt",
                allowedExtensions = setOf("txt"),
                directory = null,
                dialogSettings = FileKitDialogSettings(),
            )
        }

        assertEquals(2, (failure.cause as XdgPortalResponseException).response)
    }

    @Test
    fun XdgFilePickerPortal_cancelledPortalResponse_returnsNull() = runTest {
        val picker = XdgFilePickerPortal(RespondingXdgFileChooserTransport(response = 1))

        val result = picker.openFilePicker(
            fileExtensions = null,
            directory = null,
            dialogSettings = FileKitDialogSettings(),
        )

        assertNull(result)
    }

    @Test
    fun XdgFilePickerPortal_cancellation_propagatesUnchanged() = runTest {
        val cancellation = CancellationException("Picker cancelled")
        val picker = XdgFilePickerPortal(ThrowingXdgFileChooserTransport(cancellation))

        val failure = assertFailsWith<CancellationException> {
            picker.openFilePicker(
                fileExtensions = null,
                directory = null,
                dialogSettings = FileKitDialogSettings(),
            )
        }

        assertSame(cancellation, failure)
    }

    @Test
    fun XdgFilePickerPortal_unexpectedRuntimeFailure_propagatesUnchanged() = runTest {
        val defect = IllegalStateException("Unexpected defect")
        val picker = XdgFilePickerPortal(ThrowingXdgFileChooserTransport(defect))

        val failure = assertFailsWith<IllegalStateException> {
            picker.openFilePicker(
                fileExtensions = null,
                directory = null,
                dialogSettings = FileKitDialogSettings(),
            )
        }

        assertSame(defect, failure)
    }

    @Test
    fun XdgFilePickerPortal_invalidDialogParent_failsBeforeOperationalNormalization() = runTest {
        val picker = XdgFilePickerPortal(
            ThrowingXdgFileChooserTransport(DBusExecutionException("Transport must not run")),
        )

        assertFailsWith<IllegalArgumentException> {
            picker.openFilePicker(
                fileExtensions = null,
                directory = null,
                dialogSettings = FileKitDialogSettings(
                    parent = FileKitDialogParent.windows(0x1234),
                ),
            )
        }
    }
}

private class ThrowingXdgFileChooserTransport(
    private val failure: Throwable,
) : XdgFileChooserTransport {
    override fun isAvailable(): Boolean = true

    override suspend fun openFile(
        parentWindow: String,
        title: String,
        options: MutableMap<String, Variant<*>>,
    ): List<URI>? = throw failure

    override suspend fun saveFile(
        parentWindow: String,
        title: String,
        options: MutableMap<String, Variant<*>>,
    ): List<URI>? = throw failure
}

private class RespondingXdgFileChooserTransport(
    private val response: Int,
) : XdgFileChooserTransport {
    override fun isAvailable(): Boolean = true

    override suspend fun openFile(
        parentWindow: String,
        title: String,
        options: MutableMap<String, Variant<*>>,
    ): List<URI>? = resolveXdgPortalResponse(
        response = response,
        results = emptyMap<String, Variant<*>>(),
    )

    override suspend fun saveFile(
        parentWindow: String,
        title: String,
        options: MutableMap<String, Variant<*>>,
    ): List<URI>? = resolveXdgPortalResponse(
        response = response,
        results = emptyMap<String, Variant<*>>(),
    )
}
