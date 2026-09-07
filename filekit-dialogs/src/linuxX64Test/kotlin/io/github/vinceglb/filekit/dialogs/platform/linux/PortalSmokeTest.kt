@file:OptIn(ExperimentalForeignApi::class)

package io.github.vinceglb.filekit.dialogs.platform.linux

import dbus.DBUS_TYPE_STRING
import dbus.DBUS_TYPE_VARIANT
import dbus.DBusBusType
import dbus.DBusError
import dbus.DBusMessageIter
import dbus.dbus_bus_get_private
import dbus.dbus_connection_close
import dbus.dbus_connection_send_with_reply_and_block
import dbus.dbus_connection_unref
import dbus.dbus_error_free
import dbus.dbus_error_init
import dbus.dbus_message_iter_append_basic
import dbus.dbus_message_iter_get_arg_type
import dbus.dbus_message_iter_init
import dbus.dbus_message_iter_init_append
import dbus.dbus_message_new_method_call
import dbus.dbus_message_unref
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PortalSmokeTest {
    @Test
    fun portalConnection_readsFileChooserVersion() = memScoped {
        val error = alloc<DBusError>()
        dbus_error_init(error.ptr)
        val connection = dbus_bus_get_private(DBusBusType.DBUS_BUS_SESSION, error.ptr)
        dbus_error_free(error.ptr)
        if (connection == null) {
            println("SKIP: no session bus available")
            return@memScoped
        }
        try {
            val request = dbus_message_new_method_call(
                "org.freedesktop.portal.Desktop",
                "/org/freedesktop/portal/desktop",
                "org.freedesktop.DBus.Properties",
                "Get",
            )
            assertNotNull(request)
            try {
                val body = alloc<DBusMessageIter>()
                dbus_message_iter_init_append(request, body.ptr)
                val ifacePtr = alloc<CPointerVar<ByteVar>>()
                ifacePtr.value = "org.freedesktop.portal.FileChooser".cstr.ptr
                dbus_message_iter_append_basic(body.ptr, DBUS_TYPE_STRING, ifacePtr.ptr)
                val propPtr = alloc<CPointerVar<ByteVar>>()
                propPtr.value = "version".cstr.ptr
                dbus_message_iter_append_basic(body.ptr, DBUS_TYPE_STRING, propPtr.ptr)

                val replyError = alloc<DBusError>()
                dbus_error_init(replyError.ptr)
                val reply = dbus_connection_send_with_reply_and_block(connection, request, 1000, replyError.ptr)
                dbus_error_free(replyError.ptr)
                if (reply == null) {
                    println("SKIP: XDG desktop portal is not available on the session bus")
                    return@memScoped
                }
                try {
                    val iter = alloc<DBusMessageIter>()
                    dbus_message_iter_init(reply, iter.ptr)
                    assertTrue(dbus_message_iter_get_arg_type(iter.ptr) == DBUS_TYPE_VARIANT)
                    println("OK: portal FileChooser is available (reply type: ${dbus_message_iter_get_arg_type(iter.ptr)})")
                } finally {
                    dbus_message_unref(reply)
                }
            } finally {
                dbus_message_unref(request)
            }
        } finally {
            dbus_connection_close(connection)
            dbus_connection_unref(connection)
        }
    }
}
