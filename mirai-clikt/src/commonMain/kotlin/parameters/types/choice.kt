package net.mamoe.mirai.clikt.parameters.types

import net.mamoe.mirai.clikt.core.Context
import net.mamoe.mirai.clikt.parameters.arguments.ProcessedArgument
import net.mamoe.mirai.clikt.parameters.arguments.RawArgument
import net.mamoe.mirai.clikt.parameters.arguments.convert
import net.mamoe.mirai.clikt.parameters.options.NullableOption
import net.mamoe.mirai.clikt.parameters.options.RawOption
import net.mamoe.mirai.clikt.parameters.options.convert

private fun mvar(choices: Iterable<String>): String {
    return choices.joinToString("|", prefix = "[", postfix = "]")
}

private fun errorMessage(context: Context, choice: String, choices: Map<String, *>): String {
    return context.localization.invalidChoice(choice, choices.keys.toList())
}

// arguments

/**
 * Convert the argument based on a fixed set of values.
 *
 * If [ignoreCase] is `true`, the argument will accept values is any mix of upper and lower case.
 *
 * ### Example:
 *
 * ```
 * argument().choice(mapOf("foo" to 1, "bar" to 2))
 * ```
 */
public fun <T : Any> RawArgument.choice(choices: Map<String, T>, ignoreCase: Boolean = false): ProcessedArgument<T, T> {
    require(choices.isNotEmpty()) { "Must specify at least one choice" }
    val c = if (ignoreCase) choices.mapKeys { it.key.lowercase() } else choices
    return convert {
        c[if (ignoreCase) it.lowercase() else it] ?: fail(errorMessage(context, it, choices))
    }
}

/**
 * Convert the argument based on a fixed set of values.
 *
 * If [ignoreCase] is `true`, the argument will accept values is any mix of upper and lower case.
 *
 * ### Example:
 *
 * ```
 * argument().choice("foo" to 1, "bar" to 2)
 * ```
 */
public fun <T : Any> RawArgument.choice(
    vararg choices: Pair<String, T>,
    ignoreCase: Boolean = false,
): ProcessedArgument<T, T> {
    return choice(choices.toMap(), ignoreCase)
}

/**
 * Restrict the argument to a fixed set of values.
 *
 * If [ignoreCase] is `true`, the argument will accept values is any mix of upper and lower case.
 * The argument's final value will always match the case of the corresponding value in [choices].
 *
 * ### Example:
 *
 * ```
 * argument().choice("foo", "bar")
 * ```
 */
public fun RawArgument.choice(vararg choices: String, ignoreCase: Boolean = false): ProcessedArgument<String, String> {
    return choice(choices.associateBy { it }, ignoreCase)
}

/**
 * Convert the argument to the values of an enum.
 *
 * If [ignoreCase] is `false`, the argument will only accept values that match the case of the enum values.
 *
 * ### Example:
 *
 * ```
 * enum class Size { SMALL, LARGE }
 * argument().enum<Size>()
 * ```
 *
 * @param key A block that returns the command line value to use for an enum value. The default is
 *   the enum name.
 */
public inline fun <reified T : Enum<T>> RawArgument.enum(
    ignoreCase: Boolean = true,
    key: (T) -> String = { it.name },
): ProcessedArgument<T, T> {
    return choice(enumValues<T>().associateBy { key(it) }, ignoreCase)
}

// options

/**
 * Convert the option based on a fixed set of values.
 *
 * If [ignoreCase] is `true`, the option will accept values is any mix of upper and lower case.
 *
 * ### Example:
 *
 * ```
 * option().choice(mapOf("foo" to 1, "bar" to 2))
 * ```
 *
 * @see net.mamoe.mirai.clikt.parameters.groups.groupChoice
 */
public fun <T : Any> RawOption.choice(
    choices: Map<String, T>,
    metavar: String = mvar(choices.keys),
    ignoreCase: Boolean = false,
): NullableOption<T, T> {
    require(choices.isNotEmpty()) { "Must specify at least one choice" }
    val c = if (ignoreCase) choices.mapKeys { it.key.lowercase() } else choices
    return convert(metavar) {
        c[if (ignoreCase) it.lowercase() else it] ?: fail(errorMessage(context, it, choices))
    }
}

/**
 * Convert the option based on a fixed set of values.
 *
 * If [ignoreCase] is `true`, the option will accept values is any mix of upper and lower case.
 *
 * ### Example:
 *
 * ```
 * option().choice("foo" to 1, "bar" to 2)
 * ```
 *
 * @see net.mamoe.mirai.clikt.parameters.groups.groupChoice
 */
public fun <T : Any> RawOption.choice(
    vararg choices: Pair<String, T>,
    metavar: String = mvar(choices.map { it.first }),
    ignoreCase: Boolean = false,
): NullableOption<T, T> {
    return choice(choices.toMap(), metavar, ignoreCase)
}

/**
 * Restrict the option to a fixed set of values.
 *
 * If [ignoreCase] is `true`, the option will accept values is any mix of upper and lower case.
 * The option's final value will always match the case of the corresponding value in [choices].
 *
 * ### Example:
 *
 * ```
 * option().choice("foo", "bar")
 * ```
 */
public fun RawOption.choice(
    vararg choices: String,
    metavar: String = mvar(choices.asIterable()),
    ignoreCase: Boolean = false,
): NullableOption<String, String> {
    return choice(choices.associateBy { it }, metavar, ignoreCase)
}

/**
 * Convert the option to the values of an enum.
 *
 * If [ignoreCase] is `false`, the option will only accept values that match the case of the enum values.
 *
 * ### Example:
 *
 * ```
 * enum class Size { SMALL, LARGE }
 * option().enum<Size>()
 * ```
 *
 * @param key A block that returns the command line value to use for an enum value. The default is
 *   the enum name.
 */
public inline fun <reified T : Enum<T>> RawOption.enum(
    ignoreCase: Boolean = true,
    key: (T) -> String = { it.name },
): NullableOption<T, T> {
    return choice(enumValues<T>().associateBy { key(it) }, ignoreCase = ignoreCase)
}