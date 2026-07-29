package io.github.vinceglb.filekit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PlatformFileLinuxTest {
    @Test
    fun absolutePath_absolutePath_isReturnedUnchanged() {
        assertEquals(
            expected = "/tmp/filekit/hello.txt",
            actual = PlatformFile("/tmp/filekit/hello.txt").absolutePath(),
        )
    }

    @Test
    fun absolutePath_relativePathOfMissingFile_resolvesWithoutThrowing() {
        val absolutePath = PlatformFile("does-not-exist/hello.txt").absolutePath()

        assertTrue(absolutePath.startsWith("/"), "Expected an absolute path but got $absolutePath")
        assertTrue(absolutePath.endsWith("does-not-exist/hello.txt"), "Unexpected path $absolutePath")
    }

    @Test
    fun absolutePath_relativePathOfExistingFile_resolvesToSameParentAsMissingSibling() {
        val existing = FileKit.projectDir / "src/nonWebTest/resources/hello.txt"
        val missing = FileKit.projectDir / "src/nonWebTest/resources/not-existing-file.pdf"

        val existingParent = existing.absolutePath().substringBeforeLast('/')
        val missingParent = missing.absolutePath().substringBeforeLast('/')

        assertEquals(expected = existingParent, actual = missingParent)
    }

    @Test
    fun absolutePath_emptyPath_resolvesToWorkingDirectory() {
        val absolutePath = PlatformFile("").absolutePath()

        assertTrue(absolutePath.startsWith("/"), "Expected an absolute path but got $absolutePath")
        assertTrue(
            absolutePath == "/" || !absolutePath.endsWith("/"),
            "Working directory should not have a trailing slash: $absolutePath",
        )
    }
}
