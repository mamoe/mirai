package net.mamoe.mirai.clikt.parameters.types

import net.mamoe.mirai.clikt.core.Context
import net.mamoe.mirai.clikt.parameters.arguments.ProcessedArgument
import net.mamoe.mirai.clikt.parameters.options.OptionWithValues

private inline fun <T : Comparable<T>> checkRange(
    it: T,
    min: T?,
    max: T?,
    clamp: Boolean,
    context: Context,
    fail: (String) -> Unit,
): T {
    require(min == null || max == null || min < max) { "min must be less than max" }
    if (clamp) {
        if (min != null && it < min) return min
        if (max != null && it > max) return max
    } else if (min != null && it < min || max != null && it > max) {
        fail(
            when {
                min == null -> context.localization.rangeExceededMax(it.toString(), max.toString())
                max == null -> context.localization.rangeExceededMin(it.toString(), min.toString())
                else -> context.localization.rangeExceededBoth(it.toString(), min.toString(), max.toString())
            }
        )
    }
    return it
}


// Arguments
/**
 * Restrict the argument values to fit into a range.
 *
 * By default, conversion fails if the value is outside the range, but if [clamp] is true, the value will be
 * silently clamped to fit in the range.
 *
 * This must be called before transforms like `pair`, `default`, or `multiple`, since it checks each
 * individual value.
 *
 * ### Example:
 *
 * ```
 * argument().int().restrictTo(max=10, clamp=true).default(10)
 * ```
 */
public fun <T : Comparable<T>> ProcessedArgument<T, T>.restrictTo(
    min: T? = null,
    max: T? = null,
    clamp: Boolean = false
)
        : ProcessedArgument<T, T> {
    return copy(
        { checkRange(transformValue(it), min, max, clamp, context) { m -> fail(m) } },
        transformAll,
        transformValidator
    )
}

/**
 * Restrict the argument values to fit into a range.
 *
 * By default, conversion fails if the value is outside the range, but if [clamp] is true, the value will be
 * silently clamped to fit in the range.
 *
 * This must be called before transforms like `pair`, `default`, or `multiple`, since it checks each
 * individual value.
 *
 * ### Example:
 *
 * ```
 * argument().int().restrictTo(1..10, clamp=true).default(10)
 * ```
 */
public fun <T : Comparable<T>> ProcessedArgument<T, T>.restrictTo(
    range: ClosedRange<T>,
    clamp: Boolean = false,
): ProcessedArgument<T, T> {
    return restrictTo(range.start, range.endInclusive, clamp)
}

// Options

/**
 * Restrict the option values to fit into a range.
 *
 * By default, conversion fails if the value is outside the range, but if [clamp] is true, the value will be
 * silently clamped to fit in the range.
 *
 * This must be called before transforms like `pair`, `default`, or `multiple`, since it checks each
 * individual value.
 *
 * ### Example:
 *
 * ```
 * option().int().restrictTo(max=10, clamp=true).default(10)
 * ```
 */
public fun <T : Comparable<T>> OptionWithValues<T?, T, T>.restrictTo(
    min: T? = null,
    max: T? = null,
    clamp: Boolean = false,
): OptionWithValues<T?, T, T> {
    return copy(
        { checkRange(transformValue(it), min, max, clamp, context) { m -> fail(m) } },
        transformEach,
        transformAll,
        transformValidator
    )
}


/**
 * Restrict the option values to fit into a range.
 *
 * By default, conversion fails if the value is outside the range, but if [clamp] is true, the value will be
 * silently clamped to fit in the range.
 *
 * This must be called before transforms like `pair`, `default`, or `multiple`, since it checks each
 * individual value.
 *
 * ### Example:
 *
 * ```
 * option().int().restrictTo(1..10, clamp=true).default(10)
 * ```
 */
public fun <T : Comparable<T>> OptionWithValues<T?, T, T>.restrictTo(
    range: ClosedRange<T>,
    clamp: Boolean = false,
): OptionWithValues<T?, T, T> {
    return restrictTo(range.start, range.endInclusive, clamp)
}