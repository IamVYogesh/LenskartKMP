rootProject.name = "LenskartKMP"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
    }
}

include(":composeApp")
include(":core:common", ":core:domain", ":core:network", ":core:database", ":core:datastore", ":core:data", ":core:designsystem", ":core:ui")
include(":feature:home", ":feature:catalog", ":feature:product", ":feature:cart", ":feature:wishlist", ":feature:account", ":feature:tryon")
