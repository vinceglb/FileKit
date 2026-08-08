@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit.dialogs.compose

import java.net.URLClassLoader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class LegacyAndroidLauncherBinaryCompatibilityTest {
    @Test
    fun LegacyAndroidLaunchers_precompiledConsumer_linksEveryLegacyFamilyAgainstCurrentArtifacts() {
        val fixture = assertNotNull(javaClass.getResource("/legacy-android-launcher-consumer.jar"))

        URLClassLoader(arrayOf(fixture), javaClass.classLoader).use { loader ->
            val consumer = Class.forName(
                "io.github.vinceglb.filekit.dialogs.compose.compatibility.LegacyAndroidLauncherConsumer",
                true,
                loader,
            )

            assertEquals(8, consumer.getMethod("legacyOverloadCount").invoke(null))
            consumer.getMethod("linkLegacyOverloads").invoke(null)
        }
    }
}
