package io.github.vinceglb.filekit.dialogs

internal fun openWithXdgOpen(path: String) {
    spawnLinuxProcess("xdg-open", listOf(xdgOpenFileArgument(path)))
}

// xdg-open interprets bare relative paths as options or URI schemes.
internal fun xdgOpenFileArgument(path: String): String =
    if (path.startsWith("/")) path else "./$path"

/** Starts a process and arranges to reap it without waiting for it on the calling thread. */
internal expect fun spawnLinuxProcess(executable: String, arguments: List<String>): Int
