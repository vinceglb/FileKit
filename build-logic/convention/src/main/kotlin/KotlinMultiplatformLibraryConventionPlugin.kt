import io.github.vinceglb.filekit.convention.configureKotlinMultiplatform
import io.github.vinceglb.filekit.convention.libs
import io.github.vinceglb.filekit.convention.moduleName
import io.github.vinceglb.filekit.convention.modulePackage
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.konan.target.HostManager

@Suppress("ktlint:standard:chain-method-continuation", "unused")
class KotlinMultiplatformLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.findPlugin("androidKotlinMultiplatformLibrary").get().get().pluginId)
                apply(libs.findPlugin("kotlinMultiplatform").get().get().pluginId)
            }

            println("Module [$moduleName] - $modulePackage")

            // The libdbus cinterop of `filekit-dialogs` needs the D-Bus development headers from the
            // host machine, so its Linux targets are only created when building on Linux.
            val isLinuxHost = HostManager.hostIsLinux

            // Kotlin Multiplatform
            extensions.configure<KotlinMultiplatformExtension> {
                configureKotlinMultiplatform(
                    extension = this,
                    modulePackage = modulePackage,
                    moduleName = moduleName,
                    addMacosTargets = true,
                    addWatchosTargets = path == ":filekit-core",
                    addMingwTargets = path == ":filekit-core" || path == ":filekit-dialogs",
                    addLinuxTargets = path == ":filekit-core" || (path == ":filekit-dialogs" && isLinuxHost),
                )
            }
        }
    }
}
