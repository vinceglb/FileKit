@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")
@file:OptIn(io.github.vinceglb.filekit.dialogs.FileKitDialogsInternalApi::class)

package io.github.vinceglb.filekit.dialogs

import android.content.ActivityNotFoundException
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame

class AndroidPickerLaunchFallbackTest {
    @Test
    fun PickerLaunch_primaryThrowsActivityNotFound_usesFallbackResult() = runBlocking {
        var fallbackCalls = 0

        val result = runPickerLaunchWithActivityNotFoundFallback(
            primary = {
                throw ActivityNotFoundException("No activity for picker")
            },
            fallback = {
                fallbackCalls++
                "fallback-result"
            },
        )

        assertEquals("fallback-result", result)
        assertEquals(1, fallbackCalls)
    }

    @Test
    fun PickerLaunch_primaryAndFallbackThrowActivityNotFound_throwsPickerFailureWithFallbackCause() {
        val fallbackFailure = ActivityNotFoundException("No activity for document picker")

        val failure = assertFailsWith<FileKitPickerException> {
            runBlocking {
                runPickerLaunchWithActivityNotFoundFallback(
                    primary = {
                        throw ActivityNotFoundException("No activity for visual picker")
                    },
                    fallback = {
                        throw fallbackFailure
                    },
                )
            }
        }

        assertSame(fallbackFailure, failure.cause)
        assertIs<FileKitDialogException>(failure)
    }

    @Test
    fun PickerLaunch_primaryThrowsActivityNotFoundWithoutFallback_throwsPickerFailureWithCause() {
        val launchFailure = ActivityNotFoundException("No activity for document picker")

        val failure = assertFailsWith<FileKitPickerException> {
            runBlocking {
                runPickerLaunchWithActivityNotFoundFallback(
                    primary = {
                        throw launchFailure
                    },
                )
            }
        }

        assertSame(launchFailure, failure.cause)
    }

    @Test
    fun PickerLaunch_primaryThrowsSecurityException_doesNotInvokeFallbackAndThrowsPickerFailureWithCause() {
        val launchFailure = SecurityException("Visual picker launch rejected")
        var fallbackCalls = 0

        val failure = assertFailsWith<FileKitPickerException> {
            runBlocking {
                runPickerLaunchWithActivityNotFoundFallback(
                    primary = {
                        throw launchFailure
                    },
                    fallback = {
                        fallbackCalls++
                        "fallback-result"
                    },
                )
            }
        }

        assertSame(launchFailure, failure.cause)
        assertEquals(0, fallbackCalls)
    }

    @Test
    fun PickerLaunch_fallbackThrowsSecurityException_throwsPickerFailureWithCause() {
        val launchFailure = SecurityException("Document picker launch rejected")

        val failure = assertFailsWith<FileKitPickerException> {
            runBlocking {
                runPickerLaunchWithActivityNotFoundFallback(
                    primary = {
                        throw ActivityNotFoundException("No activity for visual picker")
                    },
                    fallback = {
                        throw launchFailure
                    },
                )
            }
        }

        assertSame(launchFailure, failure.cause)
    }

    @Test
    fun PickerLaunch_primaryReturnsNull_doesNotInvokeFallback() = runBlocking {
        var fallbackCalls = 0

        val result = runPickerLaunchWithActivityNotFoundFallback<String?>(
            primary = { null },
            fallback = {
                fallbackCalls++
                "fallback-result"
            },
        )

        assertNull(result)
        assertEquals(0, fallbackCalls)
    }

    @Test
    fun PickerLaunch_primaryThrowsNonActivityError_rethrows() {
        assertFailsWith<IllegalStateException> {
            runBlocking {
                runPickerLaunchWithActivityNotFoundFallback(
                    primary = {
                        throw IllegalStateException("Unexpected failure")
                    },
                    fallback = {
                        "fallback-result"
                    },
                )
            }
        }
    }

    @Test
    fun VisualFallbackMimeTypes_matchExpectedMappings() {
        assertContentEquals(arrayOf("image/*"), FileKitType.Image.toVisualFallbackMimeTypes())
        assertContentEquals(arrayOf("video/*"), FileKitType.Video.toVisualFallbackMimeTypes())
        assertContentEquals(arrayOf("image/*", "video/*"), FileKitType.ImageAndVideo.toVisualFallbackMimeTypes())
    }

    @Test
    fun VisualFallbackMimeTypes_fileType_throws() {
        assertFailsWith<IllegalStateException> {
            FileKitType.File().toVisualFallbackMimeTypes()
        }
    }
}
