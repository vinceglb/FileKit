@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit.dialogs

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FileSaverNameTest {
    @Test
    fun normalizeFileSaverExtension_whenNull_returnsNull() {
        assertNull(normalizeFileSaverExtension(null))
    }

    @Test
    fun normalizeFileSaverExtension_whenBlank_returnsNull() {
        assertNull(normalizeFileSaverExtension(""))
        assertNull(normalizeFileSaverExtension("   "))
        assertNull(normalizeFileSaverExtension("."))
        assertNull(normalizeFileSaverExtension("..."))
    }

    @Test
    fun normalizeFileSaverExtension_whenPrefixedWithDot_removesLeadingDotsAndWhitespace() {
        assertEquals("pdf", normalizeFileSaverExtension(".pdf"))
        assertEquals("pdf", normalizeFileSaverExtension(" .pdf "))
        assertEquals("pdf", normalizeFileSaverExtension("..pdf"))
    }

    @Test
    fun normalizeFileSaverExtensions_whenValuesContainDotsAndBlankEntries_returnsCleanSet() {
        assertEquals(
            setOf("pdf", "md"),
            normalizeFileSaverExtensions(setOf(" .pdf ", ".", "", "md")),
        )
    }

    @Test
    fun normalizeFileSaverExtensions_whenNoUsableValues_returnsNull() {
        assertNull(normalizeFileSaverExtensions(null))
        assertNull(normalizeFileSaverExtensions(setOf("", ".", "   ")))
    }

    @Test
    fun buildFileSaverSuggestedName_whenExtensionNullOrBlank_returnsSuggestedName() {
        assertEquals("document", buildFileSaverSuggestedName("document", null))
        assertEquals("document", buildFileSaverSuggestedName("document", ""))
        assertEquals("document", buildFileSaverSuggestedName("document", "   "))
        assertEquals("document", buildFileSaverSuggestedName("document", "."))
    }

    @Test
    fun buildFileSaverSuggestedName_whenExtensionProvided_appendsSingleDot() {
        assertEquals("document.pdf", buildFileSaverSuggestedName("document", "pdf"))
        assertEquals("document.pdf", buildFileSaverSuggestedName("document", ".pdf"))
        assertEquals("document.pdf", buildFileSaverSuggestedName("document", " .pdf "))
    }

    @Test
    fun buildFileSaverAllowedFileTypes_whenNoExtensions_returnsNull() {
        assertNull(buildFileSaverAllowedFileTypes(null, null))
        assertNull(buildFileSaverAllowedFileTypes("", setOf("", ".")))
    }

    @Test
    fun buildFileSaverAllowedFileTypes_whenOnlyDefaultExtension_returnsSingleNormalizedType() {
        assertEquals(listOf("pdf"), buildFileSaverAllowedFileTypes(".pdf", null))
    }

    @Test
    fun buildFileSaverAllowedFileTypes_whenNoDefault_returnsNormalizedAllowedExtensions() {
        assertEquals(listOf("md", "txt"), buildFileSaverAllowedFileTypes(null, setOf("md", "txt")))
    }

    @Test
    fun buildFileSaverAllowedFileTypes_putsDefaultExtensionFirstAndDeduplicates() {
        assertEquals(
            listOf("pdf", "md", "txt"),
            buildFileSaverAllowedFileTypes("pdf", setOf("md", "pdf", "txt")),
        )
    }
}
