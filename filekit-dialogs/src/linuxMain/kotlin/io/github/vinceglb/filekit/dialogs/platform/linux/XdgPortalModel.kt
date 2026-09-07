package io.github.vinceglb.filekit.dialogs.platform.linux

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.random.Random

/**
 * An operational failure while talking to the XDG desktop portal over D-Bus.
 *
 * This is the Linux native counterpart of the JVM [io.github.vinceglb.filekit.dialogs.platform.xdg.XdgPortalResponseException]
 * and is converted to a [io.github.vinceglb.filekit.dialogs.FileKitDialogException] by the platform actuals.
 */
internal class LinuxXdgPortalException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

/**
 * A file filter presented to the portal dialog.
 *
 * @property label The label shown in the portal file chooser.
 * @property patterns The glob patterns of the filter (e.g. `*.png`).
 */
internal data class PortalFileFilter(
    val label: String,
    val patterns: List<String>,
)

/**
 * The portal method to invoke on the `org.freedesktop.portal.FileChooser` interface.
 */
internal enum class PortalRequestMethod {
    OpenFile,
    SaveFile,
}

/**
 * A variant value carried inside the `a{sv}` options of a portal request.
 */
internal sealed class PortalVariant {
    internal abstract fun signature(): String

    internal data class Bool(
        val value: Boolean,
    ) : PortalVariant() {
        override fun signature(): String = "b"
    }

    internal data class Str(
        val value: String,
    ) : PortalVariant() {
        override fun signature(): String = "s"
    }

    internal data class Bytes(
        val value: ByteArray,
    ) : PortalVariant() {
        override fun signature(): String = "ay"
    }

    internal data class Filters(
        val value: List<PortalFileFilter>,
    ) : PortalVariant() {
        override fun signature(): String = "a(sa(us))"
    }
}

/**
 * Runs an XDG portal file chooser request on the session bus and waits for its [Response] signal.
 *
 * The implementation is provided per Linux target because the libdbus cinterop bindings are not
 * available in the shared `linuxMain` metadata compilation.
 *
 * @return The selected file URIs, or `null` when the user cancelled the dialog.
 * @throws LinuxXdgPortalException When the portal cannot be reached or rejects the request.
 */
internal expect fun runXdgPortalRequest(
    method: PortalRequestMethod,
    parentWindow: String,
    title: String,
    options: Map<String, PortalVariant>,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
): List<String>?

/**
 * Builds the `filters` option of the portal file chooser: a single "Supported files" filter listing
 * every extension plus one filter per extension, mirroring the JVM implementation.
 */
internal fun buildPortalFileFilters(extensions: Set<String>): List<PortalFileFilter> {
    val allExtensions = listOf(PortalFileFilter("Supported files", extensions.map { "*.$it" }))
    val individualExtensions = extensions.map { extension -> PortalFileFilter(extension, listOf("*.$extension")) }
    return allExtensions + individualExtensions
}

/**
 * Generates a `handle_token` for a portal request: a random lowercase hex string without dashes,
 * as expected by the portal request path `/org/freedesktop/portal/desktop/request/<sender>/<handle_token>`.
 */
internal fun generatePortalHandleToken(): String = buildString(capacity = 32) {
    repeat(32) {
        append(HEX_DIGITS[Random.nextInt(HEX_DIGITS.length)])
    }
}

private const val HEX_DIGITS = "0123456789abcdef"

/**
 * Resolves a portal [Response] code and its selected URIs into file paths.
 *
 * @return The decoded `file://` paths, or `null` when the user cancelled (response code 1).
 * @throws LinuxXdgPortalException When the portal reports a failure (response code 2) or a URI
 * cannot be resolved to a local file path.
 */
internal fun resolvePortalResponse(
    response: Int,
    uris: List<String>?,
): List<String>? = when (response) {
    0 -> {
        val resolved = uris.orEmpty().map { uri ->
            portalUriToFilePath(uri) ?: throw LinuxXdgPortalException(
                "The XDG portal returned a non-local URI that cannot be resolved to a file path: $uri",
            )
        }
        resolved
    }

    1 -> {
        null
    }

    2 -> {
        throw LinuxXdgPortalException("The XDG portal ended the request with response code 2.")
    }

    else -> {
        error("Unexpected XDG portal response code: $response")
    }
}

/**
 * Converts a portal `file://` URI into a local file path.
 *
 * @return The decoded absolute path, or `null` when the URI does not reference a local file.
 */
internal fun portalUriToFilePath(uri: String): String? {
    if (!uri.startsWith("file://")) return null
    val raw = uri.removePrefix("file://")
    return when {
        raw.startsWith("/") -> percentDecode(raw)
        raw.startsWith("localhost/") -> percentDecode(raw.removePrefix("localhost"))
        else -> null
    }
}

/**
 * Decodes percent-encoded characters in a URI. Encoded bytes are collected together with the UTF-8
 * bytes of literal characters and decoded as UTF-8, so non-ASCII filenames such as `café.txt`
 * survive the round trip.
 */
internal fun percentDecode(value: String): String {
    val bytes = mutableListOf<Byte>()
    var index = 0
    while (index < value.length) {
        val char = value[index]
        if (char == '%' && index + 3 <= value.length) {
            val hex = value.substring(index + 1, index + 3).toIntOrNull(16)
            if (hex != null) {
                bytes += hex.toByte()
                index += 3
                continue
            }
        }
        val end = if (char.isHighSurrogate() && index + 1 < value.length && value[index + 1].isLowSurrogate()) {
            index + 2
        } else {
            index + 1
        }
        value.substring(index, end).encodeToByteArray().forEach { bytes += it }
        index = end
    }
    return bytes.toByteArray().decodeToString()
}
