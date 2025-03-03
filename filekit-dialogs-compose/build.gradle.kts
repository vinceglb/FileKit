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
        it.moduleName = "FileKitDialogsCompose"
        it.browser()
    }

    // iOS / macOS
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach {
        it.binaries.framework {
            baseName = "FileKitDialogsCompose"
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
            api(projects.filekitDialogs)
        }

        val nonWebMain by creating {
            dependsOn(commonMain.get())
        }
        val webMain by creating {
            dependsOn(commonMain.get())
        }
        val mobileMain by creating {
            dependsOn(nonWebMain)
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
            dependencies {
                implementation(compose.ui)
            }
        }

        nativeMain.get().dependsOn(nonWebMain)
        iosMain.get().dependsOn(mobileMain)

        jsMain.get().dependsOn(webMain)
        wasmJsMain.get().dependsOn(webMain)
    }
}

android {
    namespace = "io.github.vinceglb.filekit.dialogs.compose"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
