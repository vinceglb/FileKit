@file:OptIn(ExperimentalForeignApi::class)
@file:Suppress("ktlint:standard:function-naming")

package io.github.vinceglb.filekit.dialogs.platform.linux

import cnames.structs.DBusConnection
import cnames.structs.DBusMessage
import dbus.DBUS_MESSAGE_TYPE_SIGNAL
import dbus.DBUS_TYPE_ARRAY
import dbus.DBUS_TYPE_DICT_ENTRY
import dbus.DBUS_TYPE_STRING
import dbus.DBUS_TYPE_UINT32
import dbus.DBUS_TYPE_VARIANT
import dbus.DBusBusType
import dbus.DBusError
import dbus.DBusMessageIter
import dbus.dbus_bus_get_private
import dbus.dbus_bus_get_unique_name
import dbus.dbus_connection_close
import dbus.dbus_connection_pop_message
import dbus.dbus_connection_send
import dbus.dbus_connection_send_with_reply_and_block
import dbus.dbus_connection_unref
import dbus.dbus_error_free
import dbus.dbus_error_init
import dbus.dbus_message_iter_append_basic
import dbus.dbus_message_iter_close_container
import dbus.dbus_message_iter_init_append
import dbus.dbus_message_iter_open_container
import dbus.dbus_message_new
import dbus.dbus_message_new_method_call
import dbus.dbus_message_set_destination
import dbus.dbus_message_set_interface
import dbus.dbus_message_set_member
import dbus.dbus_message_set_path
import dbus.dbus_message_unref
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.UIntVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PortalResponseParsingTest {
    @Test
    fun PortalResponse_withUris_returnsDecodedPaths() = memScoped {
        val message = allocResponseMessage(listOf("file:///home/user/a%20b.txt", "file:///tmp/photo.png"))
        try {
            val paths = parsePortalResponse(message)

            assertEquals(listOf("/home/user/a b.txt", "/tmp/photo.png"), paths)
        } finally {
            dbus_message_unref(message)
        }
    }

    @Test
    fun PortalResponse_withoutUris_returnsEmptyList() = memScoped {
        val message = allocResponseMessage(emptyList())
        try {
            assertEquals(emptyList(), parsePortalResponse(message))
        } finally {
            dbus_message_unref(message)
        }
    }

    @Test
    fun PortalResponse_cancelledCode_returnsNull() = memScoped {
        val message = allocResponseMessage(uris = null, response = 1)
        try {
            assertNull(parsePortalResponse(message))
        } finally {
            dbus_message_unref(message)
        }
    }

    @Test
    fun PortalResponse_failureCode_throwsOperationalFailure() = memScoped {
        val message = allocResponseMessage(uris = null, response = 2)
        try {
            val failure = runCatching { parsePortalResponse(message) }.exceptionOrNull()

            assertEquals(LinuxXdgPortalException::class, failure?.let { it::class })
            assertEquals("The XDG portal ended the request with response code 2.", failure?.message)
        } finally {
            dbus_message_unref(message)
        }
    }

    @Test
    fun PortalResponse_alreadyQueued_returnsFirstResponse() = withQueuedResponses { connection, handle ->
        assertEquals(listOf("/tmp/first"), awaitPortalResponse(connection, handle))
    }

    @Test
    fun PortalResponse_cancelledJob_propagatesCancellationBeforeConsumingResponse() = withQueuedResponses { connection, handle ->
        val job = Job().also { it.cancel() }
        assertFailsWith<CancellationException> {
            awaitPortalResponse(connection, handle, job)
        }
        assertEquals(listOf("/tmp/first"), awaitPortalResponse(connection, handle))
    }

    private fun withQueuedResponses(block: MemScope.(CPointer<DBusConnection>, String) -> Unit) = memScoped {
        val error = alloc<DBusError>()
        dbus_error_init(error.ptr)
        val connection = dbus_bus_get_private(DBusBusType.DBUS_BUS_SESSION, error.ptr)
        dbus_error_free(error.ptr)
        if (connection == null) {
            println("SKIP: run with dbus-run-session to test queued portal responses")
            return@memScoped
        }
        try {
            // Round-trip to the bus, then remove initial signals such as NameAcquired.
            busBarrier(connection)
            while (true) {
                val message = dbus_connection_pop_message(connection) ?: break
                dbus_message_unref(message)
            }
            val destination = assertNotNull(dbus_bus_get_unique_name(connection)?.toKString())
            val handle = "/org/freedesktop/portal/desktop/request/test/queued"
            // A second response makes the old dispatch-before-pop loop fail rather than hang:
            // it discards the first response and returns the second one's path.
            for (path in listOf("first", "second")) {
                val message = allocResponseMessage(listOf("file:///tmp/$path"))
                try {
                    assertTrue(dbus_message_set_path(message, handle) != 0u)
                    assertTrue(dbus_message_set_interface(message, "org.freedesktop.portal.Request") != 0u)
                    assertTrue(dbus_message_set_member(message, "Response") != 0u)
                    assertTrue(dbus_message_set_destination(message, destination) != 0u)
                    assertTrue(dbus_connection_send(connection, message, null) != 0u)
                } finally {
                    dbus_message_unref(message)
                }
            }
            // The bus processes our messages in order, so both signals precede this reply.
            busBarrier(connection)

            block(connection, handle)
        } finally {
            dbus_connection_close(connection)
            dbus_connection_unref(connection)
        }
    }

    private fun MemScope.busBarrier(connection: CPointer<DBusConnection>) {
        val request = assertNotNull(
            dbus_message_new_method_call(
                "org.freedesktop.DBus",
                "/org/freedesktop/DBus",
                "org.freedesktop.DBus",
                "GetId",
            ),
        )
        try {
            val error = alloc<DBusError>()
            dbus_error_init(error.ptr)
            try {
                val reply = assertNotNull(dbus_connection_send_with_reply_and_block(connection, request, 1000, error.ptr))
                dbus_message_unref(reply)
            } finally {
                dbus_error_free(error.ptr)
            }
        } finally {
            dbus_message_unref(request)
        }
    }

    /**
     * Builds a `(ua{sv})` portal Response message the way `xdg-desktop-portal` sends it: a response
     * code followed by a dict that may hold a `uris` variant with an array of `file://` strings.
     */
    private fun MemScope.allocResponseMessage(
        uris: List<String>?,
        response: Int = 0,
    ): CPointer<DBusMessage> {
        val message = dbus_message_new(DBUS_MESSAGE_TYPE_SIGNAL)
            ?: error("Could not allocate the response test message")
        val body = alloc<DBusMessageIter>()
        dbus_message_iter_init_append(message, body.ptr)
        appendUInt32(body.ptr, response.toUInt())

        val dict = alloc<DBusMessageIter>()
        if (dbus_message_iter_open_container(body.ptr, DBUS_TYPE_ARRAY, "{sv}", dict.ptr) == 0u) {
            error("Could not open the response dict")
        }
        if (uris != null) {
            val entry = alloc<DBusMessageIter>()
            if (dbus_message_iter_open_container(dict.ptr, DBUS_TYPE_DICT_ENTRY, null, entry.ptr) == 0u) {
                error("Could not open the response dict entry")
            }
            appendString(entry.ptr, "uris")
            val variant = alloc<DBusMessageIter>()
            if (dbus_message_iter_open_container(entry.ptr, DBUS_TYPE_VARIANT, "as", variant.ptr) == 0u) {
                error("Could not open the response uris variant")
            }
            val array = alloc<DBusMessageIter>()
            if (dbus_message_iter_open_container(variant.ptr, DBUS_TYPE_ARRAY, "s", array.ptr) == 0u) {
                error("Could not open the response uris array")
            }
            uris.forEach { uri -> appendString(array.ptr, uri) }
            if (dbus_message_iter_close_container(variant.ptr, array.ptr) == 0u) {
                error("Could not close the response uris array")
            }
            if (dbus_message_iter_close_container(entry.ptr, variant.ptr) == 0u) {
                error("Could not close the response uris variant")
            }
            if (dbus_message_iter_close_container(dict.ptr, entry.ptr) == 0u) {
                error("Could not close the response dict entry")
            }
        }
        if (dbus_message_iter_close_container(body.ptr, dict.ptr) == 0u) {
            error("Could not close the response dict")
        }
        return message
    }

    private fun MemScope.appendUInt32(
        iter: CPointer<DBusMessageIter>,
        value: UInt,
    ) {
        val uint = alloc<UIntVar>()
        uint.value = value
        if (dbus_message_iter_append_basic(iter, DBUS_TYPE_UINT32, uint.ptr) == 0u) {
            error("Could not append the response code")
        }
    }

    private fun MemScope.appendString(
        iter: CPointer<DBusMessageIter>,
        value: String,
    ) {
        val pointer = alloc<CPointerVar<ByteVar>>()
        pointer.value = value.cstr.ptr
        if (dbus_message_iter_append_basic(iter, DBUS_TYPE_STRING, pointer.ptr) == 0u) {
            error("Could not append the response string")
        }
    }
}
