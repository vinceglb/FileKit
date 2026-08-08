@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit.dialogs.compose

import java.net.URLClassLoader
import kotlin.test.Test
import kotlin.test.assertNotNull

class LegacyCameraLauncherBinaryCompatibilityTest {
    @Test
    fun LegacyCameraLauncher_precompiledConsumer_linksAgainstCurrentArtifacts() {
        val fixture = assertNotNull(javaClass.getResource("/legacy-camera-consumer.jar"))

        URLClassLoader(arrayOf(fixture), javaClass.classLoader).use { loader ->
            val consumer = Class.forName(
                "io.github.vinceglb.filekit.dialogs.compose.compatibility.LegacyCameraLauncherConsumer",
                true,
                loader,
            )

            consumer.getMethod("linkLegacyOverload").invoke(null)
        }
    }
}
