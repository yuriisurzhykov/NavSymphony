package com.surzhykov.navsymphony.window.dialogs

import androidx.compose.runtime.Stable

@Stable
interface DialogScope {
    @Stable
    class Base : DialogScope {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Base) return false
            return true
        }

        override fun hashCode(): Int {
            return javaClass.hashCode()
        }
    }
}