import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
	alias(libs.plugins.kotlinMultiplatform)
	alias(libs.plugins.jetbrainsCompose)
	alias(libs.plugins.androidApplication)
}

kotlin {
	androidTarget {
		compilations.all {
			kotlinOptions {
				jvmTarget = "17"
			}
		}
	}

	jvmToolchain(17)
	jvm("desktop") {
		compilations.all {
			kotlinOptions.jvmTarget = "17"
		}
	}

	listOf(
		iosX64(),
		iosArm64(),
		iosSimulatorArm64()
	).forEach { iosTarget ->
		iosTarget.binaries.framework {
			baseName = "ComposeApp"
			isStatic = true
		}
	}

	@OptIn(ExperimentalWasmDsl::class)
	wasmJs {
		moduleName = "composeApp"
		browser {
			commonWebpackConfig {
				outputFileName = "composeApp.js"
			}
		}
		binaries.executable()
	}

	sourceSets {
		val desktopMain by getting
		val wasmJsMain by getting

		commonMain.dependencies {
			// Compose
			implementation(compose.runtime)
			implementation(compose.foundation)
			implementation(compose.material3)
			implementation(compose.ui)
			implementation(compose.components.resources)

			// Shared
			implementation(projects.samples.sampleCore.shared)

			// Koin
			implementation(libs.koin.compose)

			// Coil3
			implementation(libs.coil.compose)
		}

		androidMain.dependencies {
			implementation(libs.androidx.activity.compose)
		}

		desktopMain.dependencies {
			// Compose
			implementation(compose.desktop.currentOs)

			// Coroutines
			implementation(libs.kotlinx.coroutines.swing)
		}
	}
}

android {
	namespace = "io.github.vinceglb.sample.core.compose"
	compileSdk = 34

	defaultConfig {
		minSdk = 34
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}
}

compose.desktop {
	application {
		mainClass = "io.github.vinceglb.sample.core.compose.MainKt"

		nativeDistributions {
			targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
			packageName = "io.github.vinceglb.sample.core.compose"
			packageVersion = "1.0.0"
		}
	}
}

compose.experimental {
	web.application {}
}
