plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    mingwX64 {
        binaries {
            executable {
                entryPoint = "io.github.vinceglb.filekit.sample.main"
                linkerOpts("-lcomdlg32", "-lole32", "-lshell32", "-ladvapi32")
            }
        }
    }

    sourceSets {
        getByName("mingwX64Main") {
            dependencies {
                implementation(projects.filekitCore)
                implementation(projects.filekitDialogs)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
    }
}
