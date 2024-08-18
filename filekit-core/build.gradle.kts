import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.mavenPublishVanniktech)
}

kotlin {
    explicitApi()

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
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                api(libs.kotlinx.io.core)
            }
        }

        val appleMain by creating {
            dependsOn(commonMain)
        }

        val iosMain by creating {
            dependsOn(appleMain)
        }

        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }

        val iosArm64Main by getting {
            dependsOn(iosMain)
        }

        val iosX64Main by getting {
            dependsOn(iosMain)
        }

        val macosMain by creating {
            dependsOn(appleMain)
        }

        val macosX64Main by getting {
            dependsOn(macosMain)
        }

        val macosArm64Main by getting {
            dependsOn(macosMain)
        }

        val androidAndJvmMain by creating {
            dependsOn(commonMain)
        }

        androidMain {
            dependsOn(androidAndJvmMain)
            dependencies {
                implementation(libs.androidx.activity.ktx)
                implementation(libs.androidx.documentfile)
            }
        }

        jvmMain {
            dependsOn(androidAndJvmMain)
            dependencies {
                implementation(libs.jna)
                implementation(libs.jna.platform)
                implementation(libs.dbus.java.core)
                implementation(libs.dbus.java.transport.native.unixsocket)
            }
        }
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

android {
    namespace = "io.github.vinceglb.filekit"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
