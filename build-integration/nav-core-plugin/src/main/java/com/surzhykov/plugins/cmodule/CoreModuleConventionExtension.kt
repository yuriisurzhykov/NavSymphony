package com.surzhykov.plugins.cmodule

import org.gradle.api.provider.Property

abstract class CoreModuleConventionExtension {
    abstract val namespaceSuffix: Property<String>

    init {
        namespaceSuffix.convention("")
    }
}