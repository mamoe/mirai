package net.mamoe.mirai.clikt.parameters.options

import net.mamoe.mirai.clikt.core.*
import net.mamoe.mirai.clikt.parsers.FlagOptionParser
import net.mamoe.mirai.clikt.parsers.OptionParser

/**
 * An [Option] with no values that is [finalize]d before other types of options.
 *
 * @param callback This callback is called when the option is encountered on the command line. If
 *   you want to print a message and halt execution normally, you should throw a [PrintMessage]
 *   exception. If you want to exit normally without printing a message, you should throw
 *   [`Abort(error=false)`][Abort]. The callback is passed the current execution context as a
 *   parameter.
 */
public class EagerOption(
    override val names: Set<String>,
    override val nvalues: Int,
    override val optionHelp: String,
    override val hidden: Boolean,
    override val helpTags: Map<String, String>,
    override val groupName: String?,
    private val callback: OptionTransformContext.() -> Unit,
) : StaticallyGroupedOption {
    public constructor(
        vararg names: String, nvalues: Int = 0, help: String = "", hidden: Boolean = false,
        helpTags: Map<String, String> = emptyMap(), groupName: String? = null,
        callback: OptionTransformContext.() -> Unit,
    )
            : this(names.toSet(), nvalues, help, hidden, helpTags, groupName, callback)

    override val secondaryNames: Set<String> get() = emptySet()
    override val parser: OptionParser = FlagOptionParser
    override fun metavar(context: Context): String? = null
    override val valueSourceKey: String? get() = null
    override fun postValidate(context: Context) {}
    override fun finalize(context: Context, invocations: List<OptionParser.Invocation>) {
        this.callback(OptionTransformContext(this, context))
    }
}

internal fun helpOption(names: Set<String>, message: String): EagerOption {
    return EagerOption(names, 0, message, false, emptyMap(), null) { throw PrintHelpMessage(context.command) }
}

/**
 * Add an eager option to this command that, when invoked, runs [action].
 *
 * @param name The names that can be used to invoke this option. They must start with a punctuation character.
 * @param help The description of this option, usually a single line.
 * @param hidden Hide this option from help outputs.
 * @param helpTags Extra information about this option to pass to the help formatter
 * @param groupName All options with that share a group name will be grouped together in help output.
 * @param action This callback is called when the option is encountered on the command line. If
 *   you want to print a message and halt execution normally, you should throw a [PrintMessage]
 *   exception. If you want to exit normally without printing a message, you should throw
 *   [`Abort(error=false)`][Abort]. The callback is passed the current execution context as a
 *   parameter.
 */
public fun <T : CliktCommand> T.eagerOption(
    name: String,
    vararg additionalNames: String,
    help: String = "",
    hidden: Boolean = false,
    helpTags: Map<String, String> = emptyMap(),
    groupName: String? = null,
    action: OptionTransformContext.() -> Unit,
): T = eagerOption(listOf(name) + additionalNames, help, hidden, helpTags, groupName, action)

/**
 * Add an eager option to this command that, when invoked, runs [action].
 *
 * @param names The names that can be used to invoke this option. They must start with a punctuation character.
 * @param help The description of this option, usually a single line.
 * @param hidden Hide this option from help outputs.
 * @param helpTags Extra information about this option to pass to the help formatter
 * @param groupName All options with that share a group name will be grouped together in help output.
 * @param action This callback is called when the option is encountered on the command line. If
 *   you want to print a message and halt execution normally, you should throw a [PrintMessage]
 *   exception. If you want to exit normally without printing a message, you should throw
 *   [`Abort(error=false)`][Abort]. The callback is passed the current execution context as a
 *   parameter.
 */
public fun <T : CliktCommand> T.eagerOption(
    names: Collection<String>,
    help: String = "",
    hidden: Boolean = false,
    helpTags: Map<String, String> = emptyMap(),
    groupName: String? = null,
    action: OptionTransformContext.() -> Unit,
): T = apply { registerOption(EagerOption(names.toSet(), 0, help, hidden, helpTags, groupName, action)) }

/** Add an eager option to this command that, when invoked, prints a version message and exits. */
public inline fun <T : CliktCommand> T.versionOption(
    version: String,
    help: String = "Show the version and exit",
    names: Set<String> = setOf("--version"),
    crossinline message: (String) -> String = { "$commandName version $it" },
): T = eagerOption(names, help) { throw PrintMessage(message(version)) }