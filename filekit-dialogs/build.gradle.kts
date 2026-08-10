import org.gradle.api.tasks.testing.Test
import org.jetbrains.kotlin.konan.target.HostManager

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

// pkg-config resolution for the libdbus cinterop. The dbus-1 development package is required to
// build the library for Linux native targets, while consumers only need the runtime library.
fun resolvePkgConfigArgs(argument: String): Array<String> = runCatching {
    providers
        .exec {
            commandLine("pkg-config", argument, "dbus-1")
        }.standardOutput
        .asText
        .get()
}.getOrDefault("")
    .trim()
    .split(Regex("\\s+"))
    .filter(String::isNotBlank)
    .toTypedArray()

fun resolvePkgConfigVariable(variable: String): String? = runCatching {
    providers
        .exec {
            commandLine("pkg-config", "--variable=$variable", "dbus-1")
        }.standardOutput
        .asText
        .get()
        .trim()
}.getOrNull()?.takeIf { it.isNotEmpty() }

val dbusCompilerOpts = resolvePkgConfigArgs("--cflags")
val dbusLibDir = resolvePkgConfigVariable("libdir")

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

    // The libdbus cinterop requires the D-Bus development headers from the host machine, so the
    // Linux targets are only created on Linux hosts (see the module convention plugin).
    val isLinuxHost = HostManager.hostIsLinux

    if (isLinuxHost) {
        listOf(linuxX64(), linuxArm64()).forEach { target ->
            // The konan linker does not search the distro's multiarch library dirs, so the host's
            // libdbus location must be passed explicitly. Native test binaries only link for the
            // host architecture, so the host libdir is always correct there.
            dbusLibDir?.let { libDir -> target.binaries.configureEach { linkerOpts("-L$libDir") } }
            listOf("main", "test").forEach { compilationName ->
                target.compilations.getByName(compilationName) {
                    cinterops {
                        create("dbus") {
                            defFile(project.file("src/linuxMain/cinterop/dbus.def"))
                            compilerOpts(*dbusCompilerOpts)
                        }
                    }
                }
            }
        }

        sourceSets {
            // The libdbus client must not live in `linuxMain`: its metadata compilation cannot see
            // target cinterop bindings, so the file is compiled into both Linux target compilations.
            getByName("linuxX64Main") { kotlin.srcDir("src/linuxDbusMain/kotlin") }
            getByName("linuxArm64Main") { kotlin.srcDir("src/linuxDbusMain/kotlin") }
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
