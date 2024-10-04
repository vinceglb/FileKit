package io.github.vinceglb.filekit.core.platform.xdg

import java.net.URI
import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertEquals

class URITest {
    @Test
    fun testSimpleURI() {
        val path = "/home/user/file.txt"
        val uri = URI(path)
        assertEquals(path, uri.path)
    }

    @Test
    fun testURIWithSpaces() {
        val path = "/home/user/file with spaces.txt"
        val filePath = Paths.get(path)
        val uri = filePath.toUri()
        assertEquals(path, uri.path)
    }

    @Test
    fun testURIWithSpecialCharacters() {
        val path = "/home/user/Ubuntu [24.04].file"
        val filePath = Paths.get(path)
        val uri = filePath.toUri()
        assertEquals(path, uri.path)
    }
}
