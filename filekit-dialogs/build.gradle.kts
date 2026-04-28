plugins {
    alias(libs.plugins.filekit.kotlinMultiplatformLibrary)
    alias(libs.plugins.vanniktech.mavenPublish)
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
                val comdialogs by creating {
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
