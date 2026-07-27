package io.github.vinceglb.filekit.dialogs.platform.xdg

import com.sun.jna.Native
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogParent
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
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
internal class XdgFilePickerPortal : PlatformFilePicker {
    fun isAvailable(): Boolean {
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

    override suspend fun openFilePicker(
        fileExtensions: Set<String>?,
        directory: PlatformFile?,
        dialogSettings: FileKitDialogSettings,
    ): File? = openFilesPicker(
        directory = directory,
        fileExtensions = fileExtensions,
        title = dialogSettings.title,
        parent = dialogSettings.parent,
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
        parent = dialogSettings.parent,
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
        parent = dialogSettings.parent,
        multiple = false,
        openDirectory = true,
    )?.firstOrNull()

    private suspend fun openFilesPicker(
        directory: PlatformFile?,
        fileExtensions: Set<String>?,
        title: String?,
        parent: FileKitDialogParent?,
        multiple: Boolean,
        openDirectory: Boolean,
    ): List<File>? {
        DBusConnectionBuilder.forSessionBus().build().use { connection ->
            val handleToken = UUID.randomUUID().toString().replace("-", "")
            val options: MutableMap<String, Variant<*>> = HashMap()
            options["handle_token"] = Variant(handleToken)
            options["multiple"] = Variant(multiple)
            options["directory"] = Variant(openDirectory)
            fileExtensions?.let { options["filters"] = createFilterOption(it) }
            directory?.let { options["current_folder"] = createCurrentFolderOption(it) }

            val deferredResult = registerResponseHandler(connection, handleToken)
            getFileChooserObject(connection).OpenFile(
                parentWindow = xdgFileChooserParent(parent),
                title = title ?: "",
                options = options,
            )
            val files = deferredResult.await()?.map { File(it) }
            return files
        }
    }

    override suspend fun openFileSaver(
        suggestedName: String,
        defaultExtension: String?,
        allowedExtensions: Set<String>?,
        directory: PlatformFile?,
        dialogSettings: FileKitDialogSettings,
    ): File? {
        DBusConnectionBuilder.forSessionBus().build().use { connection ->
            val handleToken = UUID.randomUUID().toString().replace("-", "")
            val options: MutableMap<String, Variant<*>> = HashMap()
            options["handle_token"] = Variant(handleToken)

            options["current_name"] = when {
                defaultExtension != null -> Variant("$suggestedName.$defaultExtension")
                else -> Variant(suggestedName)
            }

            val filterExtensions = allowedExtensions ?: defaultExtension?.let { setOf(it) }
            filterExtensions?.let { options["filters"] = createFilterOption(it) }
            directory?.let { options["current_folder"] = createCurrentFolderOption(it) }

            val deferredResult = registerResponseHandler(connection, handleToken)
            getFileChooserObject(connection).SaveFile(
                parentWindow = xdgFileChooserParent(dialogSettings.parent),
                title = "",
                options = options,
            )

            return deferredResult.await()?.first()?.let { File(it) }
        }
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
        val handler = ResponseHandler(path) { uris ->
            result.complete(uris)
        }
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
    ) : DBusSigHandler<DBusSignal> {
        @Suppress("UNCHECKED_CAST")
        override fun handle(signal: DBusSignal) {
            if (path == signal.path) {
                val params = signal.parameters
                val response = params[0] as UInt32
                val results = params[1] as Map<String, Variant<*>>

                if (response.toInt() == 0) {
                    val uris = (results["uris"]!!.value as List<String>).map { path ->
                        path.toURI()
                    }
                    onComplete(uris)
                } else {
                    onComplete(null)
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

internal fun xdgFileChooserParent(parent: FileKitDialogParent?): String =
    parent.resolveXdgPortalParent(Native::getWindowID)
