package net.mamoe.mirai.clikt.core

import net.mamoe.mirai.clikt.output.HelpFormatter.ParameterHelp
import net.mamoe.mirai.clikt.parameters.arguments.Argument
import net.mamoe.mirai.clikt.parameters.arguments.ProcessedArgument
import net.mamoe.mirai.clikt.parameters.arguments.argument
import net.mamoe.mirai.clikt.parameters.groups.OptionGroup
import net.mamoe.mirai.clikt.parameters.groups.ParameterGroup
import net.mamoe.mirai.clikt.parameters.options.*
import net.mamoe.mirai.clikt.parsers.Parser

/**
 * The [CliktCommand] is the core of command line interfaces in Clikt.
 *
 * Command line interfaces created by creating a subclass of [CliktCommand] with properties defined with
 * [option] and [argument]. You can then parse `argv` by calling [main], which will take care of printing
 * errors and help to the user. If you want to handle output yourself, you can use [parse] instead.
 *
 * Once the command line has been parsed and all of the parameters are populated, [run] is called.
 *
 * @param help The help for this command. The first line is used in the usage string, and the entire string is
 *   used in the help output. Paragraphs are automatically re-wrapped to the terminal width.
 * @param epilog Text to display at the end of the full help output. It is automatically re-wrapped to the
 *   terminal width.
 * @param name The name of the program to use in the help output. If not given, it is inferred from the class
 *   name.
 * @param invokeWithoutSubcommand Used when this command has subcommands, and this command is called
 *   without a subcommand. If true, [run] will be called. By default, a [PrintHelpMessage] is thrown instead.
 * @param printHelpOnEmptyArgs If this command is called with no values on the command line, print a
 *   help message (by throwing [PrintHelpMessage]) if this is true, otherwise run normally.
 * @param helpTags Extra information about this option to pass to the help formatter.
 * @param allowMultipleSubcommands If true, allow multiple of this command's subcommands to be
 *   called sequentially. This will disable `allowInterspersedArgs` on the context of this command an
 *   its descendants. This functionality is experimental, and may change in a future release.
 * @param treatUnknownOptionsAsArgs If true, any options on the command line whose names aren't
 * valid will be parsed as an argument rather than reporting an error. You'll need to define an
 * `argument().multiple()` to collect these options, or an error will still be reported. Unknown
 * short option flags grouped with other flags on the command line will always be reported as
 * errors.
 */
@Suppress("PropertyName")
@ParameterHolderDsl
public abstract class CliktCommand(
    help: String = "",
    epilog: String = "",
    name: String? = null,
    public val invokeWithoutSubcommand: Boolean = false,
    public val printHelpOnEmptyArgs: Boolean = false,
    public val helpTags: Map<String, String> = emptyMap(),
    internal val allowMultipleSubcommands: Boolean = false,
    internal val treatUnknownOptionsAsArgs: Boolean = false,
) : ParameterHolder {
    /**
     * The name of this command, used in help output.
     *
     * You can set this by passing `name` to the [CliktCommand] constructor.
     */
    public val commandName: String = name ?: inferCommandName()

    /**
     * The help text for this command.
     *
     * You can set this by passing `help` to the [CliktCommand] constructor, or by overriding this
     * property.
     */
    public open val commandHelp: String = help

    /**
     * Help text to display at the end of the help output, after any parameters.
     *
     * You can set this by passing `epilog` to the [CliktCommand] constructor, or by overriding this
     * property.
     */
    public open val commandHelpEpilog: String = epilog

    internal var _subcommands: List<CliktCommand> = emptyList()
    internal val _options: MutableList<Option> = mutableListOf()
    internal val _arguments: MutableList<Argument> = mutableListOf()
    internal val _groups: MutableList<ParameterGroup> = mutableListOf()
    internal var _contextConfig: Context.Builder.() -> Unit = {}
    private var _context: Context? = null
    private val _messages = mutableListOf<String>()

    private fun registeredOptionNames() = _options.flatMapTo(mutableSetOf()) { it.names }

    private fun createContext(parent: Context?, ancestors: List<CliktCommand>) {
        _context = Context.build(this, parent, _contextConfig)

        if (allowMultipleSubcommands) {
            require(currentContext.ancestors().drop(1).none { it.command.allowMultipleSubcommands }) {
                "Commands with allowMultipleSubcommands=true cannot be nested in " +
                        "commands that also have allowMultipleSubcommands=true"
            }
        }

        if (currentContext.helpOptionNames.isNotEmpty()) {
            val names = currentContext.helpOptionNames - registeredOptionNames()
            if (names.isNotEmpty()) _options += helpOption(names, currentContext.localization.helpOptionMessage())
        }

        for (command in _subcommands) {
            val a = (ancestors + parent?.command).filterNotNull()
            check(command !in a) { "Command ${command.commandName} already registered" }
            command.createContext(currentContext, a)
        }
    }

    private fun allHelpParams(): List<ParameterHelp> {
        return _options.mapNotNull { it.parameterHelp(currentContext) } +
                _arguments.mapNotNull { it.parameterHelp(currentContext) } +
                _groups.mapNotNull { it.parameterHelp(currentContext) } +
                _subcommands.map { ParameterHelp.Subcommand(it.commandName, it.shortHelp(), it.helpTags) }
    }

    private fun getCommandNameWithParents(): String {
        if (_context == null) createContext(null, emptyList())
        return currentContext.commandNameWithParents().joinToString(" ")
    }

    /**
     * This command's context.
     *
     * @throws NullPointerException if accessed before [parse] or [main] are called.
     */
    public val currentContext: Context
        get() {
            checkNotNull(_context) { "Context accessed before parse has been called." }
            return _context!!
        }

    /** All messages issued during parsing. */
    public val messages: List<String> get() = _messages

    /** Add a message to be printed after parsing */
    public fun issueMessage(message: String) {
        _messages += message
    }

    /** The help displayed in the commands list when this command is used as a subcommand. */
    protected fun shortHelp(): String = Regex("""\s*(?:```)?\s*(.+)""").find(commandHelp)?.groups?.get(1)?.value ?: ""

    /** The names of all direct children of this command */
    public fun registeredSubcommandNames(): List<String> = _subcommands.map { it.commandName }

    /**
     * Get a read-only list of commands registered as [subcommands] of this command.
     */
    public fun registeredSubcommands(): List<CliktCommand> = _subcommands.toList()

    /**
     * Get a read-only list of options registered in this command (e.g. via [registerOption] or an [option] delegate)
     */
    public fun registeredOptions(): List<Option> = _options.toList()

    /**
     * Get a read-only list of arguments registered in this command (e.g. via [registerArgument] or an [argument] delegate)
     */
    public fun registeredArguments(): List<Argument> = _arguments.toList()

    /**
     * Get a read-only list of groups registered in this command (e.g. via [registerOptionGroup] or an [OptionGroup] delegate)
     */
    public fun registeredParameterGroups(): List<ParameterGroup> = _groups.toList()

    /**
     * Register an option with this command.
     *
     * This is called automatically for the built in options, but you need to call this if you want to add a
     * custom option.
     */
    public fun registerOption(option: Option) {
        val names = registeredOptionNames()
        for (name in option.names) {
            require(name !in names) { "Duplicate option name $name" }
        }
        _options += option
    }

    override fun registerOption(option: GroupableOption): Unit = registerOption(option as Option)

    /**
     * Register an argument with this command.
     *
     * This is called automatically for the built in arguments, but you need to call this if you want to add a
     * custom argument.
     */
    public fun registerArgument(argument: Argument) {
        require(argument.nvalues > 0 || _arguments.none { it.nvalues < 0 }) {
            "Cannot declare multiple arguments with variable numbers of values"
        }
        _arguments += argument
    }

    /**
     * Register a group with this command.
     *
     * This is called automatically for built in groups, but you need to call this if you want to
     * add a custom group.
     */
    public fun registerOptionGroup(group: ParameterGroup) {
        require(group !in _groups) { "Cannot register the same group twice" }
        require(group.groupName == null || _groups.none { it.groupName == group.groupName }) { "Cannot register the same group name twice" }
        _groups += group
    }

    /** Return the usage string for this command. */
    public open fun getFormattedUsage(): String {
        val programName = getCommandNameWithParents()
        return currentContext.helpFormatter.formatUsage(allHelpParams(), programName = programName)
    }

    /** Return the full help string for this command. */
    public open fun getFormattedHelp(): String {
        val programName = getCommandNameWithParents()
        return currentContext.helpFormatter.formatHelp(
            commandHelp, commandHelpEpilog,
            allHelpParams(), programName = programName
        )
    }

    /**
     * A list of command aliases.
     *
     * If the user enters a command that matches a key in this map, the command is replaced with the
     * corresponding value in the map. The aliases aren't recursive, so aliases won't be looked up again while
     * tokens from an existing alias are being parsed.
     */
    public open fun aliases(): Map<String, List<String>> = emptyMap()

    /**
     * Parse the command line and throw an exception if parsing fails.
     *
     * You should use [main] instead unless you want to handle output yourself.
     */
    public suspend fun parse(argv: List<String>, parentContext: Context? = null) {
        createContext(parentContext, emptyList())
        Parser.parse(argv, this.currentContext)
    }

    public suspend fun parse(argv: Array<String>, parentContext: Context? = null) {
        parse(argv.asList(), parentContext)
    }

    /**
     * Parse the command line and print helpful output if any errors occur.
     *
     * This function calls [parse] and catches and [CliktError]s that are thrown. Other errors are allowed to
     * pass through.
     */
    public suspend fun main(argv: List<String>): CommandResult {
        return try {
            parse(argv)
            CommandResult.Success
        } catch (e: PrintHelpMessage) {
            CommandResult.Error(e.message, e.command.getFormattedHelp(), e)
        } catch (e: PrintMessage) {
            CommandResult.Error(e.message, e.message, e)
        } catch (e: UsageError) {
            CommandResult.Error(e.message, e.helpMessage(), e)
        } catch (e: CliktError) {
            CommandResult.Error(e.message, e.message, e)
        } catch (e: Abort) {
            CommandResult.Error(e.message, currentContext.localization.aborted(), e)
        }
    }

    public suspend fun main(argv: Array<out String>): CommandResult = main(argv.asList())

    public suspend fun main(rawString: String): CommandResult = main(rawString.parseToArgs())

    /**
     * Perform actions after parsing is complete and this command is invoked.
     *
     * This is called after command line parsing is complete. If this command is a subcommand, this will only
     * be called if the subcommand is invoked.
     *
     * If one of this command's subcommands is invoked, this is called before the subcommand's arguments are
     * parsed.
     */
    public abstract suspend fun run()

    override fun toString(): String = buildString {
        append("<${this@CliktCommand.classSimpleName()} name=$commandName")

        if (_options.isNotEmpty() || _arguments.isNotEmpty() || _subcommands.isNotEmpty()) {
            append(" ")
        }

        if (_options.isNotEmpty()) {
            append("options=[")
            for ((i, option) in _options.withIndex()) {
                if (i > 0) append(" ")
                append(option.longestName())
                if (_context != null && option is OptionDelegate<*>) {
                    try {
                        val value = option.value
                        append("=").append(value)
                    } catch (_: IllegalStateException) {
                    }
                }
            }
            append("]")
        }


        if (_arguments.isNotEmpty()) {
            append(" arguments=[")
            for ((i, argument) in _arguments.withIndex()) {
                if (i > 0) append(" ")
                append(argument.name)
                if (_context != null && argument is ProcessedArgument<*, *>) {
                    try {
                        val value = argument.value
                        append("=").append(value)
                    } catch (_: IllegalStateException) {
                    }
                }
            }
            append("]")
        }

        if (_subcommands.isNotEmpty()) {
            _subcommands.joinTo(this, " ", prefix = " subcommands=[", postfix = "]")
        }

        append(">")
    }
}

/** Add the given commands as a subcommand of this command. */
public fun <T : CliktCommand> T.subcommands(commands: Iterable<CliktCommand>): T = apply {
    _subcommands = _subcommands + commands
}

/** Add the given commands as a subcommand of this command. */
public fun <T : CliktCommand> T.subcommands(vararg commands: CliktCommand): T = apply {
    _subcommands = _subcommands + commands
}

/**
 * Configure this command's [Context].
 *
 * Context property values are normally inherited from the parent context, but you can override any of them
 * here.
 */
public fun <T : CliktCommand> T.context(block: Context.Builder.() -> Unit): T = apply {
    // save the old config to allow multiple context calls
    val c = _contextConfig
    _contextConfig = {
        c()
        block()
    }
}

private fun Any.classSimpleName(): String = this::class.simpleName.orEmpty().split("$").last()

private fun CliktCommand.inferCommandName(): String {
    val name = classSimpleName()
    if (name == "Command") return "command"
    return name.removeSuffix("Command").replace(Regex("([a-z])([A-Z])")) {
        "${it.groupValues[1]}-${it.groupValues[2]}"
    }.lowercase()
}