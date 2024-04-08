rootProject.name = "PickerKotlin"

include(":picker-core")
include(":picker-compose")
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
