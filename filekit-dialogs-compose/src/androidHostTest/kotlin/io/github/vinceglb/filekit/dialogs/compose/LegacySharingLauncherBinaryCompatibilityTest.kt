@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit.dialogs.compose

import java.net.URLClassLoader
import kotlin.test.Test
import kotlin.test.assertNotNull

class LegacySharingLauncherBinaryCompatibilityTest {
    @Test
    fun LegacySharingLauncher_precompiledConsumer_linksAgainstCurrentArtifacts() {
        val fixture = assertNotNull(javaClass.getResource("/legacy-sharing-consumer.jar"))

        URLClassLoader(arrayOf(fixture), javaClass.classLoader).use { loader ->
            val consumer = Class.forName(
                "io.github.vinceglb.filekit.dialogs.compose.compatibility.LegacySharingLauncherConsumer",
                true,
                loader,
            )

            consumer.getMethod("linkLegacyOverload").invoke(null)
        }
    }
}
