rootProject.name = "FileKit"

include(":filekit-core")
include(":filekit-compose")
include(":filekit-platformfile")
include(":samples:sample-core:shared")
include(":samples:sample-core:composeApp")
include(":samples:sample-compose:composeApp")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
