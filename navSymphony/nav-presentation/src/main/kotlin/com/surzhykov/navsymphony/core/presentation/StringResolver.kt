package com.surzhykov.navsymphony.core.presentation

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.core.text.toSpannable
import java.util.Locale

@Immutable
interface StringResolver : ResourceResolver<CharSequence> {

    @Composable
    fun asString(): String = resolve().toString()

    interface Annotated : StringResolver

    @Immutable
    open class Resource(
        @param:StringRes private val stringRes: Int,
        private val capitalize: Boolean = false,
    ) : StringResolver {
        @Composable
        override fun resolve() = stringResource(stringRes).let {
            if (capitalize) it.uppercase(Locale.getDefault())
            else it
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Resource) return false

            if (stringRes != other.stringRes) return false
            if (capitalize != other.capitalize) return false

            return true
        }

        override fun hashCode(): Int {
            var result = stringRes
            result = 31 * result + capitalize.hashCode()
            return result
        }

        override fun toString(): String {
            return "Resource(stringRes=$stringRes, capitalize=$capitalize)"
        }
    }

    @Immutable
    class Formatter(
        @StringRes private val stringFormatRes: Int,
        private vararg val args: StringResolver,
    ) : Resource(stringFormatRes) {
        @Composable
        override fun resolve() = super.resolve()
            .format(*args.map { it.resolve() }.toTypedArray())

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Formatter) return false
            if (!super.equals(other)) return false

            if (stringFormatRes != other.stringFormatRes) return false
            if (!args.contentEquals(other.args)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + stringFormatRes
            result = 31 * result + args.contentHashCode()
            return result
        }
    }

    @Immutable
    class List(
        private vararg val resolvers: StringResolver,
        private val separator: String = ", ",
    ) : StringResolver {
        @Composable
        override fun resolve(): CharSequence {
            val resultBuilder = StringBuilder()
            resolvers.forEachIndexed { index, resolver ->
                resultBuilder.append(resolver.resolve())
                if (index < resolvers.size - 1) {
                    resultBuilder.append(separator)
                }
            }
            return resultBuilder.toSpannable()
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is List) return false

            if (!resolvers.contentEquals(other.resolvers)) return false
            if (separator != other.separator) return false

            return true
        }

        override fun hashCode(): Int {
            var result = resolvers.contentHashCode()
            result = 31 * result + separator.hashCode()
            return result
        }

        override fun toString(): String {
            return "List(resolvers=${resolvers.contentToString()}, separator='$separator')"
        }
    }

    @Immutable
    class Objects(
        @StringRes private val stringFormatRes: Int,
        private vararg val args: Any,
    ) : Resource(stringFormatRes) {

        @Composable
        override fun resolve() = super.resolve().format(*args)

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Objects) return false

            if (stringFormatRes != other.stringFormatRes) return false
            if (!args.contentEquals(other.args)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = stringFormatRes
            result = 31 * result + args.contentHashCode()
            return result
        }

        override fun toString(): String {
            return "Objects(stringFormatRes=$stringFormatRes, args=${args.contentToString()})"
        }
    }

    @Immutable
    data class BaseString(
        private val string: String,
    ) : StringResolver {
        @Composable
        override fun resolve() = string

        override fun toString(): String = string
    }

    @Immutable
    data class AnnotateBase(
        private val value: Any,
    ) : Annotated {
        @Composable
        override fun resolve(): CharSequence = value.toString()
    }

    @Immutable
    data class AnnotateColor(
        private val resolver: StringResolver,
        private val color: Color,
    ) : Annotated {
        @Composable
        override fun resolve() = buildAnnotatedString {
            withStyle(SpanStyle(color = color)) {
                append(resolver.resolve())
            }
        }
    }

    @Immutable
    data class AnnotateFormat(
        private val resolver: StringResolver,
        private val formatType: FormatType,
    ) : Annotated {
        @Composable
        override fun resolve(): CharSequence = formatType.annotate(resolver.resolve())
    }

    @Immutable
    class AnnotateArray(
        private vararg val resolver: Annotated,
    ) : Annotated {
        @Composable
        override fun resolve() = buildAnnotatedString {
            resolver.forEach { append(it.resolve()) }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is AnnotateArray) return false

            if (!resolver.contentEquals(other.resolver)) return false

            return true
        }

        override fun hashCode(): Int {
            return resolver.contentHashCode()
        }

        override fun toString(): String {
            return "AnnotateArray(resolver=${resolver.contentToString()})"
        }
    }

    @Immutable
    class AnnotateFormatter(
        @StringRes private val formatString: Int,
        private vararg val resolvers: Annotated,
    ) : Annotated {
        @Composable
        override fun resolve() = buildAnnotatedString {
            val rawString = stringResource(formatString)
            val parts = rawString.split("%s")

            parts.forEachIndexed { index, part ->
                append(part)
                if (index < resolvers.size) {
                    append(resolvers[index].resolve())
                }
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is AnnotateFormatter) return false

            if (formatString != other.formatString) return false
            if (!resolvers.contentEquals(other.resolvers)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = formatString
            result = 31 * result + resolvers.contentHashCode()
            return result
        }
    }

    companion object {

        /**
         * Creates an empty [StringResolver].
         *
         * @return A [BaseString] resolver for an empty string.
         */
        fun empty(): StringResolver = BaseString("")

        /**
         * Creates a [StringResolver] from a [String].
         *
         * @param string The string to resolve.
         * @return A [BaseString] resolver for the given string.
         */
        @JvmStatic
        fun from(string: String) = BaseString(string)

        /**
         * Creates a [StringResolver] from a string resource.
         *
         * @param stringRes The resource ID of the string.
         * @param capitalize Whether to capitalize the resolved string. Defaults to false.
         * @return A [Resource] resolver for the given string resource.
         */
        @JvmStatic
        fun from(@StringRes stringRes: Int, capitalize: Boolean = false) =
            Resource(stringRes, capitalize)

        /**
         * Creates a [StringResolver] that formats a string resource with other [StringResolver]
         * instances.
         *
         * @param formatString The resource ID of the format string.
         * @param resolvers The [StringResolver] instances to use as arguments for the format string.
         * @return A [Formatter] resolver for the formatted string.
         */
        @JvmStatic
        fun from(@StringRes formatString: Int, vararg resolvers: StringResolver) =
            Formatter(formatString, *resolvers)

        /**
         * Creates a [StringResolver] that formats a string resource with a variable number of
         * arguments.
         *
         * @param formatString The resource ID of the format string.
         * @param args The arguments to use for the format string.
         * @return An [Objects] resolver for the formatted string.
         */
        fun from(@StringRes formatString: Int, vararg args: Any) = Objects(formatString, *args)
    }
}