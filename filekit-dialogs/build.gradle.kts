import org.gradle.api.tasks.testing.Test
import org.jetbrains.kotlin.gradle.plugin.mpp.TestExecutable
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
            isIgnoreExitValue = true
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
            isIgnoreExitValue = true
        }.standardOutput
        .asText
        .get()
        .trim()
}.getOrNull()?.takeIf { it.isNotEmpty() }

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

    // Keep targets and cinterops identical on every host so publication metadata includes Linux.
    // Cross-compilation uses Linux headers staged by CI; native Linux builds use pkg-config.
    val dbusHeaders = providers.gradleProperty("filekit.dbusHeaders").orNull
    val dbusCompilerOpts = when {
        dbusHeaders != null -> arrayOf("-I${rootProject.file(dbusHeaders).absolutePath}")
        HostManager.hostIsLinux -> resolvePkgConfigArgs("--cflags")
        else -> emptyArray()
    }
    val dbusLibDir = if (HostManager.hostIsLinux) resolvePkgConfigVariable("libdir") else null

    listOf(linuxX64(), linuxArm64()).forEach { target ->
        // Linux test executables need Linux runtime libraries and cannot run on other hosts.
        target.binaries.withType<TestExecutable>().configureEach {
            linkTaskProvider.configure { enabled = HostManager.hostIsLinux }
        }
        dbusLibDir?.let { libDir -> target.binaries.configureEach { linkerOpts("-L$libDir") } }
        // Tests reuse main's bindings; generating either interop twice duplicates native symbols.
        target.compilations.getByName("main") {
            cinterops {
                create("process") {
                    defFile(project.file("src/linuxMain/cinterop/process.def"))
                }
                create("dbus") {
                    defFile(project.file("src/linuxMain/cinterop/dbus.def"))
                    compilerOpts(*dbusCompilerOpts)
                }
            }
        }
    }

    sourceSets {
        // Shared metadata cannot see target cinterop bindings.
        getByName("linuxX64Main") { kotlin.srcDir("src/linuxDbusMain/kotlin") }
        getByName("linuxArm64Main") { kotlin.srcDir("src/linuxDbusMain/kotlin") }
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
