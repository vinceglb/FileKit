@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit.dialogs

import android.content.ActivityNotFoundException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class AndroidSharingFailureTest {
    @Test
    fun AndroidSharing_missingActivity_throwsDialogOperationalFailureWithCause() {
        val platformFailure = ActivityNotFoundException("No sharing activity")

        val failure = assertFailsWith<FileKitDialogException> {
            launchAndroidShareIntent {
                throw platformFailure
            }
        }

        assertEquals("No Android activity is available to share the selected files.", failure.message)
        assertSame(platformFailure, failure.cause)
    }

    @Test
    fun AndroidSharing_securityRejection_throwsDialogOperationalFailureWithCause() {
        val platformFailure = SecurityException("Sharing launch rejected")

        val failure = assertFailsWith<FileKitDialogException> {
            launchAndroidShareIntent {
                throw platformFailure
            }
        }

        assertEquals("Android rejected the sharing launch.", failure.message)
        assertSame(platformFailure, failure.cause)
    }

    @Test
    fun AndroidSharing_unexpectedFailure_propagates() {
        val platformFailure = IllegalStateException("Unexpected sharing defect")

        val failure = assertFailsWith<IllegalStateException> {
            launchAndroidShareIntent {
                throw platformFailure
            }
        }

        assertSame(platformFailure, failure)
    }
}
