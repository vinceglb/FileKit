@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit.dialogs.compose

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyPickerLauncherBinaryCompatibilityTest {
    @Test
    fun LegacyPickerLauncher_precompiledConsumer_linksAgainstCurrentArtifacts() {
        val consumer = Class.forName(
            "io.github.vinceglb.filekit.dialogs.compose.compatibility.LegacyPickerLauncherConsumer",
        )

        assertEquals(12, consumer.getMethod("legacyOverloadCount").invoke(null))
        consumer.getMethod("linkLegacyOverloads").invoke(null)
    }
}
