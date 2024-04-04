rootProject.name = "PickerKotlin"

include(":picker-core")
include(":samples:sample-core:shared")
include(":samples:sample-core:composeApp")

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
