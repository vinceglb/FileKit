@file:OptIn(
    ExperimentalFileKitApi::class,
    kotlin.js.ExperimentalWasmJsInterop::class,
)

package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.ExperimentalFileKitApi
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.WebFile
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.isDirectory
import io.github.vinceglb.filekit.isRegularFile
import io.github.vinceglb.filekit.lastModified
import io.github.vinceglb.filekit.mimeType
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.nameWithoutExtension
import io.github.vinceglb.filekit.parent
import io.github.vinceglb.filekit.path
import io.github.vinceglb.filekit.readString
import io.github.vinceglb.filekit.size
import io.github.vinceglb.filekit.writeString
import kotlinx.coroutines.test.runTest
import kotlin.js.JsAny
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

private fun unavailablePickerOverride(): JsAny = js("({ available: false })")

private fun cancelledPickerOverride(): JsAny = js(
    "({ available: true, picker: function(options) { globalThis.__filekitSavePickerOptions = options; " +
        "return Promise.reject(new DOMException('cancelled', 'AbortError')); } })",
)

private fun failedPickerOverride(): JsAny = js(
    "({ available: true, picker: function(options) { globalThis.__filekitSavePickerOptions = options; " +
        "return Promise.reject(new Error('picker failed')); } })",
)

private fun synchronousFailurePickerOverride(): JsAny = js(
    "({ available: true, picker: function(options) { globalThis.__filekitSavePickerOptions = options; " +
        "throw new Error('synchronous picker failure'); } })",
)

private fun selectedPickerOverride(): JsAny = js(
    "({ available: true, picker: function(options) { globalThis.__filekitSavePickerOptions = options; " +
        "return Promise.resolve({ name: 'selected.txt' }); } })",
)

private fun lastPickerSuggestedName(): String =
    js("globalThis.__filekitSavePickerOptions.suggestedName")

private fun lastPickerTypesJson(): String =
    js("JSON.stringify(globalThis.__filekitSavePickerOptions.types)")

private fun hasPickerTypes(): Boolean =
    js("globalThis.__filekitSavePickerOptions.types !== undefined")

class FileSaverWebTest {
    @Test
    fun openFileSaver_whenPickerUnavailable_returnsDownloadDestination() = runTest {
        withPickerOverride(unavailablePickerOverride()) {
            val file = FileKit.openFileSaver(
                suggestedName = "report",
                defaultExtension = ".pdf",
            )

            assertEquals("report.pdf", file?.name)
            assertTrue((file?.webFile as? WebFile.SaveDestination)?.isDownload == true)
        }
    }

    @Test
    fun openFileSaver_whenPickerUnavailable_returnsDestinationWithBasicMetadata() = runTest {
        withPickerOverride(unavailablePickerOverride()) {
            val file = assertNotNull(
                FileKit.openFileSaver(suggestedName = "report", defaultExtension = "txt"),
            )

            assertEquals("report.txt", file.name)
            assertEquals("report", file.nameWithoutExtension)
            assertEquals("txt", file.extension)
            assertEquals("report.txt", file.path)
            assertEquals(0L, file.size())
            assertNull(file.mimeType())
            assertEquals(Instant.fromEpochMilliseconds(0), file.lastModified())
            assertNull(file.parent())
            assertTrue(file.isRegularFile())
            assertFalse(file.isDirectory())
        }
    }

    @Test
    fun openFileSaver_whenPickerUnavailable_writesAndReadsString() = runTest {
        withPickerOverride(unavailablePickerOverride()) {
            val destination = assertNotNull(
                FileKit.openFileSaver(suggestedName = "report", defaultExtension = "txt"),
            )

            destination.writeString("Saved from web saver")

            assertEquals("Saved from web saver", destination.readString())
            assertEquals(20L, destination.size())
        }
    }

    @Test
    fun openFileSaver_whenCancelled_returnsNull() = runTest {
        withPickerOverride(cancelledPickerOverride()) {
            assertNull(FileKit.openFileSaver(suggestedName = "report"))
        }
    }

    @Test
    fun openFileSaver_whenPickerFails_throwsDialogException() = runTest {
        withPickerOverride(failedPickerOverride()) {
            val error = assertFailsWith<FileKitDialogException> {
                FileKit.openFileSaver(suggestedName = "report")
            }

            assertContains(error.message.orEmpty(), "picker failed")
        }
    }

    @Test
    fun openFileSaver_whenPickerThrowsSynchronously_throwsDialogException() = runTest {
        withPickerOverride(synchronousFailurePickerOverride()) {
            val error = assertFailsWith<FileKitDialogException> {
                FileKit.openFileSaver(suggestedName = "report")
            }

            assertContains(error.message.orEmpty(), "synchronous picker failure")
        }
    }

    @Test
    fun openFileSaver_whenFileIsSelected_returnsHandleDestinationAndExpectedOptions() = runTest {
        withPickerOverride(selectedPickerOverride()) {
            val file = FileKit.openFileSaver(
                suggestedName = "report",
                defaultExtension = "pdf",
                allowedExtensions = setOf("txt", ".pdf"),
            )
            val selectedFile = assertNotNull(file)
            val destination = assertIs<WebFile.SaveDestination>(selectedFile.webFile)

            assertEquals("report.pdf", selectedFile.name)
            assertEquals(false, destination.isDownload)
            assertEquals("report.pdf", lastPickerSuggestedName())
            assertEquals(
                """[{"description":"Files","accept":{"application/pdf":[".pdf"],"text/plain":[".txt"]}}]""",
                lastPickerTypesJson(),
            )
        }
    }

    @Test
    fun openFileSaver_withoutExtensions_omitsPickerTypes() = runTest {
        withPickerOverride(selectedPickerOverride()) {
            FileKit.openFileSaver(suggestedName = "notes")

            assertEquals("notes", lastPickerSuggestedName())
            assertFalse(hasPickerTypes())
        }
    }

    private suspend fun withPickerOverride(
        override: JsAny,
        block: suspend () -> Unit,
    ) {
        val previous = browserSavePickerTestOverride
        browserSavePickerTestOverride = override
        try {
            block()
        } finally {
            browserSavePickerTestOverride = previous
        }
    }
}
