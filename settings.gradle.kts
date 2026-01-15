@file:Suppress("UnstableApiUsage")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

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

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "FileKit"

include(":filekit-coil")
include(":filekit-core")
include(":filekit-dialogs")
include(":filekit-dialogs-compose")
include(":sample:androidApp")
include(":sample:desktopApp")
include(":sample:shared")
include(":sample:webApp")

// include(":samples-old:sample-core:shared")
// include(":samples-old:sample-core:composeApp")
// include(":samples-old:sample-compose:composeApp")
// include(":samples-old:sample-file-explorer:composeApp")
