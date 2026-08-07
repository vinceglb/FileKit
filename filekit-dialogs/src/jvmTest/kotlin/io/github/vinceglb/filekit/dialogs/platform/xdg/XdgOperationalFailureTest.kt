@file:Suppress("ktlint:standard:function-naming", "FunctionName")

package io.github.vinceglb.filekit.dialogs.platform.xdg

import io.github.vinceglb.filekit.dialogs.FileKitDialogException
import io.github.vinceglb.filekit.dialogs.FileKitDialogParent
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.FileKitPickerException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.freedesktop.dbus.exceptions.DBusException
import org.freedesktop.dbus.exceptions.DBusExecutionException
import org.freedesktop.dbus.types.Variant
import java.net.URI
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class XdgOperationalFailureTest {
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
