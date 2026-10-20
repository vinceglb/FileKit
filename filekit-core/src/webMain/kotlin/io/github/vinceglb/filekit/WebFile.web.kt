package io.github.vinceglb.filekit

import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.toDouble
import kotlin.time.Instant

/**
 * FileKit's web backing type for [PlatformFile].
 *
 * It wraps browser file snapshots, live origin-private file-system entries,
 * and virtual directories reconstructed from browser directory picker results.
 */
public sealed class WebFile {
    @OptIn(ExperimentalWasmJsInterop::class)
    public class FileWrapper(
        public val file: BrowserFile,
        path: String? = file.webkitRelativePath,
        public val parent: DirectoryWrapper? = null,
    ) : WebFile() {
        public val path: String = path?.takeIf { it.isNotBlank() } ?: file.name

        public val name: String
            get() = file.name

        public val type: String
            get() = file.type

        public val size: Long
            get() = file.size.toDouble().toLong()

        @Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")
        public val lastModified: Instant
            get() = Instant.fromEpochMilliseconds(file.lastModified.toDouble().toLong())
    }

    public class DirectoryWrapper internal constructor(
        public val name: String,
        public val path: String,
        public val parent: DirectoryWrapper?,
        internal val mutableChildren: MutableList<WebFile> = mutableListOf(),
    ) : WebFile() {
        public val children: List<WebFile>
            get() = mutableChildren

        public val lastModified: Instant = WEB_DIRECTORY_LAST_MODIFIED
    }

    /**
     * A file in the origin private file system (OPFS).
     *
     * Its metadata comes from a snapshot taken when the file was opened, listed, or last written by
     * FileKit. Changes made elsewhere, such as in another tab or worker, are visible after the file
     * is opened again.
     */
    @OptIn(ExperimentalWasmJsInterop::class)
    public class OriginPrivateFile internal constructor(
        internal val handle: FileSystemFileHandle,
        private var fileSnapshot: BrowserFile,
        public val path: String,
        public val parent: OriginPrivateDirectory?,
    ) : WebFile() {
        public val name: String
            get() = fileSnapshot.name

        public val type: String
            get() = fileSnapshot.type

        public val size: Long
            get() = fileSnapshot.size.toDouble().toLong()

        @Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")
        public val lastModified: Instant
            get() = Instant.fromEpochMilliseconds(fileSnapshot.lastModified.toDouble().toLong())

        internal fun updateSnapshot(fileSnapshot: BrowserFile) {
            this.fileSnapshot = fileSnapshot
        }
    }

    /** A directory in the origin private file system (OPFS). */
    public class OriginPrivateDirectory internal constructor(
        internal val handle: FileSystemDirectoryHandle,
        public val path: String,
        public val parent: OriginPrivateDirectory?,
    ) : WebFile() {
        public val name: String
            get() = handle.name
    }
}

internal val WEB_DIRECTORY_LAST_MODIFIED: Instant = Instant.fromEpochMilliseconds(0)

public fun WebFile.toPlatformFile(): PlatformFile = PlatformFile(this)
