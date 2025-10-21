package com.surzhykov.plugins.cmodule

import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.gradle.LibraryExtension
import org.gradle.api.JavaVersion.toVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeFeatureFlag
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

@Suppress("unused")
class CoreLibraryConventionPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        with(project.pluginManager) {
            apply(ANDROID_LIBRARY_PLUGIN_ID)
            apply(KOTLIN_ANDROID_PLUGIN_ID)
        }

        val libs = project.extensions
            .getByType(VersionCatalogsExtension::class.java)
            .named("props")
        val extension = project.extensions
            .create("coreModuleConfig", CoreModuleConventionExtension::class.java)

        val compileSdk = libs.getProperty("compile-sdk").toInt()
        val minSdk = libs.getProperty("min-sdk").toInt()
        val jvmSource = libs.getProperty("jvm-source")
        val jvmTarget = libs.getProperty("jvm-target")
        val projectId = libs.getProperty("project-id")

        /* Setup Android library */
        val androidComponents =
            project.extensions.getByType(LibraryAndroidComponentsExtension::class.java)
        androidComponents.finalizeDsl { libExt ->
            if (extension.namespaceSuffix.get().isEmpty()) {
                project.logger.error("Required property `namespaceSuffix` is not defined! Please define it using `coreModuleConfig.namespaceSuffix`")
            }
            libExt.namespace = "${projectId}.${extension.namespaceSuffix.get()}"
            libExt.compileSdk = compileSdk
            libExt.defaultConfig {
                this.minSdk = minSdk
                this.testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                consumerProguardFiles("consumer-rules.pro")
            }
            libExt.buildTypes {
                getByName("release") {
                    isMinifyEnabled = false
                    proguardFiles(
                        libExt.getDefaultProguardFile("proguard-android-optimize.txt"),
                        "proguard-rules.pro",
                    )
                }
            }
            libExt.compileOptions {
                sourceCompatibility = toVersion(jvmSource)
                targetCompatibility = toVersion(jvmTarget)
            }
        }

        /* Setup Jetpack Compose */
        project.afterEvaluate {
            val useCompose = project.pluginManager.hasPlugin(COMPOSE_PLUGIN_ID)
            if (useCompose) {
                val composeComponent =
                    project.extensions.getByType(ComposeCompilerGradlePluginExtension::class.java)
                composeComponent.includeSourceInformation.set(true)
                composeComponent.featureFlags.set(
                    setOf(
                        ComposeFeatureFlag.OptimizeNonSkippingGroups,
                        ComposeFeatureFlag.PausableComposition,
                        ComposeFeatureFlag.StrongSkipping
                    )
                )
            }
        }

        /* Setup Kotlin Compiler options */
        project.extensions.configure(KotlinAndroidProjectExtension::class.java) {
            compilerOptions {
                this.jvmTarget.set(JvmTarget.fromTarget(jvmTarget))
            }
        }

        if (project.logger.isDebugEnabled) {
            project.afterEvaluate {
                project.extensions.configure(LibraryExtension::class.java) {
                    project.logger.debug("coreModuleConfig.namespaceSuffix: ${extension.namespaceSuffix}")
                    project.logger.debug("coreModuleConfig.namespace: ${this.namespace}")
                    project.logger.debug("coreModuleConfig.compile-sdk: ${this.compileSdk}")
                    project.logger.debug("coreModuleConfig.min-sdk: ${this.defaultConfig.minSdk}")
                    project.logger.debug("coreModuleConfig.jvm-source: ${this.compileOptions.sourceCompatibility}")
                    project.logger.debug("coreModuleConfig.jvm-target: ${this.compileOptions.targetCompatibility}")
                }
            }
        }
    }

    private fun VersionCatalog.getProperty(name: String): String {
        return try {
            findVersion(name).get().requiredVersion
        } catch (e: NoSuchElementException) {
            throw IllegalStateException("Property named '$name' is not defined in 'root/props.versions.toml'")
        }
    }

    companion object {
        private const val COMPOSE_PLUGIN_ID = "org.jetbrains.kotlin.plugin.compose"
        private const val KOTLIN_ANDROID_PLUGIN_ID = "org.jetbrains.kotlin.android"
        private const val ANDROID_LIBRARY_PLUGIN_ID = "com.android.library"
    }
}