plugins {
    alias(libs.plugins.compose.compiler)
    id("com.surzhykov.plugins.core-module")
}

coreModuleConfig {
    namespaceSuffix.set("choreographer")
}

dependencies {
    implementation(projects.navDomain)
    implementation(projects.navPresentation)
    implementation(projects.navGraphCore)
    implementation(projects.navScreenCore)
    implementation(projects.navWindowOverlay)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.material)

    // Kotlin dependencies
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.collections.immutable)

    // Dependency injection
    implementation(libs.javax.inject)

    //Jetpack compose dependencies
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.activity)
    implementation(libs.androidx.compose.navigation)
    implementation(libs.androidx.compose.runtime.livedata)
    implementation(libs.androidx.compose.animation.graphics)

    // Dependencies required for unit tests
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}