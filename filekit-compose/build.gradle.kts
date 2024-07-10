import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.mavenPublishVanniktech)
}

kotlin {
    explicitApi()
    applyDefaultHierarchyTemplate()

    // Android
    androidTarget {
        publishLibraryVariants("release")
    }

    // JVM / Desktop
    jvm()

    // JS / Web
    @OptIn(ExperimentalWasmDsl::class)
    listOf(
        js(),
        wasmJs(),
    ).forEach {
        it.moduleName = "FileKitCompose"
        it.browser()
    }

    // iOS / macOS
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach {
        it.binaries.framework {
            baseName = "FileKitCompose"
            isStatic = true
        }
    }

    sourceSets {
        val wasmJsMain by getting
        val jsMain by getting

        commonMain.dependencies {
            // Compose
            implementation(compose.runtime)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // FileKit Core
            api(projects.filekitCore)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }

        val nonAndroidMain by creating {
            dependsOn(commonMain.get())
        }

        nativeMain.get().dependsOn(nonAndroidMain)
        jvmMain.get().dependsOn(nonAndroidMain)
        wasmJsMain.dependsOn(nonAndroidMain)
        jsMain.dependsOn(nonAndroidMain)
    }
}

android {
    namespace = "io.github.vinceglb.filekit.compose"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
