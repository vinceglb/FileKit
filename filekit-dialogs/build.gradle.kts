plugins {
    alias(libs.plugins.filekit.kotlinMultiplatformLibrary)
    alias(libs.plugins.vanniktech.mavenPublish)
}

kotlin {
    androidLibrary {
        androidResources {
            enable = true
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

        jvmMain.dependencies {
            implementation(libs.jna)
            implementation(libs.jna.platform)
            implementation(libs.dbus.java.core)
            implementation(libs.dbus.java.transport.native.unixsocket)
        }

        wasmJsMain.dependencies {
            implementation(libs.kotlinx.browser)
        }
    }
}
