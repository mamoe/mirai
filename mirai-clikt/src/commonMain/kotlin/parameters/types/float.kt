package net.mamoe.mirai.clikt.parameters.types

import net.mamoe.mirai.clikt.core.BadParameterValue
import net.mamoe.mirai.clikt.core.Context
import net.mamoe.mirai.clikt.parameters.arguments.ProcessedArgument
import net.mamoe.mirai.clikt.parameters.arguments.RawArgument
import net.mamoe.mirai.clikt.parameters.arguments.convert
import net.mamoe.mirai.clikt.parameters.options.OptionWithValues
import net.mamoe.mirai.clikt.parameters.options.RawOption
import net.mamoe.mirai.clikt.parameters.options.convert

private fun valueToFloat(context: Context, it: String): Float {
    return it.toFloatOrNull() ?: throw BadParameterValue(context.localization.floatConversionError(it))
}

/** Convert the argument values to a `Float` */
public fun RawArgument.float(): ProcessedArgument<Float, Float> = convert { valueToFloat(context, it) }

/** Convert the option values to a `Float` */
public fun RawOption.float(): OptionWithValues<Float?, Float, Float> =
    convert({ localization.floatMetavar() }) { valueToFloat(context, it) }