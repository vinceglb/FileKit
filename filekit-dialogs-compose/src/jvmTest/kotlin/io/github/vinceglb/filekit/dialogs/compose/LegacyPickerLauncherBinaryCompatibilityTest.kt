@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit.dialogs.compose

import kotlin.test.Test

class LegacyPickerLauncherBinaryCompatibilityTest {
    @Test
    fun LegacyPickerLauncher_precompiledConsumer_linksAgainstCurrentArtifacts() {
        val consumer = Class.forName(
            "io.github.vinceglb.filekit.dialogs.compose.compatibility.LegacyPickerLauncherConsumer",
        )

        consumer.getMethod("linkLegacyOverloads").invoke(null)
    }
}
