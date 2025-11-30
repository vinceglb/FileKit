
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    // https://kotlinlang.org/docs/multiplatform-hierarchy.html#creating-additional-source-sets
    applyDefaultHierarchyTemplate()

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        // iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    jvm("desktop")

    js {
        browser()
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            // Compose
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            // TODO with compose 1.10 implementation(libs.compose.uiToolingPreview)

            // FileKit
            implementation(projects.filekitCoil)
            implementation(projects.filekitDialogsCompose)

            // Human Readable
            implementation(libs.human.readable)

            // Coil
            implementation(libs.coil.compose)

            // Icons
            implementation(libs.material.icons.core)
        }

        val nonWebMain by creating {
            dependsOn(commonMain.get())
        }

        androidMain {
            dependsOn(nonWebMain)
            dependencies {
                implementation(libs.compose.uiTooling)
                implementation(libs.androidx.activity.compose)
            }
        }

        val desktopMain by getting {
            dependsOn(nonWebMain)
            dependencies {
                // Compose
                implementation(compose.desktop.currentOs)

                // Coroutines
                implementation(libs.kotlinx.coroutines.swing)
            }
        }

        nativeMain.get().dependsOn(nonWebMain)
    }
}

android {
    namespace = "io.github.vinceglb.sample.compose"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "io.github.vinceglb.sample.compose"
        minSdk = 23
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "io.github.vinceglb.sample.compose"
            packageVersion = "1.0.0"
        }

        buildTypes.release.proguard {
            configurationFiles.from(project.file("compose-desktop.pro"))
            obfuscate.set(true)
        }
    }
}
