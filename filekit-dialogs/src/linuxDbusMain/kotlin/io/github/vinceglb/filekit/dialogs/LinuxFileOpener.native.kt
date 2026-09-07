@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.github.vinceglb.filekit.dialogs

import filekit.process.filekit_spawn_process
import io.github.vinceglb.filekit.exceptions.FileKitException
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.set
import kotlinx.cinterop.value
import platform.posix.pid_tVar

internal actual fun spawnLinuxProcess(executable: String, arguments: List<String>): Int = memScoped {
    val argv = allocArray<CPointerVar<ByteVar>>(arguments.size + 2)
    argv[0] = executable.cstr.ptr
    arguments.forEachIndexed { index, argument -> argv[index + 1] = argument.cstr.ptr }
    argv[arguments.size + 1] = null
    val child = alloc<pid_tVar>()
    val error = filekit_spawn_process(child.ptr, executable, argv)
    if (error != 0) {
        throw FileKitException("Could not open the file with the default application (error $error).")
    }
    child.value
}
