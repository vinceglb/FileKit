@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit.mimeType

import io.github.vinceglb.filekit.exceptions.InvalidMimeTypeException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class MimeTypeTest {
    @Test
    fun MimeType_parse_withParameter_readsValue() {
        val mimeType = MimeType.parse("text/plain; charset=UTF-8")

        assertEquals(expected = "text", actual = mimeType.primaryType)
        assertEquals(expected = "plain", actual = mimeType.subtype)
        assertEquals(
            expected = setOf(MimeTypeParameter(name = "charset", value = "utf-8")),
            actual = mimeType.parameters,
        )
        assertEquals(expected = mimeType, actual = MimeType.parse("text/plain;charset=utf-8"))
    }

    @Test
    fun MimeType_parse_withSpacesAroundEquals_trimsNameAndValue() {
        val mimeType = MimeType.parse("text/plain; charset = utf-8")

        assertEquals(
            expected = setOf(MimeTypeParameter(name = "charset", value = "utf-8")),
            actual = mimeType.parameters,
        )
    }

    @Test
    fun MimeType_parse_withMultipleParameters_readsEachValue() {
        val mimeType = MimeType.parse("text/plain; Charset=UTF-8; format=flowed")

        assertEquals(
            expected = setOf(
                MimeTypeParameter(name = "charset", value = "utf-8"),
                MimeTypeParameter(name = "format", value = "flowed"),
            ),
            actual = mimeType.parameters,
        )
    }

    @Test
    fun MimeType_parse_withEqualsInsideQuotedValue_splitsOnFirstEqualsOnly() {
        val mimeType = MimeType.parse("application/x-test; name=\"a=b\"")

        // Quotes are kept as-is: quoted-string values are not unquoted yet.
        assertEquals(
            expected = setOf(MimeTypeParameter(name = "name", value = "\"a=b\"")),
            actual = mimeType.parameters,
        )
    }

    @Test
    fun MimeType_parse_withParameterWithoutValue_throws() {
        assertFailsWith<InvalidMimeTypeException> {
            MimeType.parse("text/plain; charset")
        }
    }

    @Test
    fun MimeType_toString_withParameter_includesValue() {
        val mimeType = MimeType.parse("text/plain; charset=UTF-8")

        assertEquals(expected = "text/plain; charset=utf-8", actual = mimeType.toString())
        assertEquals(expected = mimeType, actual = MimeType.parse(mimeType.toString()))
    }

    @Test
    fun MimeType_equals_differentParameterValues_returnsFalse() {
        assertNotEquals(
            illegal = MimeType.parse("text/plain; charset=utf-8"),
            actual = MimeType.parse("text/plain; charset=iso-8859-1"),
        )
    }

    @Test
    fun MimeType_equals_differentCase_returnsTrue() {
        assertEquals(
            expected = MimeType.parse("text/plain; charset=utf-8"),
            actual = MimeType.parse("TEXT/Plain; CHARSET=UTF-8"),
        )
    }
}
