@file:JvmMultifileClass
@file:JvmName("OptionWithValuesKt")

package net.mamoe.mirai.clikt.parameters.options

import net.mamoe.mirai.clikt.core.BadParameterValue
import net.mamoe.mirai.clikt.core.Context
import net.mamoe.mirai.clikt.core.UsageError
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName


/**
 * Convert the option's value type.
 *
 * The [conversion] is called once for each value in each invocation of the option. If any errors
 * are thrown, they are caught and a [BadParameterValue] is thrown with the error message. You can
 * call [fail][OptionTransformContext.fail] to throw a [BadParameterValue] manually.
 *
 * You can call `convert` more than once to wrap the result of the previous `convert`, but it cannot
 * be called after [transformAll] (e.g. [multiple]) or [transformValues] (e.g. [pair]).
 *
 * ## Example
 *
 * ```
 * val bd: BigDecimal? by option().convert { it.toBigDecimal() }
 * val fileText: ByteArray? by option().file().convert { it.readBytes() }
 * ```
 *
 * @param metavar The metavar for the type. Overridden by a metavar passed to [option].
 */
public inline fun <InT : Any, ValueT : Any> NullableOption<InT, InT>.convert(
    metavar: String,
    crossinline conversion: ValueConverter<InT, ValueT>,
): NullableOption<ValueT, ValueT> {
    return convert({ metavar }, conversion)
}

/**
 * Convert the option's value type.
 *
 * The [conversion] is called once for each value in each invocation of the option. If any errors
 * are thrown, they are caught and a [BadParameterValue] is thrown with the error message. You can
 * call [fail][OptionTransformContext.fail] to throw a [BadParameterValue] manually.
 *
 * You can call `convert` more than once to wrap the result of the previous `convert`, but it cannot
 * be called after [transformAll] (e.g. [multiple]) or [transformValues] (e.g. [pair]).
 *
 * ## Example
 *
 * ```
 * val bd: BigDecimal? by option().convert { it.toBigDecimal() }
 * val fileText: ByteArray? by option().file().convert { it.readBytes() }
 * ```
 *
 * @param metavar A lambda returning the metavar for the type. The lambda has a [Context] receiver
 *   for access to localization. Overridden by a metavar passed to [option].
 */
public inline fun <InT : Any, ValueT : Any> NullableOption<InT, InT>.convert(
    noinline metavar: Context.() -> String = { localization.defaultMetavar() },
    crossinline conversion: ValueConverter<InT, ValueT>,
): NullableOption<ValueT, ValueT> {
    val proc: ValueTransformer<ValueT> = {
        try {
            conversion(transformValue(it))
        } catch (err: UsageError) {
            err.paramName = name
            throw err
        } catch (err: Exception) {
            fail(err.message ?: "")
        }
    }
    return copy(
        proc, defaultEachProcessor(), defaultAllProcessor(), defaultValidator(),
        metavarWithDefault = metavarWithDefault.copy(default = metavar),
    )
}


/**
 * Change to option to take any number of values, separated by a [regex].
 *
 * This must be called after converting the value type, and before other transforms.
 *
 * ### Example:
 *
 * ```
 * val opt: List<Int>? by option().int().split(Regex(","))
 * ```
 *
 * Which can be called like this:
 *
 * `./program --opt 1,2,3`
 */
public fun <EachT : Any, ValueT> NullableOption<EachT, ValueT>.split(regex: Regex)
        : OptionWithValues<List<ValueT>?, List<ValueT>, ValueT> {
    return copy(
        transformValue = transformValue,
        transformEach = { it },
        transformAll = defaultAllProcessor(),
        validator = defaultValidator(),
        nvalues = 1,
        valueSplit = regex
    )
}

/**
 * Change to option to take any number of values, separated by a string [delimiter].
 *
 * This must be called after converting the value type, and before other transforms.
 *
 * ### Example:
 *
 * ```
 * val opt: List<Int>? by option().int().split(",")
 * ```
 *
 * Which can be called like this:
 *
 * `./program --opt 1,2,3`
 */
public fun <EachT : Any, ValueT> NullableOption<EachT, ValueT>.split(delimiter: String)
        : OptionWithValues<List<ValueT>?, List<ValueT>, ValueT> {
    return split(Regex.fromLiteral(delimiter))
}


/**
 * Split this option's value into two with a [delimiter].
 *
 * If the delimiter is not present in the value, the [second][Pair.second] part of the pair will be
 * an empty string. You can use [validate] to reject these values.
 *
 * You cannot call [convert] before this function, but you can call it after.
 *
 * ### Example:
 *
 * ```
 * val opt: option("-o").splitPair()
 * ```
 *
 * Which can be called like this:
 *
 * `./program -o key=value`
 */
public fun RawOption.splitPair(delimiter: String = "="): NullableOption<Pair<String, String>, Pair<String, String>> {
    return convert { it.substringBefore(delimiter) to it.substringAfter(delimiter, missingDelimiterValue = "") }
}