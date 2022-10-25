package net.mamoe.mirai.clikt.parsers

import net.mamoe.mirai.clikt.core.*
import net.mamoe.mirai.clikt.internal.finalizeOptions
import net.mamoe.mirai.clikt.parameters.arguments.Argument
import net.mamoe.mirai.clikt.parameters.options.EagerOption
import net.mamoe.mirai.clikt.parameters.options.Option
import net.mamoe.mirai.clikt.parameters.options.splitOptionPrefix
import net.mamoe.mirai.clikt.parsers.OptionParser.Invocation

private data class OptInvocation(val opt: Option, val inv: Invocation)
private data class OptParseResult(val consumed: Int, val unknown: List<String>, val known: List<OptInvocation>)

internal object Parser {
    suspend fun parse(argv: List<String>, context: Context) {
        parse(argv, context, 0, true)
    }

    private suspend fun parse(
        argv: List<String>,
        context: Context,
        startingArgI: Int,
        canRun: Boolean,
    ): Pair<List<String>, Int> {
        var tokens = argv
        val command = context.command
        val aliases = command.aliases()
        val subcommands = command._subcommands.associateBy { it.commandName }
        val optionsByName = HashMap<String, Option>()
        val arguments = command._arguments
        val prefixes = mutableSetOf<String>()
        val longNames = mutableSetOf<String>()
        val hasMultipleSubAncestor =
            generateSequence(context.parent) { it.parent }.any { it.command.allowMultipleSubcommands }

        for (option in command._options) {
            require(option.names.isNotEmpty() || option.secondaryNames.isNotEmpty()) {
                "options must have at least one name"
            }

            for (name in option.names + option.secondaryNames) {
                optionsByName[name] = option
                if (name.length > 2) longNames += name
                prefixes += splitOptionPrefix(name).first
            }
        }
        prefixes.remove("")

        if (startingArgI > tokens.lastIndex && command.printHelpOnEmptyArgs) {
            throw PrintHelpMessage(command, error = true)
        }

        val positionalArgs = ArrayList<String>()
        var i = startingArgI
        var subcommand: CliktCommand? = null
        var canParseOptions = true
        val invocations = mutableListOf<OptInvocation>()
        var minAliasI = 0

        fun isLongOptionWithEquals(prefix: String, token: String): Boolean {
            if ("=" !in token) return false
            if (prefix.isEmpty()) return false
            if (prefix.length > 1) return true
            if (context.tokenTransformer(context, token.substringBefore("=")) in longNames) return true
            if (context.tokenTransformer(context, token.take(2)) in optionsByName) return false
            return true
        }

        fun consumeParse(result: OptParseResult) {
            positionalArgs += result.unknown
            invocations += result.known
            i += result.consumed
        }

        loop@ while (i <= tokens.lastIndex) {
            val tok = tokens[i]
            val normTok = context.tokenTransformer(context, tok)
            val prefix = splitOptionPrefix(tok).first
            when {
                canParseOptions && tok == "--" -> {
                    i += 1
                    canParseOptions = false
                }

                canParseOptions && (
                        prefix.length > 1 && prefix in prefixes
                                || normTok in longNames
                                || isLongOptionWithEquals(prefix, tok)
                        ) -> {
                    consumeParse(
                        parseLongOpt(
                            command.treatUnknownOptionsAsArgs,
                            context,
                            tokens,
                            tok,
                            i,
                            optionsByName
                        )
                    )
                }

                canParseOptions && tok.length >= 2 && prefix.isNotEmpty() && prefix in prefixes -> {
                    consumeParse(
                        parseShortOpt(
                            command.treatUnknownOptionsAsArgs,
                            context,
                            tokens,
                            tok,
                            i,
                            optionsByName
                        )
                    )
                }

                i >= minAliasI && tok in aliases -> {
                    tokens = aliases.getValue(tok) + tokens.slice(i + 1..tokens.lastIndex)
                    i = 0
                    minAliasI = aliases.getValue(tok).size
                }

                normTok in subcommands -> {
                    subcommand = subcommands.getValue(normTok)
                    break@loop
                }

                else -> {
                    if (!context.allowInterspersedArgs) canParseOptions = false
                    positionalArgs += tokens[i] // arguments aren't transformed
                    i += 1
                }
            }
        }

        val invocationsByOption = invocations.groupBy({ it.opt }, { it.inv })
        val invocationsByGroup = invocations.groupBy { (it.opt as? GroupableOption)?.parameterGroup }
        val invocationsByOptionByGroup = invocationsByGroup
            .mapValues { (_, invs) ->
                invs.groupBy({ it.opt }, { it.inv })
                    .filterKeys { !it.isEager }
            }

        try {
            // Finalize and validate everything as long as we aren't resuming a parse for multiple subcommands
            if (canRun) {
                // Finalize eager options
                invocationsByOption.forEach { (o, inv) -> if (o.isEager) o.finalize(context, inv) }

                // Finalize arguments before options, so that options can reference them
                val (excess, parsedArgs) = parseArguments(positionalArgs, arguments)
                val retries = finalizeArguments(parsedArgs, context)
                i = handleExcessArguments(
                    excess,
                    hasMultipleSubAncestor,
                    i,
                    tokens,
                    subcommands,
                    positionalArgs,
                    context
                )

                // Finalize un-grouped options
                finalizeOptions(
                    context,
                    command._options.filter { !it.isEager && (it as? GroupableOption)?.parameterGroup == null },
                    invocationsByOptionByGroup[null] ?: emptyMap()
                )

                // Finalize groups after ungrouped options so that the groups can use their values
                invocationsByOptionByGroup.forEach { (group, invocations) -> group?.finalize(context, invocations) }

                // Finalize groups with no invocations
                command._groups.forEach { if (it !in invocationsByGroup) it.finalize(context, emptyMap()) }

                // Retry any failed args now that they can reference option values
                retries.forEach { (arg, v) -> arg.finalize(context, v) }

                // Now that all parameters have been finalized, we can validate everything
                command._options.forEach { o ->
                    if ((o as? GroupableOption)?.parameterGroup == null) o.postValidate(context)
                }
                command._groups.forEach { it.postValidate(context) }
                command._arguments.forEach { it.postValidate(context) }

                if (subcommand == null && subcommands.isNotEmpty() && !command.invokeWithoutSubcommand) {
                    throw PrintHelpMessage(command, error = true)
                }

                command.currentContext.invokedSubcommand = subcommand

                command.run()
            } else if (subcommand == null && positionalArgs.isNotEmpty()) {
                // If we're resuming a parse with multiple subcommands, there can't be any args after the last
                // subcommand is parsed
                throwExcessArgsError(positionalArgs, positionalArgs.size, context)
            }
        } catch (e: UsageError) {
            // Augment usage errors with the current context if they don't have one
            if (e.context == null) e.context = context
            throw e
        }

        if (subcommand != null) {
            val (nextTokens, nextArgI) = parse(tokens, subcommand.currentContext, i + 1, true)
            if (command.allowMultipleSubcommands && nextTokens.size - nextArgI > 0) {
                parse(nextTokens, context, nextArgI, false)
            }
            return nextTokens to nextArgI
        }

        return tokens to i
    }

    private fun handleExcessArguments(
        excess: Int,
        hasMultipleSubAncestor: Boolean,
        i: Int,
        tokens: List<String>,
        subcommands: Map<String, CliktCommand>,
        positionalArgs: ArrayList<String>,
        context: Context,
    ): Int {
        if (excess > 0) {
            if (hasMultipleSubAncestor) {
                return tokens.size - excess
            } else if (excess == 1 && subcommands.isNotEmpty()) {
                val actual = positionalArgs.last()
                throw NoSuchSubcommand(actual, context.correctionSuggestor(actual, subcommands.keys.toList()))
            } else {
                throwExcessArgsError(positionalArgs, excess, context)
            }
        }
        return i
    }

    private fun parseLongOpt(
        ignoreUnknown: Boolean,
        context: Context,
        tokens: List<String>,
        tok: String,
        index: Int,
        optionsByName: Map<String, Option>,
    ): OptParseResult {
        val equalsIndex = tok.indexOf('=')
        var (name, value) = if (equalsIndex >= 0) {
            tok.substring(0, equalsIndex) to tok.substring(equalsIndex + 1)
        } else {
            tok to null
        }
        name = context.tokenTransformer(context, name)
        val option = optionsByName[name] ?: if (ignoreUnknown) {
            return OptParseResult(1, listOf(tok), emptyList())
        } else {
            throw NoSuchOption(
                givenName = name,
                possibilities = context.correctionSuggestor(
                    name,
                    optionsByName.filterNot { it.value.hidden }.keys.toList()
                )
            )
        }

        val result = option.parser.parseLongOpt(option, name, tokens, index, value)
        return OptParseResult(result.consumedCount, emptyList(), listOf(OptInvocation(option, result.invocation)))
    }

    private fun parseShortOpt(
        ignoreUnknown: Boolean,
        context: Context,
        tokens: List<String>,
        tok: String,
        index: Int,
        optionsByName: Map<String, Option>,
    ): OptParseResult {
        val prefix = tok[0].toString()
        val invocations = mutableListOf<OptInvocation>()
        for ((i, opt) in tok.withIndex()) {
            if (i == 0) continue // skip the dash

            val name = context.tokenTransformer(context, prefix + opt)
            val option = optionsByName[name] ?: if (ignoreUnknown && tok.length == 2) {
                return OptParseResult(1, listOf(tok), emptyList())
            } else {
                val possibilities = when {
                    prefix == "-" && "-$tok" in optionsByName -> listOf("-$tok")
                    else -> emptyList()
                }
                throw NoSuchOption(name, possibilities)
            }
            val result = option.parser.parseShortOpt(option, name, tokens, index, i)
            invocations += OptInvocation(option, result.invocation)
            if (result.consumedCount > 0) return OptParseResult(result.consumedCount, emptyList(), invocations)
        }
        throw IllegalStateException("Error parsing short option ${tokens[index]}: no parser consumed value.")
    }

    private fun parseArguments(
        positionalArgs: List<String>,
        arguments: List<Argument>,
    ): Pair<Int, Map<Argument, List<String>>> {
        val out = linkedMapOf<Argument, List<String>>().withDefault { listOf() }
        // The number of fixed size arguments that occur after an unlimited size argument. This
        // includes optional single value args, so it might be bigger than the number of provided
        // values.
        val endSize = arguments.asReversed()
            .takeWhile { it.nvalues > 0 }
            .sumOf { it.nvalues }

        var i = 0
        for (argument in arguments) {
            val remaining = positionalArgs.size - i
            val consumed = when {
                argument.nvalues <= 0 -> maxOf(if (argument.required) 1 else 0, remaining - endSize)
                argument.nvalues > 0 && !argument.required && remaining == 0 -> 0
                else -> argument.nvalues
            }
            if (consumed > remaining) {
                if (remaining == 0) throw MissingArgument(argument)
                else throw IncorrectArgumentValueCount(argument)
            }
            out[argument] = out.getValue(argument) + positionalArgs.subList(i, i + consumed)
            i += consumed
        }

        val excess = positionalArgs.size - i
        return excess to out
    }

    private fun finalizeArguments(
        parsedArgs: Map<Argument, List<String>>,
        context: Context,
    ): Map<Argument, List<String>> {
        val retries = mutableMapOf<Argument, List<String>>()
        for ((it, v) in parsedArgs) {
            try {
                it.finalize(context, v)
            } catch (e: IllegalStateException) {
                retries[it] = v
            }
        }
        return retries
    }

    private fun throwExcessArgsError(positionalArgs: List<String>, excess: Int, context: Context): Nothing {
        val actual =
            positionalArgs.takeLast(excess).joinToString(" ", limit = 3, prefix = "(", postfix = ")")
        val message =
            if (excess == 1) context.localization.extraArgumentOne(actual) else context.localization.extraArgumentMany(
                actual,
                excess
            )
        throw UsageError(message)
    }
}

// It would be better to have eagerness be a property on Option rather than needing these custom subclasses.
private val Option.isEager get() = this is EagerOption