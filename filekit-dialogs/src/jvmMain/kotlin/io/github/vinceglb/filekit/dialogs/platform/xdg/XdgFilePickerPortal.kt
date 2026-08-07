package io.github.vinceglb.filekit.dialogs.platform.xdg

import com.sun.jna.Native
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogException
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.FileKitPickerException
import io.github.vinceglb.filekit.dialogs.platform.PlatformFilePicker
import io.github.vinceglb.filekit.dialogs.resolveXdgPortalParent
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.CompletableDeferred
import org.freedesktop.dbus.DBusMatchRule
import org.freedesktop.dbus.DBusPath
import org.freedesktop.dbus.Tuple
import org.freedesktop.dbus.annotations.DBusBoundProperty
import org.freedesktop.dbus.annotations.DBusInterfaceName
import org.freedesktop.dbus.annotations.DBusProperty.Access
import org.freedesktop.dbus.annotations.Position
import org.freedesktop.dbus.connections.impl.DBusConnection
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder
import org.freedesktop.dbus.exceptions.DBusException
import org.freedesktop.dbus.exceptions.DBusExecutionException
import org.freedesktop.dbus.interfaces.DBusInterface
import org.freedesktop.dbus.interfaces.DBusSigHandler
import org.freedesktop.dbus.interfaces.Properties
import org.freedesktop.dbus.messages.DBusSignal
import org.freedesktop.dbus.types.UInt32
import org.freedesktop.dbus.types.Variant
import java.io.File
import java.lang.reflect.InvocationTargetException
import java.net.URI
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

// https://flatpak.github.io/xdg-desktop-portal/docs/doc-org.freedesktop.portal.FileChooser.html
internal class XdgFilePickerPortal(
    private val transport: XdgFileChooserTransport = DbusXdgFileChooserTransport(),
) : PlatformFilePicker {
    fun isAvailable(): Boolean = transport.isAvailable()

    override suspend fun openFilePicker(
        fileExtensions: Set<String>?,
        directory: PlatformFile?,
        dialogSettings: FileKitDialogSettings,
    ): File? = openFilesPicker(
        directory = directory,
        fileExtensions = fileExtensions,
        title = dialogSettings.title,
        parentWindow = dialogSettings.resolveXdgPortalParent(),
        multiple = false,
        openDirectory = false,
    )?.firstOrNull()

    override suspend fun openFilesPicker(
        fileExtensions: Set<String>?,
        directory: PlatformFile?,
        dialogSettings: FileKitDialogSettings,
    ): List<File>? = openFilesPicker(
        directory = directory,
        fileExtensions = fileExtensions,
        title = dialogSettings.title,
        parentWindow = dialogSettings.resolveXdgPortalParent(),
        multiple = true,
        openDirectory = false,
    )

    override suspend fun openDirectoryPicker(
        directory: PlatformFile?,
        dialogSettings: FileKitDialogSettings,
    ): File? = openFilesPicker(
        directory = directory,
        fileExtensions = null,
        title = dialogSettings.title,
        parentWindow = dialogSettings.resolveXdgPortalParent(),
        multiple = false,
        openDirectory = true,
    )?.firstOrNull()

    private suspend fun openFilesPicker(
        directory: PlatformFile?,
        fileExtensions: Set<String>?,
        title: String?,
        parentWindow: String,
        multiple: Boolean,
        openDirectory: Boolean,
    ): List<File>? {
        val options: MutableMap<String, Variant<*>> = HashMap()
        options["multiple"] = Variant(multiple)
        options["directory"] = Variant(openDirectory)
        fileExtensions?.let { options["filters"] = createFilterOption(it) }
        directory?.let { options["current_folder"] = createCurrentFolderOption(it) }

        return runXdgRequest(
            toFailure = if (openDirectory) {
                Throwable::toDirectoryPickerFailure
            } else {
                Throwable::toFilePickerFailure
            },
        ) {
            transport.openFile(
                parentWindow = parentWindow,
                title = title ?: "",
                options = options,
            )
        }?.map { File(it) }
    }

    override suspend fun openFileSaver(
        suggestedName: String,
        defaultExtension: String?,
        allowedExtensions: Set<String>?,
        directory: PlatformFile?,
        dialogSettings: FileKitDialogSettings,
    ): File? {
        val options: MutableMap<String, Variant<*>> = HashMap()
        options["current_name"] = when {
            defaultExtension != null -> Variant("$suggestedName.$defaultExtension")
            else -> Variant(suggestedName)
        }

        val filterExtensions = allowedExtensions ?: defaultExtension?.let { setOf(it) }
        filterExtensions?.let { options["filters"] = createFilterOption(it) }
        directory?.let { options["current_folder"] = createCurrentFolderOption(it) }

        val parentWindow = dialogSettings.resolveXdgPortalParent()
        return runXdgRequest(Throwable::toFileSaverFailure) {
            transport.saveFile(
                parentWindow = parentWindow,
                title = "",
                options = options,
            )
        }?.first()
            ?.let { File(it) }
    }

    private fun FileKitDialogSettings.resolveXdgPortalParent(): String =
        parent.resolveXdgPortalParent(Native::getWindowID)

    private fun createFilterOption(extensions: Set<String>): Variant<*> {
        val allExtensions = Pair("Supported files", extensions.map { extension -> Pair(0, "*.$extension") })
        val individualExtensions = extensions.map { extension -> Pair(extension, listOf(Pair(0, "*.$extension"))) }
        return Variant(
            listOf(allExtensions) + individualExtensions,
            "a(sa(us))",
        )
    }

    private fun createCurrentFolderOption(currentFolder: PlatformFile): Variant<*> {
        val stringBytes = currentFolder.path.encodeToByteArray()
        val nullTerminated = ByteArray(stringBytes.size + 1)
        System.arraycopy(stringBytes, 0, nullTerminated, 0, stringBytes.size)
        return Variant(nullTerminated)
    }
}

private fun Throwable.toFilePickerFailure(): FileKitPickerException = FileKitPickerException(
    message = "The XDG file picker could not complete the operation.",
    cause = this,
)

private fun Throwable.toDirectoryPickerFailure(): FileKitDialogException = FileKitDialogException(
    message = "The XDG directory picker could not complete the operation.",
    cause = this,
)

private fun Throwable.toFileSaverFailure(): FileKitDialogException = FileKitDialogException(
    message = "The XDG file saver could not complete the operation.",
    cause = this,
)

private suspend fun <T> runXdgRequest(
    toFailure: (Throwable) -> FileKitDialogException,
    request: suspend () -> T,
): T = try {
    request()
} catch (failure: DBusExecutionException) {
    throw toFailure(failure)
} catch (failure: DBusException) {
    throw toFailure(failure)
} catch (failure: XdgPortalResponseException) {
    throw toFailure(failure)
}

internal class XdgPortalResponseException(
    internal val response: Int,
) : RuntimeException("The XDG portal ended the request with response code $response.")

internal fun resolveXdgPortalResponse(
    response: Int,
    results: Map<String, Variant<*>>,
): List<URI>? = when (response) {
    0 -> {
        @Suppress("UNCHECKED_CAST")
        (results["uris"]!!.value as List<String>).map { path -> path.toURI() }
    }

    1 -> {
        null
    }

    2 -> {
        throw XdgPortalResponseException(response)
    }

    else -> {
        error("Unexpected XDG portal response code: $response")
    }
}

internal interface XdgFileChooserTransport {
    fun isAvailable(): Boolean

    suspend fun openFile(
        parentWindow: String,
        title: String,
        options: MutableMap<String, Variant<*>>,
    ): List<URI>?

    suspend fun saveFile(
        parentWindow: String,
        title: String,
        options: MutableMap<String, Variant<*>>,
    ): List<URI>?
}

private class DbusXdgFileChooserTransport : XdgFileChooserTransport {
    override fun isAvailable(): Boolean {
        try {
            DBusConnectionBuilder.forSessionBus().build().use { connection ->
                connection
                    .getRemoteObject(
                        "org.freedesktop.portal.Desktop",
                        "/org/freedesktop/portal/desktop",
                        Properties::class.java,
                    ).Get<UInt32>("org.freedesktop.portal.FileChooser", "version")
                return true
            }
        } catch (_: Exception) {
            return false
        }
    }

    override suspend fun openFile(
        parentWindow: String,
        title: String,
        options: MutableMap<String, Variant<*>>,
    ): List<URI>? = executeRequest(options) { fileChooser, requestOptions ->
        fileChooser.OpenFile(
            parentWindow = parentWindow,
            title = title,
            options = requestOptions,
        )
    }

    override suspend fun saveFile(
        parentWindow: String,
        title: String,
        options: MutableMap<String, Variant<*>>,
    ): List<URI>? = executeRequest(options) { fileChooser, requestOptions ->
        fileChooser.SaveFile(
            parentWindow = parentWindow,
            title = title,
            options = requestOptions,
        )
    }

    private suspend fun executeRequest(
        options: MutableMap<String, Variant<*>>,
        request: (FileChooserDbusInterface, MutableMap<String, Variant<*>>) -> Unit,
    ): List<URI>? = DBusConnectionBuilder.forSessionBus().build().use { connection ->
        val handleToken = UUID.randomUUID().toString().replace("-", "")
        val requestOptions = HashMap(options)
        requestOptions["handle_token"] = Variant(handleToken)

        val deferredResult = registerResponseHandler(connection, handleToken)
        request(getFileChooserObject(connection), requestOptions)
        deferredResult.await()
    }

    // https://flatpak.github.io/xdg-desktop-portal/docs/doc-org.freedesktop.portal.Request.html
    private fun registerResponseHandler(
        connection: DBusConnection,
        handleToken: String,
    ): CompletableDeferred<List<URI>?> {
        val sender = connection.uniqueName.substring(1).replace('.', '_')
        val path = "/org/freedesktop/portal/desktop/request/$sender/$handleToken"

        val result = CompletableDeferred<List<URI>?>()
        val matchRule = DBusMatchRule("signal", "org.freedesktop.portal.Request", "Response")
        val registration = AtomicReference<AutoCloseable?>(null)
        val handler = ResponseHandler(
            path = path,
            onComplete = { uris -> result.complete(uris) },
            onFailure = { failure -> result.completeExceptionally(failure) },
        )
        registration.set(
            addGenericSigHandlerCompat(
                connection = connection,
                matchRule = matchRule,
                handler = handler,
            ),
        )
        result.invokeOnCompletion {
            registration.getAndSet(null).safeClose()
        }
        return result
    }

    private class ResponseHandler(
        private val path: String,
        private val onComplete: (result: List<URI>?) -> Unit,
        private val onFailure: (failure: XdgPortalResponseException) -> Unit,
    ) : DBusSigHandler<DBusSignal> {
        @Suppress("UNCHECKED_CAST")
        override fun handle(signal: DBusSignal) {
            if (path == signal.path) {
                val params = signal.parameters
                val response = params[0] as UInt32
                val results = params[1] as Map<String, Variant<*>>

                try {
                    onComplete(resolveXdgPortalResponse(response.toInt(), results))
                } catch (failure: XdgPortalResponseException) {
                    onFailure(failure)
                }
            }
        }
    }

    private fun addGenericSigHandlerCompat(
        connection: DBusConnection,
        matchRule: DBusMatchRule,
        handler: DBusSigHandler<DBusSignal>,
    ): AutoCloseable {
        val method = connection.javaClass.methods.firstOrNull { candidate ->
            candidate.name == "addGenericSigHandler" &&
                candidate.parameterTypes.size == 2 &&
                candidate.parameterTypes[0].isAssignableFrom(matchRule.javaClass) &&
                candidate.parameterTypes[1].isAssignableFrom(handler.javaClass)
        } ?: connection.javaClass.methods.firstOrNull { candidate ->
            candidate.name == "addSigHandler" &&
                candidate.parameterTypes.size == 2 &&
                candidate.parameterTypes[0].isAssignableFrom(matchRule.javaClass) &&
                candidate.parameterTypes[1].isAssignableFrom(handler.javaClass)
        } ?: error("No compatible DBusConnection signal-registration method found")

        try {
            val registration = method.invoke(connection, matchRule, handler)
            return registration as? AutoCloseable
                ?: error("DBusConnection signal-registration method did not return AutoCloseable")
        } catch (error: InvocationTargetException) {
            throw error.targetException
        }
    }

    private fun AutoCloseable?.safeClose() {
        try {
            this?.close()
        } catch (_: Exception) {
            // Signal handler may already be removed; ignore cleanup failures.
        }
    }

    private fun getFileChooserObject(connection: DBusConnection) = connection.getRemoteObject(
        "org.freedesktop.portal.Desktop",
        "/org/freedesktop/portal/desktop",
        FileChooserDbusInterface::class.java,
    )
}

@DBusInterfaceName(value = "org.freedesktop.portal.FileChooser")
@Suppress("FunctionName")
internal interface FileChooserDbusInterface : DBusInterface {
    fun OpenFile(
        parentWindow: String,
        title: String,
        options: MutableMap<String, Variant<*>>,
    ): DBusPath

    fun SaveFile(
        parentWindow: String,
        title: String,
        options: MutableMap<String, Variant<*>>,
    ): DBusPath

    fun SaveFiles(
        parentWindow: String,
        title: String,
        options: MutableMap<String, Variant<*>>,
    ): DBusPath

    @DBusBoundProperty(name = "version", access = Access.READ)
    fun GetVersion(): UInt32
}

internal class Pair<A, B>(
    @field:Position(0) val a: A,
    @field:Position(1) val b: B,
) : Tuple()

internal fun String.toURI(): URI =
    this
        .replace(" ", "%20")
        .replace("[", "%5B")
        .replace("]", "%5D")
        .let { URI(it) }
