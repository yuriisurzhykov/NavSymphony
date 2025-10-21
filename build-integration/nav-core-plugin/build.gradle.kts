plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

// Define the plugin with the id and implementation class
gradlePlugin {
    plugins {
        create("core-module") {
            id = "com.surzhykov.plugins.core-module"
            version = "1.0"
            description = "Plugin that helps to setup core library build.gradle file"
            displayName = "Core Library Plugin"
            implementationClass = "com.surzhykov.plugins.cmodule.CoreLibraryConventionPlugin"
        }
    }
}

dependencies {
    implementation(pluginLibs.gradle.plugin.library)
    implementation(pluginLibs.kotlin.android.plugin.library)
    implementation(pluginLibs.compose.compiler.plugin.library)
}