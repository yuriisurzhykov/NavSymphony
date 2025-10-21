plugins {
    id("com.surzhykov.plugins.core-module")
}

coreModuleConfig {
    namespaceSuffix.set("core.domain")
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.core.ktx)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    testImplementation(kotlin("test"))
}