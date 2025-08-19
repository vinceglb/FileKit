import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.vanniktech.mavenPublish)
}

kotlin {
    explicitApi()

    // https://kotlinlang.org/docs/multiplatform-hierarchy.html#creating-additional-source-sets
    applyDefaultHierarchyTemplate()

    // Android
    androidTarget {
        publishLibraryVariants("release")
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    // JVM / Desktop
    jvm()

    // JS / Web
    @OptIn(ExperimentalWasmDsl::class)
    listOf(
        js(),
        wasmJs(),
    ).forEach {
        it.outputModuleName = "FileKitCoil"
        it.browser()
    }

    // iOS
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach {
        it.binaries.framework {
            baseName = "FileKitCoil"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Compose
            implementation(compose.runtime)

            // Coil
            api(libs.coil.compose)

            // FileKit
            api(projects.filekitCore)
        }

        val nonWebMain by creating {
            dependsOn(commonMain.get())
        }

        val webMain by creating {
            dependsOn(commonMain.get())

            dependencies {
                // Coroutines
                implementation(libs.kotlinx.coroutines.core)
            }
        }

        androidMain.get().dependsOn(nonWebMain)
        jvmMain.get().dependsOn(nonWebMain)
        nativeMain.get().dependsOn(nonWebMain)

        jsMain.get().dependsOn(webMain)
        wasmJsMain.get().dependsOn(webMain)
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

android {
    namespace = "io.github.vinceglb.filekit.coil"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
