package net.mamoe.mirai.clikt.parsers

import net.mamoe.mirai.clikt.core.IncorrectOptionValueCount
import net.mamoe.mirai.clikt.parameters.options.Option
import net.mamoe.mirai.clikt.parsers.OptionParser.ParseResult

/** An option that takes one more values */
public object OptionWithValuesParser : OptionParser {
    override fun parseLongOpt(
        option: Option, name: String, argv: List<String>,
        index: Int, explicitValue: String?,
    ): ParseResult {
        require(option.nvalues > 0) {
            "This parser can only be used with a fixed number of arguments. Try the flag parser instead."
        }
        val hasIncludedValue = explicitValue != null
        val consumedCount = if (hasIncludedValue) option.nvalues else option.nvalues + 1
        val endIndex = index + consumedCount - 1

        if (endIndex > argv.lastIndex) {
            throw IncorrectOptionValueCount(option, name)
        }

        val invocation = if (option.nvalues > 1) {
            var args = argv.slice((index + 1)..endIndex)
            if (explicitValue != null) args = listOf(explicitValue) + args
            OptionParser.Invocation(name, args)
        } else {
            OptionParser.Invocation(
                name, listOf(
                    explicitValue
                        ?: argv[index + 1]
                )
            )
        }
        return ParseResult(consumedCount, invocation)
    }

    override fun parseShortOpt(
        option: Option, name: String, argv: List<String>,
        index: Int, optionIndex: Int,
    ): ParseResult {
        val opt = argv[index]
        val hasIncludedValue = optionIndex != opt.lastIndex
        val explicitValue = if (hasIncludedValue) opt.substring(optionIndex + 1) else null
        return parseLongOpt(option, name, argv, index, explicitValue)
    }
}