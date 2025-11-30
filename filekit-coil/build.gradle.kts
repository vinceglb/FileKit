plugins {
    alias(libs.plugins.filekit.composeMultiplatformLibrary)
    alias(libs.plugins.vanniktech.mavenPublish)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.coil.compose)
            api(projects.filekitCore)
        }

        webMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
