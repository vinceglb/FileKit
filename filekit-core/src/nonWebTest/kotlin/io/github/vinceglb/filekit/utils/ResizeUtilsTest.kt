package io.github.vinceglb.filekit.utils

import kotlin.test.Test
import kotlin.test.assertEquals

class ResizeUtilsTest {
    @Test
    fun testPerfectScale() {
        val (newWidth, newHeight) = calculateNewDimensions(
            originalWidth = 100,
            originalHeight = 200,
            maxWidth = 50,
            maxHeight = 100,
        )

        assertEquals(50, newWidth)
        assertEquals(100, newHeight)
    }

    @Test
    fun testNoResize() {
        val (newWidth, newHeight) = calculateNewDimensions(
            originalWidth = 110,
            originalHeight = 120,
            maxWidth = 200,
            maxHeight = 200,
        )

        assertEquals(110, newWidth)
        assertEquals(120, newHeight)
    }

    @Test
    fun testResizeWidth() {
        val (newWidth, newHeight) = calculateNewDimensions(
            originalWidth = 200,
            originalHeight = 100,
            maxWidth = 100,
            maxHeight = 100,
        )

        assertEquals(100, newWidth)
        assertEquals(50, newHeight)
    }

    @Test
    fun testResizeHeight() {
        val (newWidth, newHeight) = calculateNewDimensions(
            originalWidth = 100,
            originalHeight = 200,
            maxWidth = 100,
            maxHeight = 100,
        )

        assertEquals(50, newWidth)
        assertEquals(100, newHeight)
    }

    @Test
    fun testConstraintsRespected() {
        val (newWidth, newHeight) = calculateNewDimensions(
            originalWidth = 400,
            originalHeight = 200,
            maxWidth = 200,
            maxHeight = 50,
        )

        assertEquals(100, newWidth)
        assertEquals(50, newHeight)
    }

    @Test
    fun testNoConstraints() {
        val (newWidth, newHeight) = calculateNewDimensions(
            originalWidth = 400,
            originalHeight = 200,
            maxWidth = null,
            maxHeight = null,
        )

        assertEquals(400, newWidth)
        assertEquals(200, newHeight)
    }
}
