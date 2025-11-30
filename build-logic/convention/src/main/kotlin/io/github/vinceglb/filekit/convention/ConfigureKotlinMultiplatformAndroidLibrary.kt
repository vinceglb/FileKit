package io.github.vinceglb.filekit.convention

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import org.gradle.api.Project

@Suppress("ktlint:standard:chain-method-continuation")
internal fun Project.configureKotlinMultiplatformAndroidLibrary(
    extension: KotlinMultiplatformAndroidLibraryExtension,
    modulePackage: String,
) = extension.apply {
    namespace = modulePackage
    minSdk = libs.findVersion("android-minSdk").get().requiredVersion.toInt()
    compileSdk = libs.findVersion("android-compileSdk").get().requiredVersion.toInt()

    // Exclude unwanted META-INF files to avoid packaging conflicts
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    // Add Android Unit tests
    withHostTest {}
}
