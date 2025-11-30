package io.github.vinceglb.filekit.utils

public object PlatformUtil {
    public val current: Platform
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

public enum class Platform {
    Linux,
    MacOS,
    Windows,
}
