@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit.dialogs.platform.xdg

import io.github.vinceglb.filekit.dialogs.platform.JvmDialogOperationException
import kotlinx.coroutines.test.runTest
import org.freedesktop.dbus.exceptions.DBusExecutionException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame

class XdgPortalResponseTest {
    @Test
    fun XdgPortalResponse_success_returnsUris() {
        assertEquals(
            listOf("file:///tmp/example.txt"),
            resolveXdgPortalResponse(0, listOf("file:///tmp/example.txt"))?.map { it.toString() },
        )
    }

    @Test
    fun XdgPortalResponse_cancel_returnsNull() {
        assertNull(resolveXdgPortalResponse(1, null))
    }

    @Test
    fun XdgPortalResponse_error_throwsOperationFailure() {
        assertFailsWith<JvmDialogOperationException> {
            resolveXdgPortalResponse(2, null)
        }
    }

    @Test
    fun XdgPortalOperation_dbusFailure_wrapsCause() = runTest {
        val dbusFailure = DBusExecutionException("Portal unavailable")

        val failure = assertFailsWith<JvmDialogOperationException> {
            runXdgPortalOperation<Unit> { throw dbusFailure }
        }

        assertSame(dbusFailure, failure.cause)
    }
}
