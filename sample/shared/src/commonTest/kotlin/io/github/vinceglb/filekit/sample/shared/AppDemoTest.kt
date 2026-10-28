@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit.sample.shared

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AppDemoTest {
    @Test
    fun AppDemo_fromId_returnsMatchingDemo() {
        AppDemo.entries.forEach { demo ->
            assertEquals(demo, AppDemo.fromId(demo.id))
        }
    }

    @Test
    fun AppDemo_fromId_unknownOrMissingReturnsNull() {
        assertNull(AppDemo.fromId("unknown"))
        assertNull(AppDemo.fromId(""))
        assertNull(AppDemo.fromId(null))
    }

    @Test
    fun AppDemo_ids_matchDocumentedUrls() {
        // Documentation embeds rely on these ids (docs/snippets/demo-embed.jsx, docs/live-demo.mdx)
        assertEquals(
            listOf("file-picker", "gallery-picker", "directory-picker", "camera-picker", "file-saver", "share-file"),
            AppDemo.entries.map { it.id },
        )
    }
}
