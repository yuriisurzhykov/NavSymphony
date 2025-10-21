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
include(":nav-menu-core")
include(":nav-screen-core")

includeBuild("../build-integration")