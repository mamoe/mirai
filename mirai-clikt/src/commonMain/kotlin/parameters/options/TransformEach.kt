@file:JvmMultifileClass
@file:JvmName("OptionWithValuesKt")

package net.mamoe.mirai.clikt.parameters.options

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

/**
 * Change the number of values that this option takes.
 *
 * The input will be a list of size [nvalues], with each item in the list being the output of a call to
 * [convert]. [nvalues] must be 2 or greater, since options cannot take a variable number of values, and
 * [option] has [nvalues] = 1 by default. If you want to change the type of an option with one value, use
 * [convert] instead.
 *
 * Used to implement functions like [pair] and [triple]. This must be applied after value
 * [conversions][convert] and before [transformAll].
 *
 * ## Example
 *
 * ```
 * data class Square(val top: Int, val right: Int, val bottom: Int, val left: Int)
 * val square by option().int().transformValues(4) { Square(it[0], it[1], it[2], it[3]) }
 * ```
 */
public fun <EachInT : Any, EachOutT : Any, ValueT> NullableOption<EachInT, ValueT>.transformValues(
    nvalues: Int,
    transform: ArgsTransformer<ValueT, EachOutT>,
): NullableOption<EachOutT, ValueT> {
    require(nvalues != 0) { "Cannot set nvalues = 0. Use flag() instead." }
    require(nvalues > 0) { "Options cannot have nvalues < 0" }
    require(nvalues > 1) { "Cannot set nvalues = 1. Use convert() instead." }
    return copy(transformValue, transform, defaultAllProcessor(), defaultValidator(), nvalues = nvalues)
}

/**
 * Change this option to take two values, held in a [Pair].
 *
 * This must be called after converting the value type, and before other transforms.
 *
 * ### Example:
 *
 * ```
 * val opt: Pair<Int, Int>? by option().int().pair()
 * ```
 */
public fun <EachT : Any, ValueT> NullableOption<EachT, ValueT>.pair()
        : NullableOption<Pair<ValueT, ValueT>, ValueT> {
    return transformValues(nvalues = 2) { it[0] to it[1] }
}

/**
 * Change this option to take three values, held in a [Triple].
 *
 * This must be called after converting the value type, and before other transforms.
 *
 * ### Example:
 *
 * ```
 * val opt: Triple<Int, Int, Int>? by option().int().triple()
 * ```
 */
public fun <EachT : Any, ValueT> NullableOption<EachT, ValueT>.triple()
        : NullableOption<Triple<ValueT, ValueT, ValueT>, ValueT> {
    return transformValues(nvalues = 3) { Triple(it[0], it[1], it[2]) }
}