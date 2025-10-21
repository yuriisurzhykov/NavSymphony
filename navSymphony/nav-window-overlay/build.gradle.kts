plugins {
    alias(libs.plugins.compose.compiler)
    id("com.surzhykov.plugins.core-module")
}

coreModuleConfig {
    namespaceSuffix.set("window")
}

dependencies {

    implementation(projects.navPresentation)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    //Jetpack compose dependencies
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material.theme.adapter)

    // Kotlin dependencies
    implementation(libs.kotlinx.collections.immutable)

    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}