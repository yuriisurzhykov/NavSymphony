package com.surzhykov.navsymphony.core.presentation

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

/**
 * `NumberFormat` designed to be a communication interface with the UI to format any number in a
 * more readable format, e.g. to format all numbers using two digits always (for timers or so),
 * or to format any decimal number with certain format like 1,999.
 * */
@Immutable
interface NumberFormat {

    /**
     * Formats the input [number] and outputs a formatted [String]
     * @param number A number to be formatter
     * */
    @Stable
    fun format(number: Number): String

    /**
     * Abstract implementation of [NumberFormat] that takes [format] as a String and uses it to
     * output the number with a specific format representation.
     * */
    @Immutable
    abstract class Abstract(
        private val format: String,
    ) : NumberFormat {

        @Stable
        override fun format(number: Number): String {
            return format.format(number.toInt())
        }
    }

    /**
     * Formats any number in the following format: 1 -> "01", 11 -> "11"
     * */
    @Immutable
    class TwoDigits : Abstract("%02d")
}