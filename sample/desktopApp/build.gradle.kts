import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
}

compose.desktop {
    application {
        mainClass = "io.github.vinceglb.filekit.sample.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "io.github.vinceglb.filekit.sample"
            packageVersion = "1.0.0"
        }
    }
}

dependencies {
    // App module
    implementation(projects.sample.shared)

    // Compose Desktop
    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutines.swing)

    // FileKit
    implementation(projects.filekitCore)
}
