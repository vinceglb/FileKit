import dev.nucleusframework.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.nucleus)
}

nucleus.application {
    mainClass = "io.github.vinceglb.filekit.sample.nucleus.MainKt"

    nativeDistributions {
        targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
        packageName = "FileKit Nucleus Sample"
        packageVersion = "1.0.0"
    }
}

dependencies {
    // App module
    implementation(projects.sample.shared)

    // Compose Desktop
    implementation(compose.desktop.currentOs)

    // Nucleus
    implementation(libs.nucleus.application)
    implementation(libs.nucleus.decorated.window.tao)

    // FileKit
    implementation(projects.filekitCore)
}
