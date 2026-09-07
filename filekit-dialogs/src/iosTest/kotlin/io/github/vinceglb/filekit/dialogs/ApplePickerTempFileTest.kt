@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.utils.toByteArray
import io.github.vinceglb.filekit.utils.toNSData
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager
import platform.Foundation.NSUUID
import platform.Foundation.temporaryDirectory
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalForeignApi::class)
class ApplePickerTempFileTest {
    @Test
    fun ApplePicker_sameFilename_preservesBothFilesAndContents() {
        val fileManager = NSFileManager.defaultManager
        val id = NSUUID().UUIDString
        val root = assertNotNull(fileManager.temporaryDirectory.URLByAppendingPathComponent(id))
        val contents = listOf(byteArrayOf(1, 2, 3), byteArrayOf(4, 5, 6))

        try {
            val sources = contents.mapIndexed { index, bytes ->
                val directory = assertNotNull(root.URLByAppendingPathComponent("source-$index"))
                assertTrue(fileManager.createDirectoryAtURL(directory, true, null, null))
                val source = assertNotNull(directory.URLByAppendingPathComponent("image.jpeg"))
                assertTrue(fileManager.createFileAtPath(assertNotNull(source.path), bytes.toNSData(), null))
                source
            }

            val copies = sources.mapIndexed { index, source ->
                copyToTempFile(fileManager, source, id, index)
            }

            assertNotEquals(copies[0].path, copies[1].path)
            copies.forEachIndexed { index, copy ->
                assertEquals("image.jpeg", copy.lastPathComponent)
                val data = assertNotNull(fileManager.contentsAtPath(assertNotNull(copy.path)))
                assertContentEquals(contents[index], data.toByteArray())
            }
        } finally {
            fileManager.removeItemAtURL(root, null)
        }
    }
}
