@file:Suppress("ktlint:standard:function-naming")

package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.PlatformFile
import kotlin.test.Test
import kotlin.test.assertEquals

class LinuxCurrentFolderTest {
    @Test
    fun CurrentFolder_unicodePaths_preserveUtf8AndNullTerminator() {
        for (path in listOf("/tmp/plain", "/tmp/café", "/tmp/中文", "/tmp/📁")) {
            val bytes = createCurrentFolderOption(PlatformFile(path)).value

            assertEquals(0.toByte(), bytes.last(), "Missing terminator for $path")
            assertEquals(path, bytes.decodeToString(endIndex = bytes.lastIndex))
        }
    }
}
