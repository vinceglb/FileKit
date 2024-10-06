package io.github.vinceglb.filekit.core.platform.xdg

import java.net.URI
import java.net.URLEncoder
import kotlin.test.Test
import kotlin.test.assertEquals

class URITest {
    private fun String.URI(): URI = URLEncoder
        .encode(this, "UTF-8")
        .replace("+", "%20")
        .let { URI(it) }

    @Test
    fun testSimpleURI() {
        val path = "/home/user/file.txt"
        val uri = path.URI()
        assertEquals(path, uri.path)
    }

    @Test
    fun testURIWithSpaces() {
        val path = "/home/user/file with spaces.txt"
        val uri = path.URI()
        assertEquals(path, uri.path)
    }

    @Test
    fun testURIWithSpecialCharacters() {
        val path = "/home/user/Ubuntu [24.04].file"
        val uri = path.URI()
        assertEquals(path, uri.path)
    }
}
