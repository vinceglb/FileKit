pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "FileKit"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":filekit-coil")
include(":filekit-core")
include(":filekit-dialog")
include(":filekit-dialog-compose")
include(":samples:sample-core:shared")
include(":samples:sample-core:composeApp")
include(":samples:sample-compose:composeApp")
