import io.github.vinceglb.filekit.convention.configureKotlinMultiplatform
import io.github.vinceglb.filekit.convention.libs
import io.github.vinceglb.filekit.convention.moduleName
import io.github.vinceglb.filekit.convention.modulePackage
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

@Suppress("ktlint:standard:chain-method-continuation", "unused")
class KotlinMultiplatformLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.findPlugin("androidKotlinMultiplatformLibrary").get().get().pluginId)
                apply(libs.findPlugin("kotlinMultiplatform").get().get().pluginId)
            }

            println("Module [$moduleName] - $modulePackage")

            // Kotlin Multiplatform
            extensions.configure<KotlinMultiplatformExtension> {
                configureKotlinMultiplatform(
                    extension = this,
                    modulePackage = modulePackage,
                    moduleName = moduleName,
                    addMacosTargets = true
                )
            }
        }
    }
}
