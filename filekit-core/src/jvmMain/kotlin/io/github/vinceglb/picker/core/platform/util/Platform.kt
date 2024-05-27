package io.github.vinceglb.picker.core.platform.util

internal object PlatformUtil {
	val current: Platform
		get() {
			val system = System.getProperty("os.name").lowercase()
			return if (system.contains("win")) {
				Platform.Windows
			} else if (
				system.contains("nix") ||
				system.contains("nux") ||
				system.contains("aix")
			) {
				Platform.Linux
			} else if (system.contains("mac")) {
				Platform.MacOS
			} else {
				Platform.Linux
			}
		}
}

internal enum class Platform {
	Linux,
	MacOS,
	Windows
}
