@file:Suppress("UnstableApiUsage")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

rootProject.name = "FileKit"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":filekit-coil")
include(":filekit-core")
include(":filekit-dialogs")
include(":filekit-dialogs-compose")
include(":samples:sample-core:shared")
include(":samples:sample-core:composeApp")
include(":samples:sample-compose:composeApp")
include(":samples:sample-file-explorer:composeApp")
