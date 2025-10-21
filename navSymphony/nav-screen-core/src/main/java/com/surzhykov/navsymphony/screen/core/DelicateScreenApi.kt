package com.surzhykov.navsymphony.screen.core

@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "The API you trying to access or override is a delicate, meaning that it may result" +
        "in something unpredictable. If you sure that you need to override method or functionality" +
        "opt in to this annotation."
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class DelicateScreenApi
