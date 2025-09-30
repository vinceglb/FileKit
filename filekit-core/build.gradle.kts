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
        it.outputModuleName = "FileKit"
        it.browser()
    }

    // iOS / macOS
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
        macosX64(),
        macosArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "FileKit"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // Kotlinx IO
            api(libs.kotlinx.io)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }

        val nonWebMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(libs.androidx.annotation)
            }
        }
        val nonWebTest by creating { dependsOn(commonTest.get()) }

        val jvmAndNativeMain by creating { dependsOn(nonWebMain) }

        androidMain {
            dependsOn(nonWebMain)
            dependencies {
                implementation(libs.androidx.documentfile)
                implementation(libs.androidx.startup)
                implementation(libs.androidx.exifinterface)
            }
        }
        androidUnitTest.get().dependsOn(nonWebTest)
        jvmMain.get().dependsOn(jvmAndNativeMain)
        jvmTest.get().dependsOn(nonWebTest)
        nativeMain.get().dependsOn(jvmAndNativeMain)
        nativeTest.get().dependsOn(nonWebTest)

        wasmJsMain.dependencies {
            implementation(libs.kotlinx.browser)
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

android {
    namespace = "io.github.vinceglb.filekit"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

tasks.register<Copy>("copyTestResourcesToIos") {
    from("src/nonWebTest/resources")
    into("build/bin/iosSimulatorArm64/debugTest/src/nonWebTest/resources")
}

tasks.named("iosSimulatorArm64Test") {
    dependsOn("copyTestResourcesToIos")
}
