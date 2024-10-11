package io.github.vinceglb.filekit.core.platform.xdg

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class URITest {
    @Test
    fun testSimpleURI() {
        val path = "file:///home/user/file.txt"
        val uri = path.toURI()
        val file = File(uri)
        assertEquals("/home/user/file.txt", file.path)
    }

    @Test
    fun testURIWithSpaces() {
        val path = "file:///home/user/file with spaces.txt"
        val uri = path.toURI()
        val file = File(uri)
        assertEquals("/home/user/file with spaces.txt", file.path)
    }

    @Test
    fun testURIWithSpecialCharacters() {
        val path = "file:///home/user/Ubuntu [24.04].file"
        val uri = path.toURI()
        val file = File(uri)
        assertEquals("/home/user/Ubuntu [24.04].file", file.path)
    }

    @Test
    fun testToFixIssue129() {
        val path = "file:///home/dik/%D0%A0%D0%B0%D0%B1%D0%BE%D1%87%D0%B8%D0%B9%20%D1%81%D1%82%D0%BE%D0%BB/Ubuntu%20[24.04].torrent"
        val uri = path.toURI()
        val file = File(uri)
        assertEquals("/home/dik/Рабочий стол/Ubuntu [24.04].torrent", file.path)
    }
}
