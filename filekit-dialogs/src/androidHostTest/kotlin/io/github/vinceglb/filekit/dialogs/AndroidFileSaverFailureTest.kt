@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit.dialogs

import android.content.ActivityNotFoundException
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityOptionsCompat
import io.github.vinceglb.filekit.FileKit
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
class AndroidFileSaverFailureTest {
    private lateinit var registry: ActivityResultRegistry

    @Test
    fun AndroidFileSaver_missingActivity_throwsDialogOperationalFailureWithCause() {
        val platformFailure = ActivityNotFoundException("No activity for file saver")
        registry = throwingActivityResultRegistry(platformFailure)
        FileKit.init(registry)

        val failure = assertFailsWith<FileKitDialogException> {
            runBlocking { openFileSaver() }
        }

        assertSame(platformFailure, failure.cause)
    }

    @Test
    fun AndroidFileSaver_securityRejection_throwsDialogOperationalFailureWithCause() {
        val platformFailure = SecurityException("File saver launch rejected")
        registry = throwingActivityResultRegistry(platformFailure)
        FileKit.init(registry)

        val failure = assertFailsWith<FileKitDialogException> {
            runBlocking { openFileSaver() }
        }

        val cause = assertIs<SecurityException>(failure.cause)
        assertEquals(platformFailure.message, cause.message)
    }

    @Test
    fun AndroidFileSaver_cancellation_propagatesUnchanged() {
        val cancellation = CancellationException("Saver cancelled")
        registry = throwingActivityResultRegistry(cancellation)
        FileKit.init(registry)

        val failure = assertFailsWith<CancellationException> {
            runBlocking { openFileSaver() }
        }

        assertEquals(cancellation.message, failure.message)
    }

    @Test
    fun AndroidFileSaver_unexpectedFailure_propagatesUnchanged() {
        val defect = IllegalStateException("Unexpected saver defect")
        registry = throwingActivityResultRegistry(defect)
        FileKit.init(registry)

        val failure = assertFailsWith<IllegalStateException> {
            runBlocking { openFileSaver() }
        }

        assertEquals(defect.message, failure.message)
    }

    private suspend fun openFileSaver() =
        FileKit.openFileSaver(
            suggestedName = "document",
            defaultExtension = null,
        )

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
