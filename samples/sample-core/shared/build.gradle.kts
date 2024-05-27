import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    // Android
    androidTarget()

    // JVM / Desktop
    jvm()

    // JS / Web
    @OptIn(ExperimentalWasmDsl::class)
    listOf(
        js(IR),
        wasmJs()
    ).forEach {
        it.moduleName = "SamplePickerKt"
        it.browser()
    }

    // iOS / macOS
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
        macosX64(),
        macosArm64(),
    ).forEach {
        it.binaries.framework {
            baseName = "SamplePickerKt"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // FileKit Core
            api(projects.filekitCore)

            // Observable ViewModel
            api(libs.observable.viewmodel)
        }

        // https://github.com/rickclephas/KMP-ObservableViewModel?tab=readme-ov-file#kotlin
        all {
            languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
        }
    }
}

android {
    namespace = "io.github.vinceglb.sample.core.shared"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
    }
}
