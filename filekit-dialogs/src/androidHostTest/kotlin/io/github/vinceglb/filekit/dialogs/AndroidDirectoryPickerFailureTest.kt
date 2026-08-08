@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit.dialogs

import android.content.ActivityNotFoundException
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityOptionsCompat
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.exceptions.FileKitNotInitializedException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertSame

@RunWith(RobolectricTestRunner::class)
class AndroidDirectoryPickerFailureTest {
    @Test
    fun AndroidDirectoryPicker_missingActivity_throwsDialogOperationalFailureWithCause() {
        val platformFailure = ActivityNotFoundException("No activity for directory picker")
        FileKit.init(throwingActivityResultRegistry(platformFailure))

        val failure = assertFailsWith<FileKitDialogException> {
            runBlocking { FileKit.openDirectoryPicker() }
        }

        assertSame(platformFailure, failure.cause)
    }

    @Test
    fun AndroidDirectoryPicker_securityRejection_throwsDialogOperationalFailureWithCause() {
        val platformFailure = SecurityException("Directory picker launch rejected")
        FileKit.init(throwingActivityResultRegistry(platformFailure))

        val failure = assertFailsWith<FileKitDialogException> {
            runBlocking { FileKit.openDirectoryPicker() }
        }

        val cause = assertIs<SecurityException>(failure.cause)
        assertEquals(platformFailure.message, cause.message)
    }

    @Test
    fun AndroidDirectoryPicker_cancellation_propagatesUnchanged() {
        val cancellation = CancellationException("Directory picker cancelled")
        FileKit.init(throwingActivityResultRegistry(cancellation))

        val failure = assertFailsWith<CancellationException> {
            runBlocking { FileKit.openDirectoryPicker() }
        }

        assertEquals(cancellation.message, failure.message)
    }

    @Test
    fun AndroidDirectoryPicker_unexpectedFailure_propagatesUnchanged() {
        val defect = IllegalStateException("Unexpected directory picker defect")
        FileKit.init(throwingActivityResultRegistry(defect))

        val failure = assertFailsWith<IllegalStateException> {
            runBlocking { FileKit.openDirectoryPicker() }
        }

        assertEquals(defect.message, failure.message)
    }

    private fun throwingActivityResultRegistry(failure: Throwable): ActivityResultRegistry =
        object : ActivityResultRegistry() {
            override fun <I, O> onLaunch(
                requestCode: Int,
                contract: ActivityResultContract<I, O>,
                input: I,
                options: ActivityOptionsCompat?,
            ) = throw failure
        }
}

class AndroidDirectoryPickerInvalidInvocationTest {
    @Test
    fun AndroidDirectoryPicker_uninitializedFileKit_throwsInvalidInvocationFailure() {
        assertFailsWith<FileKitNotInitializedException> {
            runBlocking { FileKit.openDirectoryPicker() }
        }
    }
}
