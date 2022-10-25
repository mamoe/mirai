package net.mamoe.mirai.clikt.parameters.types

import net.mamoe.mirai.clikt.core.BadParameterValue
import net.mamoe.mirai.clikt.core.Context
import net.mamoe.mirai.clikt.parameters.arguments.ProcessedArgument
import net.mamoe.mirai.clikt.parameters.arguments.RawArgument
import net.mamoe.mirai.clikt.parameters.arguments.convert
import net.mamoe.mirai.clikt.parameters.options.OptionWithValues
import net.mamoe.mirai.clikt.parameters.options.RawOption
import net.mamoe.mirai.clikt.parameters.options.convert

internal fun valueToInt(context: Context, it: String): Int {
    return it.toIntOrNull() ?: throw BadParameterValue(context.localization.intConversionError(it))
}

/** Convert the argument values to an `Int` */
public fun RawArgument.int(): ProcessedArgument<Int, Int> = convert { valueToInt(context, it) }

/** Convert the option values to an `Int` */
public fun RawOption.int(): OptionWithValues<Int?, Int, Int> =
    convert({ localization.intMetavar() }) { valueToInt(context, it) }