import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
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
        js(IR),
        wasmJs(),
    ).forEach {
        it.moduleName = "FileKit"
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
            baseName = "FileKit"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.io)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        val nonWebMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(libs.androidx.annotation)
            }
        }
        val nonWebTest by creating { dependsOn(commonTest.get()) }

        androidMain {
            dependsOn(nonWebMain)
            dependencies {
                implementation(libs.androidx.documentfile)
                implementation(libs.androidx.startup)
            }
        }
        androidUnitTest.get().dependsOn(nonWebTest)
        jvmMain.get().dependsOn(nonWebMain)
        jvmTest.get().dependsOn(nonWebTest)
        nativeMain.get().dependsOn(nonWebMain)
        nativeTest.get().dependsOn(nonWebTest)

        wasmJsMain.dependencies {
            implementation(libs.kotlinx.browser)
        }
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

android {
    namespace = "io.github.vinceglb.filekit"
    compileSdk = 35

    defaultConfig {
        minSdk = 21
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
