plugins {
    alias(libs.plugins.compose.compiler)
    id("com.surzhykov.plugins.core-module")
}

coreModuleConfig {
    namespaceSuffix.set("screen.shared")
}

dependencies {
    implementation(projects.navDomain)
    implementation(projects.navPresentation)
    implementation(projects.navScreenCore)
    implementation(projects.navGraphCore)
    implementation(projects.navChoreographer)
    implementation(projects.navWindowOverlay)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.android)

    // Kotlin dependencies
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.collections.immutable)

    // Dependency injection
    implementation(libs.javax.inject)

    //Jetpack compose dependencies
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.animation.graphics)

    // Dependencies required for unit tests
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}