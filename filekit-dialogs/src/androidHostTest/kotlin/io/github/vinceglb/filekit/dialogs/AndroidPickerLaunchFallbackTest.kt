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

        val result = runPickerLaunchWithFallback(
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
    fun PickerLaunch_primaryAndFallbackThrowActivityNotFound_throwsPickerFailure() {
        val fallbackFailure = ActivityNotFoundException("No activity for document picker")

        val failure = assertFailsWith<FileKitPickerException> {
            runBlocking {
                runPickerLaunchWithFallback(
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
    }

    @Test
    fun PickerLaunch_primaryThrowsActivityNotFoundWithoutFallback_throwsPickerFailure() {
        val launchFailure = ActivityNotFoundException("No activity for document picker")

        val failure = assertFailsWith<FileKitPickerException> {
            runBlocking {
                runPickerLaunchWithFallback(
                    primary = {
                        throw launchFailure
                    },
                )
            }
        }

        assertSame(launchFailure, failure.cause)
    }

    @Test
    fun PickerLaunch_primaryThrowsSecurityException_usesFallbackResult() = runBlocking {
        var fallbackCalls = 0

        val result = runPickerLaunchWithFallback(
            primary = {
                throw SecurityException("Visual picker launch denied")
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
    fun PickerLaunch_fallbackThrowsSecurityException_throwsPickerFailure() {
        val fallbackFailure = SecurityException("Document picker launch denied")

        val failure = assertFailsWith<FileKitPickerException> {
            runBlocking {
                runPickerLaunchWithFallback(
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
    }

    @Test
    fun PickerLaunch_primaryThrowsSecurityExceptionWithoutFallback_throwsPickerFailure() {
        val launchFailure = SecurityException("Document picker launch denied")

        val failure = assertFailsWith<FileKitPickerException> {
            runBlocking {
                runPickerLaunchWithFallback(
                    primary = {
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

        val result = runPickerLaunchWithFallback<String?>(
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
                runPickerLaunchWithFallback(
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
    fun AndroidDialogLaunch_activityNotFound_wrapsCause() {
        val launchFailure = ActivityNotFoundException("No directory picker")

        val failure = assertFailsWith<FileKitDialogException> {
            runBlocking {
                runAndroidDialogLaunch("Failed to launch the directory picker.") {
                    throw launchFailure
                }
            }
        }

        assertSame(launchFailure, failure.cause)
    }

    @Test
    fun AndroidDialogLaunch_securityException_wrapsCause() {
        val launchFailure = SecurityException("Camera launch denied")

        val failure = assertFailsWith<FileKitDialogException> {
            runBlocking {
                runAndroidDialogLaunch("Failed to launch the camera picker.") {
                    throw launchFailure
                }
            }
        }

        assertIs<SecurityException>(failure.cause)
        assertSame(launchFailure, failure.cause)
    }

    @Test
    fun FileSaverLaunch_activityNotFound_wrapsCause() {
        val launchFailure = ActivityNotFoundException("No file saver")

        val failure = assertFailsWith<FileKitDialogException> {
            runBlocking {
                runAndroidDialogLaunch("Failed to launch the file saver.") {
                    throw launchFailure
                }
            }
        }

        assertSame(launchFailure, failure.cause)
    }

    @Test
    fun ShareLaunch_activityNotFound_wrapsCause() {
        val launchFailure = ActivityNotFoundException("No share activity")

        val failure = assertFailsWith<FileKitDialogException> {
            runBlocking {
                runAndroidDialogLaunch("Failed to launch the share sheet.") {
                    throw launchFailure
                }
            }
        }

        assertSame(launchFailure, failure.cause)
    }

    @Test
    fun AndroidDialogLaunch_unexpectedException_propagates() {
        val unexpectedFailure = IllegalStateException("Broken registry")

        val failure = assertFailsWith<IllegalStateException> {
            runBlocking {
                runAndroidDialogLaunch("Failed to launch the directory picker.") {
                    throw unexpectedFailure
                }
            }
        }

        assertSame(unexpectedFailure, failure)
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
