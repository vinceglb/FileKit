package io.github.vinceglb.filekit.convention

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun Project.configureComposeMultiplatform(
    extension: KotlinMultiplatformExtension,
) = extension.apply {
    sourceSets.apply {
        commonMain.dependencies {
            implementation(libs.findLibrary("compose.runtime").get())
        }
    }
}
