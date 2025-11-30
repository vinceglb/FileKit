plugins {
    alias(libs.plugins.filekit.kotlinMultiplatformLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.vanniktech.mavenPublish)
}

kotlin {
    sourceSets {
        val jvmAndNativeMain by creating { dependsOn(nonWebMain.get()) }
        jvmMain.get().dependsOn(jvmAndNativeMain)
        nativeMain.get().dependsOn(jvmAndNativeMain)

        commonMain.dependencies {
            api(libs.kotlinx.io)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.core)
        }

        nonWebMain.dependencies {
            implementation(libs.androidx.annotation)
        }

        androidMain {
            dependencies {
                implementation(libs.androidx.documentfile)
                implementation(libs.androidx.startup)
                implementation(libs.androidx.exifinterface)
            }
        }

        wasmJsMain.dependencies {
            implementation(libs.kotlinx.browser)
        }

        androidHostTest.dependencies {
            implementation(libs.test.android.robolectric)
        }

        nonWebTest.dependencies {
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
