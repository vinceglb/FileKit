import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    // https://kotlinlang.org/docs/multiplatform-hierarchy.html#creating-additional-source-sets
    applyDefaultHierarchyTemplate()

    // Android
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    // JVM / Desktop
    jvm()

    // JS / Web
    @OptIn(ExperimentalWasmDsl::class)
    listOf(
        js(IR),
        wasmJs(),
    ).forEach {
        it.outputModuleName = "SamplePickerKt"
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
            export(libs.androidx.lifecycle.viewmodel)
            export(projects.filekitCore)
        }
    }

    sourceSets {
        commonMain.dependencies {
            // FileKit Core
            api(projects.filekitDialogs)

            // ViewModel
            api(libs.androidx.lifecycle.viewmodel)
        }

        val nonWebMain by creating { dependsOn(commonMain.get()) }
        androidMain.get().dependsOn(nonWebMain)
        jvmMain.get().dependsOn(nonWebMain)
        nativeMain.get().dependsOn(nonWebMain)
    }
}

android {
    namespace = "io.github.vinceglb.sample.core.shared"
    compileSdk = libs.versions.android.compileSdk
        .get()
        .toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk
            .get()
            .toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
