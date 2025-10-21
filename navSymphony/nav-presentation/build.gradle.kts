plugins {
    alias(libs.plugins.compose.compiler)
    id("com.surzhykov.plugins.core-module")
}

coreModuleConfig {
    namespaceSuffix.set("core.presentation")
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.kotlinx.collections.immutable)

    //Jetpack compose dependencies
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.ui)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}