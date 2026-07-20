plugins {
    alias(libs.plugins.filekit.composeMultiplatformLibrary)
    alias(libs.plugins.vanniktech.mavenPublish)
}

kotlin {
    sourceSets {
        val jvmAndNativeMain = create("jvmAndNativeMain") { dependsOn(nonWebMain.get()) }
        val nonAndroidMain = create("nonAndroidMain") { dependsOn(commonMain.get()) }
        jvmMain {
            dependsOn(nonAndroidMain)
            dependsOn(jvmAndNativeMain)
        }
        nativeMain {
            dependsOn(nonAndroidMain)
            dependsOn(jvmAndNativeMain)
        }
        webMain.get().dependsOn(nonAndroidMain)

        commonMain.dependencies {
            implementation(libs.compose.ui)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.androidx.annotation)
            api(projects.filekitDialogs)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.exifinterface)
        }

        androidHostTest.dependencies {
            implementation(libs.test.android.robolectric)
        }

        jvmMain.dependencies {
            implementation(libs.compose.ui)
        }
    }
}
