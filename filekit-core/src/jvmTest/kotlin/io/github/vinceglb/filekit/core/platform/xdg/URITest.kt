package io.github.vinceglb.filekit.core.platform.xdg

import kotlin.test.Test
import kotlin.test.assertEquals

class URITest {
    @Test
    fun testSimpleURI() {
        val path = "image:///home/user/file.txt"
        val uri = path.toURI()
        assertEquals("/home/user/file.txt", uri.path)
    }

    @Test
    fun testURIWithSpaces() {
        val path = "file:///home/user/file with spaces.txt"
        val uri = path.toURI()
        assertEquals("/home/user/file with spaces.txt", uri.path)
    }

    @Test
    fun testURIWithSpecialCharacters() {
        val path = "file:///home/user/Ubuntu [24.04].file"
        val uri = path.toURI()
        assertEquals("/home/user/Ubuntu [24.04].file", uri.path)
    }

    @Test
    fun testURIWithoutScheme() {
        val path = "/home/user/file.txt"
        val uri = path.toURI()
        assertEquals("/home/user/file.txt", uri.path)
    }
}
