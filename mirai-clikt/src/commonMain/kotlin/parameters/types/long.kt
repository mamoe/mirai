package net.mamoe.mirai.clikt.parameters.types

import net.mamoe.mirai.clikt.core.BadParameterValue
import net.mamoe.mirai.clikt.core.Context
import net.mamoe.mirai.clikt.parameters.arguments.ProcessedArgument
import net.mamoe.mirai.clikt.parameters.arguments.RawArgument
import net.mamoe.mirai.clikt.parameters.arguments.convert
import net.mamoe.mirai.clikt.parameters.options.OptionWithValues
import net.mamoe.mirai.clikt.parameters.options.RawOption
import net.mamoe.mirai.clikt.parameters.options.convert

internal fun valueToLong(context: Context, it: String): Long {
    return it.toLongOrNull() ?: throw BadParameterValue(context.localization.intConversionError(it))
}

/** Convert the argument values to a `Long` */
public fun RawArgument.long(): ProcessedArgument<Long, Long> = convert { valueToLong(context, it) }

/** Convert the option values to a `Long` */
public fun RawOption.long(): OptionWithValues<Long?, Long, Long> =
    convert({ localization.intMetavar() }) { valueToLong(context, it) }