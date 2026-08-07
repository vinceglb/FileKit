import org.gradle.api.tasks.testing.Test

plugins {
    alias(libs.plugins.filekit.kotlinMultiplatformLibrary)
    alias(libs.plugins.vanniktech.mavenPublish)
}

val jvmTest = tasks.named<Test>("jvmTest")
val headlessAwtFilePickerTest = tasks.register<Test>("headlessAwtFilePickerTest") {
    dependsOn(tasks.named("jvmTestClasses"))
    testClassesDirs = jvmTest.get().testClassesDirs
    classpath = jvmTest.get().classpath
    filter.includeTestsMatching(
        "io.github.vinceglb.filekit.dialogs.platform.awt.AwtFilePickerFailureTest",
    )
    systemProperty("filekit.test.headlessAwtFilePicker", "true")
    systemProperty("java.awt.headless", "true")
}

jvmTest.configure {
    dependsOn(headlessAwtFilePickerTest)
}

kotlin {
    android {
        androidResources {
            enable = true
        }
    }

    mingwX64 {
        compilations.getByName("main") {
            cinterops {
                create("comdialogs") {
                    defFile(project.file("src/mingwX64Main/cinterop/comdialogs.def"))
                }
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.filekitCore)
            implementation(libs.kotlinx.coroutines.core)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.ktx)
        }

        androidHostTest.dependencies {
            implementation(libs.test.android.robolectric)
        }

        jvmMain.dependencies {
            implementation(libs.jna)
            implementation(libs.jna.platform)
            implementation(libs.dbus.java.core)
            implementation(libs.dbus.java.transport.native.unixsocket)
        }

        webMain.dependencies {
            implementation(libs.kotlinx.browser)
        }
    }
}
