@file:Suppress("ktlint:standard:chain-method-continuation")

plugins {
    `kotlin-dsl`
}

group = "io.github.vinceglb.filekit.convention"

dependencies {
    compileOnly(libs.android.gradleApiPlugin)
    compileOnly(libs.kotlin.multiplatform.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.compose.multiplatform.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("kotlinMultiplatformLibrary") {
            id = libs.plugins.filekit.kotlinMultiplatformLibrary.get().pluginId
            implementationClass = "KotlinMultiplatformLibraryConventionPlugin"
        }

        register("composeMultiplatformLibrary") {
            id = libs.plugins.filekit.composeMultiplatformLibrary.get().pluginId
            implementationClass = "ComposeMultiplatformLibraryConventionPlugin"
        }
//
//        register("feature") {
//            id = libs.plugins.filekitFeature.get().pluginId
//            implementationClass = "FeatureConventionPlugin"
//        }
    }
}
