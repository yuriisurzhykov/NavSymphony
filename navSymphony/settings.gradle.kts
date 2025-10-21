enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { setUrl("https://jitpack.io") }
    }
    versionCatalogs {
        create("libs") {
            // Single source of truth for all versions and constants and all included builds
            // e.g. includeBuild(...)
            from(files("../gradle/libs.versions.toml"))
        }
        create("props") {
            // Single source of truth for all versions and constants and all included builds
            // e.g. includeBuild(...)
            from(files("../gradle/props.versions.toml"))
        }
    }
}
rootProject.name = "navsymphony-lib"
include(":nav-choreographer")
include(":nav-domain")
include(":nav-graph-builder")
include(":nav-graph-core")
include(":nav-menu-core")
include(":nav-presentation")
include(":nav-screen-core")
include(":nav-screen-shared")
include(":nav-window-overlay")

includeBuild("../build-integration")