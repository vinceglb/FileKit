import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
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
        js(IR),
        wasmJs(),
    ).forEach {
        it.outputModuleName = "FileKitDialogs"
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
            baseName = "FileKitDialogs"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // FileKit Core
            api(projects.filekitCore)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
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
                implementation(libs.androidx.activity.ktx)
            }
        }

        jvmMain {
            dependsOn(nonWebMain)
            dependencies {
                implementation(libs.jna)
                implementation(libs.jna.platform)
                implementation(libs.dbus.java.core)
                implementation(libs.dbus.java.transport.native.unixsocket)
            }
        }

        nativeMain.get().dependsOn(nonWebMain)
        iosMain.get().dependsOn(mobileMain)

        jsMain.get().dependsOn(webMain)
        wasmJsMain {
            dependsOn(webMain)
            dependencies {
                implementation(libs.kotlinx.browser)
            }
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

android {
    namespace = "io.github.vinceglb.filekit.dialogs"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
