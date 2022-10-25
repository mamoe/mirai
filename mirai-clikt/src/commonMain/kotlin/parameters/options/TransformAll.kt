@file:JvmMultifileClass
@file:JvmName("OptionWithValuesKt")

package net.mamoe.mirai.clikt.parameters.options

import net.mamoe.mirai.clikt.core.MissingOption
import net.mamoe.mirai.clikt.output.HelpFormatter
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

/**
 * Transform all calls to the option to the final option type.
 *
 * The input is a list of calls, one for each time the option appears on the command line. The values in the
 * list are the output of calls to [transformValues]. If the option does not appear from any source (command
 * line or envvar), this will be called with an empty list.
 *
 * Used to implement functions like [default] and [multiple].
 *
 * ## Example
 *
 * ```
 * val entries by option().transformAll { it.joinToString() }
 * ```
 *
 * @param defaultForHelp The help text for this option's default value if the help formatter is
 *   configured to show them, or null if this option has no default or the default value should not be
 *   shown.This does not affect behavior outside of help formatting.
 * @param showAsRequired Tell the help formatter that this option should be marked as required. This
 *   does not affect behavior outside of help formatting.
 */
public fun <AllT, EachT : Any, ValueT> NullableOption<EachT, ValueT>.transformAll(
    defaultForHelp: String? = this.helpTags[HelpFormatter.Tags.DEFAULT],
    showAsRequired: Boolean = HelpFormatter.Tags.REQUIRED in this.helpTags,
    transform: CallsTransformer<EachT, AllT>,
): OptionWithValues<AllT, EachT, ValueT> {
    val tags = this.helpTags.toMutableMap()

    if (showAsRequired) tags[HelpFormatter.Tags.REQUIRED] = ""
    else tags.remove(HelpFormatter.Tags.REQUIRED)

    if (defaultForHelp != null) tags[HelpFormatter.Tags.DEFAULT] = defaultForHelp
    else tags.remove(HelpFormatter.Tags.DEFAULT)

    return copy(transformValue, transformEach, transform, defaultValidator(), helpTags = tags)
}

/**
 * If the option is not called on the command line (and is not set in an envvar), use [value] for the option.
 *
 * This must be applied after all other transforms.
 *
 * You can customize how the default is shown to the user with [defaultForHelp].
 *
 * ### Example:
 *
 * ```
 * val opt: Pair<Int, Int> by option().int().pair().default(1 to 2)
 * ```
 */
public fun <EachT : Any, ValueT> NullableOption<EachT, ValueT>.default(
    value: EachT,
    defaultForHelp: String = value.toString(),
): OptionWithValues<EachT, EachT, ValueT> {
    return transformAll(defaultForHelp) { it.lastOrNull() ?: value }
}

/**
 * If the option is not called on the command line (and is not set in an envvar), call the [value] and use its
 * return value for the option.
 *
 * This must be applied after all other transforms. If the option is given on the command line, [value] will
 * not be called.
 *
 * You can customize how the default is shown to the user with [defaultForHelp]. The default value
 * is an empty string, so if you have the help formatter configured to show values, you should set
 * this value manually.
 *
 * ### Example:
 *
 * ```
 * val opt: Pair<Int, Int> by option().int().pair().defaultLazy { expensiveOperation() }
 * ```
 */
public inline fun <EachT : Any, ValueT> NullableOption<EachT, ValueT>.defaultLazy(
    defaultForHelp: String = "",
    crossinline value: () -> EachT,
): OptionWithValues<EachT, EachT, ValueT> {
    return transformAll(defaultForHelp) { it.lastOrNull() ?: value() }
}

/**
 * If the option is not called on the command line (and is not set in an envvar), throw a [MissingOption].
 *
 * This must be applied after all other transforms.
 *
 * ### Example:
 *
 * ```
 * val opt: Pair<Int, Int> by option().int().pair().required()
 * ```
 */
public fun <EachT : Any, ValueT> NullableOption<EachT, ValueT>.required(): OptionWithValues<EachT, EachT, ValueT> {
    return transformAll(showAsRequired = true) { it.lastOrNull() ?: throw MissingOption(option) }
}

/**
 * Make the option return a list of calls; each item in the list is the value of one call.
 *
 * If the option is never called, the list will be empty. This must be applied after all other transforms.
 *
 * ### Example:
 *
 * ```
 * val opt: List<Pair<Int, Int>> by option().int().pair().multiple()
 * ```
 *
 * @param default The value to use if the option is not supplied. Defaults to an empty list.
 * @param required If true, [default] is ignored and [MissingOption] will be thrown if no
 *   instances of the option are present on the command line.
 */
public fun <EachT : Any, ValueT> NullableOption<EachT, ValueT>.multiple(
    default: List<EachT> = emptyList(),
    required: Boolean = false,
): OptionWithValues<List<EachT>, EachT, ValueT> {
    return transformAll(showAsRequired = required) {
        when {
            it.isEmpty() && required -> throw MissingOption(option)
            it.isEmpty() && !required -> default
            else -> it
        }
    }
}

/**
 * Make a [multiple] option return a unique set of values
 *
 * ### Example:
 *
 * ```
 * val opt: Set<Int> by option().int().multiple().unique()
 * val opt2: Set<Int> by option().int().split(",").default(emptyList()).unique()
 * ```
 */
public fun <T, EachT, ValueT> OptionWithValues<List<T>, EachT, ValueT>.unique(): OptionWithValues<Set<T>, EachT, ValueT> {
    return copy(transformValue, transformEach, { transformAll(it).toSet() }, defaultValidator())
}

/**
 * Convert this option's values from a list of pairs into a map with key-value pairs from the list.
 *
 * If the same key appears more than once, the last one will be added to the map.
 */
public fun <A, B, EachT, ValueT> OptionWithValues<List<Pair<A, B>>, EachT, ValueT>.toMap(): OptionWithValues<Map<A, B>, EachT, ValueT> {
    return copy(transformValue, transformEach, { transformAll(it).toMap() }, defaultValidator())
}

/**
 * Change this option to take multiple values, each split on a [delimiter], and converted to a map.
 *
 * This is shorthand for [splitPair], [multiple], and [toMap].
 */
public fun RawOption.associate(delimiter: String = "="): OptionWithValues<Map<String, String>, Pair<String, String>, Pair<String, String>> {
    return splitPair(delimiter).multiple().toMap()
}