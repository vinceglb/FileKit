package io.github.vinceglb.filekit.convention

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun Project.configureKotlinMultiplatform(
    extension: KotlinMultiplatformExtension,
    modulePackage: String,
    moduleName: String,
    addMacosTargets: Boolean,
    addWatchosTargets: Boolean,
    addMingwTargets: Boolean = false,
) = extension.apply {
    // Force visibility of public API
    explicitApi()

    // https://kotlinlang.org/docs/multiplatform-hierarchy.html#creating-additional-source-sets
    applyDefaultHierarchyTemplate()

    // Android Kotlin Multiplatform Library
    extensions.configure<KotlinMultiplatformAndroidLibraryExtension> {
        configureKotlinMultiplatformAndroidLibrary(this, modulePackage = modulePackage)
    }

    // Apple targets
    listOfNotNull(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
        if (addMacosTargets) macosArm64() else null,
        if (addWatchosTargets) watchosArm64() else null,
        if (addWatchosTargets) watchosDeviceArm64() else null,
        if (addWatchosTargets) watchosSimulatorArm64() else null,
    ).forEach { appleTarget ->
        appleTarget.binaries.framework {
            isStatic = true
            baseName = "${moduleName}Kit"
            binaryOption("bundleId", modulePackage)
        }
    }

    // Windows native target
    if (addMingwTargets) {
        mingwX64()
    }

    // If one day we need to disable watchOS tests
    //
    // if (addWatchosTargets) {
    //    this@configureKotlinMultiplatform
    //        .tasks
    //        .matching { task ->
    //            task.name.endsWith("WatchosX64Test") ||
    //                task.name.endsWith("WatchosSimulatorArm64Test") ||
    //                task.name.endsWith("WatchosArm64Test") ||
    //                task.name == "watchosX64Test" ||
    //                task.name == "watchosSimulatorArm64Test" ||
    //                task.name == "watchosArm64Test"
    //        }.configureEach {
    //            enabled = false
    //        }
    // }

    // Desktop JVM
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    // JS
    js {
        browser()
        binaries.executable()
    }

    // Wasm
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    // Opt-in
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xexpect-actual-classes",
        )
    }

    sourceSets.apply {
        create("nonWebMain") { dependsOn(commonMain.get()) }
        androidMain.get().dependsOn(getByName("nonWebMain"))
        jvmMain.get().dependsOn(getByName("nonWebMain"))
        nativeMain.get().dependsOn(getByName("nonWebMain"))

        create("nonWebTest") { dependsOn(commonTest.get()) }
        getByName("androidHostTest").dependsOn(getByName("nonWebTest"))
        jvmTest.get().dependsOn(getByName("nonWebTest"))
        nativeTest.get().dependsOn(getByName("nonWebTest"))

        create("mobileMain") { dependsOn(getByName("nonWebMain")) }
        iosMain.get().dependsOn(getByName("mobileMain"))
        androidMain.get().dependsOn(getByName("mobileMain"))
        if (addWatchosTargets) {
            watchosMain.get().dependsOn(getByName("mobileMain"))
        }

        create("mobileTest") { dependsOn(getByName("nonWebTest")) }
        getByName("androidHostTest").dependsOn(getByName("mobileTest"))
        iosTest.get().dependsOn(getByName("mobileTest"))
        if (addWatchosTargets) {
            watchosTest.get().dependsOn(getByName("mobileTest"))
        }

        commonTest.dependencies {
            implementation(libs.findLibrary("kotlin.test").get())
            implementation(libs.findLibrary("kotlinx.coroutines.test").get())
        }
    }
}
