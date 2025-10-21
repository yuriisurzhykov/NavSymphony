dependencyResolutionManagement {
    versionCatalogs {
        create("pluginLibs") {
            // the path is relative to the build-integration
            from(files("gradle/libs.versions.toml"))
        }
        create("props") {
            // the path is relative to the build-integration
            from(files("../gradle/props.versions.toml"))
        }
    }
}

include(":nav-core-plugin")
