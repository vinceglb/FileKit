package io.github.vinceglb.filekit

import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class FileKitWebTest {
    @Test
    fun testDownload() = runTest {
        val bytes = "Hello, World!".encodeToByteArray()
        FileKit.download(
            bytes = bytes,
            fileName = "hello.txt",
        )
    }
}
