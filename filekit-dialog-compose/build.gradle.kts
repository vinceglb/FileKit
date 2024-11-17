import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.mavenPublishVanniktech)
}

kotlin {
    explicitApi()

    // https://kotlinlang.org/docs/multiplatform-hierarchy.html#creating-additional-source-sets
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
        it.moduleName = "FileKitDialogCompose"
        it.browser()
    }

    // iOS / macOS
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach {
        it.binaries.framework {
            baseName = "FileKitDialogCompose"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Compose
            implementation(compose.runtime)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // FileKit Dialog
            api(projects.filekitCore)
            api(projects.filekitDialog)
        }

        val nonWebMain by creating {
            dependsOn(commonMain.get())
        }

        val mobileMain by creating {
            dependsOn(nonWebMain)
        }

        val nonAndroidMain by creating {
            dependsOn(commonMain.get())
        }

        androidMain {
            dependsOn(nonWebMain)
            dependsOn(mobileMain)
            dependencies {
                implementation(libs.androidx.activity.compose)
            }
        }

        jvmMain {
            dependsOn(nonWebMain)
            dependsOn(nonAndroidMain)
            dependencies {
                implementation(compose.ui)
            }
        }

        nativeMain {
            dependsOn(nonWebMain)
            dependsOn(nonAndroidMain)
        }
        iosMain.get().dependsOn(mobileMain)

        wasmJsMain.get().dependsOn(nonAndroidMain)
        jsMain.get().dependsOn(nonAndroidMain)
    }
}

android {
    namespace = "io.github.vinceglb.filekit.dialog.compose"
    compileSdk = 35

    defaultConfig {
        minSdk = 21
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}