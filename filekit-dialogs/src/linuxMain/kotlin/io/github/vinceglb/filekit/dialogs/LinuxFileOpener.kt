package io.github.vinceglb.filekit.dialogs

internal fun openWithXdgOpen(path: String) {
    spawnLinuxProcess("xdg-open", listOf(path))
}

/** Starts a process and arranges to reap it without waiting for it on the calling thread. */
internal expect fun spawnLinuxProcess(executable: String, arguments: List<String>): Int
