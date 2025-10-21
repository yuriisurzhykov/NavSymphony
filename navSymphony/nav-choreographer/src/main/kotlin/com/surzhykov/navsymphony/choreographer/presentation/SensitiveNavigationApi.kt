package com.surzhykov.navsymphony.choreographer.presentation

@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "This navigation API is sensitive and should not be used with careful consideration."
)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class SensitiveNavigationApi
