@file:OptIn(ExperimentalForeignApi::class)

package io.github.vinceglb.filekit.dialogs.platform.linux

import cnames.structs.DBusConnection
import cnames.structs.DBusMessage
import dbus.DBUS_TYPE_ARRAY
import dbus.DBUS_TYPE_BOOLEAN
import dbus.DBUS_TYPE_BYTE
import dbus.DBUS_TYPE_DICT_ENTRY
import dbus.DBUS_TYPE_OBJECT_PATH
import dbus.DBUS_TYPE_STRING
import dbus.DBUS_TYPE_STRUCT
import dbus.DBUS_TYPE_UINT32
import dbus.DBUS_TYPE_VARIANT
import dbus.DBusBusType
import dbus.DBusError
import dbus.DBusMessageIter
import dbus.dbus_bus_add_match
import dbus.dbus_bus_get_private
import dbus.dbus_connection_close
import dbus.dbus_connection_flush
import dbus.dbus_connection_get_is_connected
import dbus.dbus_connection_pop_message
import dbus.dbus_connection_read_write
import dbus.dbus_connection_send
import dbus.dbus_connection_send_with_reply_and_block
import dbus.dbus_connection_set_exit_on_disconnect
import dbus.dbus_connection_unref
import dbus.dbus_error_free
import dbus.dbus_error_init
import dbus.dbus_error_is_set
import dbus.dbus_message_get_path
import dbus.dbus_message_get_sender
import dbus.dbus_message_is_signal
import dbus.dbus_message_iter_append_basic
import dbus.dbus_message_iter_close_container
import dbus.dbus_message_iter_get_arg_type
import dbus.dbus_message_iter_get_basic
import dbus.dbus_message_iter_get_element_type
import dbus.dbus_message_iter_init
import dbus.dbus_message_iter_init_append
import dbus.dbus_message_iter_next
import dbus.dbus_message_iter_open_container
import dbus.dbus_message_iter_recurse
import dbus.dbus_message_new_method_call
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
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

// https://flatpak.github.io/xdg-desktop-portal/docs/doc-org.freedesktop.portal.FileChooser.html
// https://flatpak.github.io/xdg-desktop-portal/docs/doc-org.freedesktop.portal.Request.html
private const val PORTAL_DESTINATION = "org.freedesktop.portal.Desktop"
private const val PORTAL_PATH = "/org/freedesktop/portal/desktop"
private const val PORTAL_FILE_CHOOSER_INTERFACE = "org.freedesktop.portal.FileChooser"
private const val PORTAL_REQUEST_INTERFACE = "org.freedesktop.portal.Request"
private const val PORTAL_RESPONSE_MATCH_RULE =
    "type='signal',interface='org.freedesktop.portal.Request',member='Response'"

private const val PORTAL_OWNER_MATCH_RULE =
    "type='signal',sender='org.freedesktop.DBus',interface='org.freedesktop.DBus'," +
        "member='NameOwnerChanged',arg0='org.freedesktop.portal.Desktop'"

private const val NO_TIMEOUT = -1
private const val READ_WRITE_TIMEOUT_MS = 100

private fun MemScope.appendVariant(
    iter: CPointer<DBusMessageIter>,
    value: PortalVariant,
) {
    when (value) {
        is PortalVariant.Bool -> appendBoolean(iter, value.value)
        is PortalVariant.Str -> appendString(iter, value.value)
        is PortalVariant.Bytes -> appendBytes(iter, value.value)
        is PortalVariant.Filters -> appendFilters(iter, value.value)
    }
}

/**
 * Runs an XDG portal file chooser request on the session bus and waits for its [Response] signal.
 *
 * @return The selected file URIs, or `null` when the user cancelled the dialog.
 * @throws LinuxXdgPortalException When the portal cannot be reached or rejects the request.
 */
internal actual fun runXdgPortalRequest(
    method: PortalRequestMethod,
    parentWindow: String,
    title: String,
    options: Map<String, PortalVariant>,
    coroutineContext: CoroutineContext,
): List<String>? = memScoped {
    val error = alloc<DBusError>()
    dbus_error_init(error.ptr)
    var connection: CPointer<DBusConnection>? = null
    var request: CPointer<DBusMessage>? = null
    var reply: CPointer<DBusMessage>? = null
    var handlePath: String? = null
    try {
        coroutineContext.ensureActive()
        connection = dbus_bus_get_private(DBusBusType.DBUS_BUS_SESSION, error.ptr)
            ?: throw dbusOperationFailure(error, "Could not connect to the D-Bus session bus")
        // A library must report a lost bus to its caller, never terminate the application.
        dbus_connection_set_exit_on_disconnect(connection, 0u)

        for (rule in listOf(PORTAL_RESPONSE_MATCH_RULE, PORTAL_OWNER_MATCH_RULE)) {
            dbus_bus_add_match(connection, rule, error.ptr)
            if (dbus_error_is_set(error.ptr) != 0u) {
                throw dbusOperationFailure(error, "Could not subscribe to XDG portal events")
            }
        }
        dbus_connection_flush(connection)

        request = dbus_message_new_method_call(
            PORTAL_DESTINATION,
            PORTAL_PATH,
            PORTAL_FILE_CHOOSER_INTERFACE,
            when (method) {
                PortalRequestMethod.OpenFile -> "OpenFile"
                PortalRequestMethod.SaveFile -> "SaveFile"
            },
        ) ?: throw LinuxXdgPortalException("Could not allocate the XDG portal request")

        val handleToken = generatePortalHandleToken()
        val body = alloc<DBusMessageIter>()
        dbus_message_iter_init_append(request, body.ptr)
        appendString(body.ptr, parentWindow)
        appendString(body.ptr, title)
        appendOptions(body.ptr, options + ("handle_token" to PortalVariant.Str(handleToken)))

        reply = dbus_connection_send_with_reply_and_block(connection, request, NO_TIMEOUT, error.ptr)
            ?: throw dbusOperationFailure(error, "The XDG portal did not answer the ${method.name} request")

        handlePath = readRequestHandle(reply)
            ?: throw LinuxXdgPortalException("The XDG portal returned an invalid request handle")

        val portalOwner = dbus_message_get_sender(reply)?.toKString()
            ?: throw LinuxXdgPortalException("The XDG portal reply had no sender")
        awaitPortalResponse(connection, handlePath, coroutineContext, portalOwner)
    } catch (cancelled: CancellationException) {
        if (connection != null && handlePath != null) {
            closePortalRequest(connection, handlePath)
        }
        throw cancelled
    } finally {
        reply?.let { dbus_message_unref(it) }
        request?.let { dbus_message_unref(it) }
        connection?.let {
            dbus_connection_close(it)
            dbus_connection_unref(it)
        }
        dbus_error_free(error.ptr)
    }
}

private fun closePortalRequest(connection: CPointer<DBusConnection>, handlePath: String) {
    val close = dbus_message_new_method_call(
        PORTAL_DESTINATION,
        handlePath,
        PORTAL_REQUEST_INTERFACE,
        "Close",
    ) ?: return
    try {
        dbus_connection_send(connection, close, null)
        // Best effort, without blocking cancellation on a flush or a method reply.
        // Closing the private connection below also releases the caller's portal requests.
        dbus_connection_read_write(connection, 0)
    } finally {
        dbus_message_unref(close)
    }
}

// region Request construction

private fun MemScope.appendOptions(
    iter: CPointer<DBusMessageIter>,
    options: Map<String, PortalVariant>,
) {
    val dict = alloc<DBusMessageIter>()
    openContainerChecked(iter, DBUS_TYPE_ARRAY, "{sv}", dict.ptr)
    options.forEach { (key, value) ->
        val entry = alloc<DBusMessageIter>()
        openContainerChecked(dict.ptr, DBUS_TYPE_DICT_ENTRY, null, entry.ptr)
        appendString(entry.ptr, key)
        val variant = alloc<DBusMessageIter>()
        openContainerChecked(entry.ptr, DBUS_TYPE_VARIANT, value.signature(), variant.ptr)
        appendVariant(variant.ptr, value)
        closeContainerChecked(entry.ptr, variant.ptr)
        closeContainerChecked(dict.ptr, entry.ptr)
    }
    closeContainerChecked(iter, dict.ptr)
}

private fun MemScope.appendFilters(
    iter: CPointer<DBusMessageIter>,
    filters: List<PortalFileFilter>,
) {
    val filtersArray = alloc<DBusMessageIter>()
    openContainerChecked(iter, DBUS_TYPE_ARRAY, "(sa(us))", filtersArray.ptr)
    filters.forEach { filter ->
        val filterStruct = alloc<DBusMessageIter>()
        openContainerChecked(filtersArray.ptr, DBUS_TYPE_STRUCT, null, filterStruct.ptr)
        appendString(filterStruct.ptr, filter.label)
        val patternsArray = alloc<DBusMessageIter>()
        openContainerChecked(filterStruct.ptr, DBUS_TYPE_ARRAY, "(us)", patternsArray.ptr)
        filter.patterns.forEach { pattern ->
            val patternStruct = alloc<DBusMessageIter>()
            openContainerChecked(patternsArray.ptr, DBUS_TYPE_STRUCT, null, patternStruct.ptr)
            appendUInt32(patternStruct.ptr, 0u)
            appendString(patternStruct.ptr, pattern)
            closeContainerChecked(patternsArray.ptr, patternStruct.ptr)
        }
        closeContainerChecked(filterStruct.ptr, patternsArray.ptr)
        closeContainerChecked(filtersArray.ptr, filterStruct.ptr)
    }
    closeContainerChecked(iter, filtersArray.ptr)
}

private fun MemScope.appendBytes(
    iter: CPointer<DBusMessageIter>,
    bytes: ByteArray,
) {
    val array = alloc<DBusMessageIter>()
    openContainerChecked(iter, DBUS_TYPE_ARRAY, "y", array.ptr)
    val byte = alloc<ByteVar>()
    bytes.forEach { value ->
        byte.value = value
        if (dbus_message_iter_append_basic(array.ptr, DBUS_TYPE_BYTE, byte.ptr) == 0u) {
            throw LinuxXdgPortalException("Could not build the XDG portal request")
        }
    }
    closeContainerChecked(iter, array.ptr)
}

private fun MemScope.appendString(
    iter: CPointer<DBusMessageIter>,
    value: String,
) {
    val pointer = alloc<CPointerVar<ByteVar>>()
    pointer.value = value.cstr.ptr
    if (dbus_message_iter_append_basic(iter, DBUS_TYPE_STRING, pointer.ptr) == 0u) {
        throw LinuxXdgPortalException("Could not build the XDG portal request")
    }
}

private fun MemScope.appendBoolean(
    iter: CPointer<DBusMessageIter>,
    value: Boolean,
) {
    val boolean = alloc<UIntVar>()
    boolean.value = if (value) 1u else 0u
    if (dbus_message_iter_append_basic(iter, DBUS_TYPE_BOOLEAN, boolean.ptr) == 0u) {
        throw LinuxXdgPortalException("Could not build the XDG portal request")
    }
}

private fun MemScope.appendUInt32(
    iter: CPointer<DBusMessageIter>,
    value: UInt,
) {
    val uint = alloc<UIntVar>()
    uint.value = value
    if (dbus_message_iter_append_basic(iter, DBUS_TYPE_UINT32, uint.ptr) == 0u) {
        throw LinuxXdgPortalException("Could not build the XDG portal request")
    }
}

private fun MemScope.openContainerChecked(
    iter: CPointer<DBusMessageIter>,
    type: Int,
    containedSignature: String?,
    sub: CPointer<DBusMessageIter>,
) {
    if (dbus_message_iter_open_container(iter, type, containedSignature, sub) == 0u) {
        throw LinuxXdgPortalException("Could not build the XDG portal request")
    }
}

private fun MemScope.closeContainerChecked(
    iter: CPointer<DBusMessageIter>,
    sub: CPointer<DBusMessageIter>,
) {
    if (dbus_message_iter_close_container(iter, sub) == 0u) {
        throw LinuxXdgPortalException("Could not build the XDG portal request")
    }
}

// endregion

// region Response handling

private fun MemScope.readRequestHandle(
    reply: CPointer<DBusMessage>,
): String? {
    val iter = alloc<DBusMessageIter>()
    if (dbus_message_iter_init(reply, iter.ptr) == 0u) return null
    if (dbus_message_iter_get_arg_type(iter.ptr) != DBUS_TYPE_OBJECT_PATH) return null
    return readString(iter.ptr)
}

internal fun MemScope.awaitPortalResponse(
    connection: CPointer<DBusConnection>,
    handlePath: String,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    portalOwner: String? = null,
): List<String>? {
    while (true) {
        coroutineContext.ensureActive()
        if (dbus_connection_get_is_connected(connection) == 0u) {
            throw LinuxXdgPortalException(
                "The connection to the D-Bus session bus was lost while waiting for the XDG portal response",
            )
        }
        // Messages are consumed below; dispatching here could discard a queued Response.
        dbus_connection_read_write(connection, READ_WRITE_TIMEOUT_MS)

        while (true) {
            coroutineContext.ensureActive()
            val message = dbus_connection_pop_message(connection) ?: break
            try {
                if (portalOwner != null && portalOwnerWasLost(message, portalOwner)) {
                    throw LinuxXdgPortalException("The XDG portal service stopped while waiting for its response")
                }
                if (dbus_message_is_signal(message, PORTAL_REQUEST_INTERFACE, "Response") != 0u) {
                    val path = dbus_message_get_path(message)?.toKString()
                    if (path == handlePath) {
                        return parsePortalResponse(message)
                    }
                }
            } finally {
                dbus_message_unref(message)
            }
        }
    }
}

internal fun MemScope.portalOwnerWasLost(message: CPointer<DBusMessage>, expectedOwner: String): Boolean {
    if (dbus_message_is_signal(message, "org.freedesktop.DBus", "NameOwnerChanged") == 0u ||
        dbus_message_get_sender(message)?.toKString() != "org.freedesktop.DBus"
    ) {
        return false
    }
    val iter = alloc<DBusMessageIter>()
    if (dbus_message_iter_init(message, iter.ptr) == 0u) return false
    if (dbus_message_iter_get_arg_type(iter.ptr) != DBUS_TYPE_STRING) return false
    if (readString(iter.ptr) != PORTAL_DESTINATION) return false
    dbus_message_iter_next(iter.ptr)
    if (dbus_message_iter_get_arg_type(iter.ptr) != DBUS_TYPE_STRING) return false
    if (readString(iter.ptr) != expectedOwner) return false
    dbus_message_iter_next(iter.ptr)
    if (dbus_message_iter_get_arg_type(iter.ptr) != DBUS_TYPE_STRING) return false
    return readString(iter.ptr) != expectedOwner
}

internal fun MemScope.parsePortalResponse(
    message: CPointer<DBusMessage>,
): List<String>? {
    val iter = alloc<DBusMessageIter>()
    if (dbus_message_iter_init(message, iter.ptr) == 0u) {
        throw LinuxXdgPortalException("The XDG portal response could not be read")
    }
    if (dbus_message_iter_get_arg_type(iter.ptr) != DBUS_TYPE_UINT32) {
        throw LinuxXdgPortalException("The XDG portal response had an unexpected signature")
    }
    val response = readUInt32(iter.ptr)
    dbus_message_iter_next(iter.ptr)
    val uris = readUrisOption(iter.ptr)
    return resolvePortalResponse(response.toInt(), uris)
}

private fun MemScope.readUrisOption(
    iter: CPointer<DBusMessageIter>,
): List<String>? {
    if (dbus_message_iter_get_arg_type(iter) != DBUS_TYPE_ARRAY) return null
    val dict = alloc<DBusMessageIter>()
    dbus_message_iter_recurse(iter, dict.ptr)
    var uris: List<String>? = null
    while (dbus_message_iter_get_arg_type(dict.ptr) == DBUS_TYPE_DICT_ENTRY) {
        val entry = alloc<DBusMessageIter>()
        dbus_message_iter_recurse(dict.ptr, entry.ptr)
        val key = readString(entry.ptr)
        dbus_message_iter_next(entry.ptr)
        val value = readStringArrayVariant(entry.ptr)
        if (key == "uris") {
            uris = value
        }
        dbus_message_iter_next(dict.ptr)
    }
    return uris
}

private fun MemScope.readStringArrayVariant(
    iter: CPointer<DBusMessageIter>,
): List<String>? {
    if (dbus_message_iter_get_arg_type(iter) != DBUS_TYPE_VARIANT) return null
    val variant = alloc<DBusMessageIter>()
    dbus_message_iter_recurse(iter, variant.ptr)
    return readStringArray(variant.ptr)
}

private fun MemScope.readStringArray(
    iter: CPointer<DBusMessageIter>,
): List<String>? {
    if (dbus_message_iter_get_arg_type(iter) != DBUS_TYPE_ARRAY) return null
    if (dbus_message_iter_get_element_type(iter) != DBUS_TYPE_STRING) return null
    val array = alloc<DBusMessageIter>()
    dbus_message_iter_recurse(iter, array.ptr)
    val result = mutableListOf<String>()
    while (dbus_message_iter_get_arg_type(array.ptr) == DBUS_TYPE_STRING) {
        readString(array.ptr)?.let { result += it }
        dbus_message_iter_next(array.ptr)
    }
    return result
}

private fun MemScope.readUInt32(
    iter: CPointer<DBusMessageIter>,
): UInt {
    val value = alloc<UIntVar>()
    dbus_message_iter_get_basic(iter, value.ptr)
    return value.value
}

private fun MemScope.readString(
    iter: CPointer<DBusMessageIter>,
): String? {
    val pointer = alloc<CPointerVar<ByteVar>>()
    dbus_message_iter_get_basic(iter, pointer.ptr)
    return pointer.value?.toKString()
}

// endregion

private fun dbusOperationFailure(
    error: DBusError,
    context: String,
): LinuxXdgPortalException {
    val message = error.message?.toKString()
    return if (message.isNullOrBlank()) {
        LinuxXdgPortalException(context)
    } else {
        LinuxXdgPortalException("$context: $message")
    }
}
