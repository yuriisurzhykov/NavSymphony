package com.surzhykov.navsymphony.core.presentation

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit

@Immutable
interface FormatType {

    @Stable
    fun annotate(string: CharSequence): AnnotatedString

    class Color(
        private val color: androidx.compose.ui.graphics.Color,
    ) : FormatType {
        override fun annotate(string: CharSequence) = buildAnnotatedString {
            withStyle(SpanStyle(color = color)) {
                append(string)
            }
        }
    }

    class Bold(
        private val weight: FontWeight = FontWeight.Bold,
        private val size: TextUnit = TextUnit.Unspecified,
        private val style: FontStyle = FontStyle.Normal,
    ) : FormatType {
        override fun annotate(string: CharSequence) = buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    fontSize = size,
                    fontWeight = weight,
                    fontStyle = style
                )
            ) {
                append(string)
            }
        }
    }
}