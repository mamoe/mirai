package net.mamoe.mirai.clikt.parameters.types

import net.mamoe.mirai.clikt.core.BadParameterValue
import net.mamoe.mirai.clikt.core.Context
import net.mamoe.mirai.clikt.parameters.arguments.ProcessedArgument
import net.mamoe.mirai.clikt.parameters.arguments.RawArgument
import net.mamoe.mirai.clikt.parameters.arguments.convert
import net.mamoe.mirai.clikt.parameters.options.OptionWithValues
import net.mamoe.mirai.clikt.parameters.options.RawOption
import net.mamoe.mirai.clikt.parameters.options.convert

private fun valueToDouble(context: Context, it: String): Double {
    return it.toDoubleOrNull() ?: throw BadParameterValue(context.localization.floatConversionError(it))
}

/** Convert the argument values to a `Double` */
public fun RawArgument.double(): ProcessedArgument<Double, Double> = convert { valueToDouble(context, it) }

/** Convert the option values to a `Double` */
public fun RawOption.double(): OptionWithValues<Double?, Double, Double> =
    convert({ localization.floatMetavar() }) { valueToDouble(context, it) }