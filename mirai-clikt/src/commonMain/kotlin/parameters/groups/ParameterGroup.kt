package net.mamoe.mirai.clikt.parameters.groups

import net.mamoe.mirai.clikt.core.CliktCommand
import net.mamoe.mirai.clikt.core.Context
import net.mamoe.mirai.clikt.core.GroupableOption
import net.mamoe.mirai.clikt.core.ParameterHolder
import net.mamoe.mirai.clikt.internal.finalizeOptions
import net.mamoe.mirai.clikt.output.HelpFormatter
import net.mamoe.mirai.clikt.parameters.options.Option
import net.mamoe.mirai.clikt.parsers.OptionParser
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

public interface ParameterGroup {
    /**
     * The name of the group, or null if parameters in the group should not be separated from other
     * parameters in the help output.
     */
    public val groupName: String?

    /**
     * A help message to display for this group.
     *
     * If [groupName] is null, the help formatter will ignore this value.
     */
    public val groupHelp: String?

    public fun parameterHelp(context: Context): HelpFormatter.ParameterHelp.Group? {
        val n = groupName
        val h = groupHelp
        return if (n == null || h == null) null else HelpFormatter.ParameterHelp.Group(n, h)
    }

    /**
     * Called after this command's argv is parsed and all options are validated to validate the group constraints.
     *
     * @param context The context for this parse
     * @param invocationsByOption The invocations of options in this group.
     */
    public fun finalize(context: Context, invocationsByOption: Map<Option, List<OptionParser.Invocation>>)

    /**
     * Called after all of a command's parameters have been [finalize]d to perform validation of the final values.
     */
    public fun postValidate(context: Context)
}

public interface ParameterGroupDelegate<out T> : ParameterGroup, ReadOnlyProperty<CliktCommand, T> {
    /** Implementations must call [CliktCommand.registerOptionGroup] */
    public operator fun provideDelegate(thisRef: CliktCommand, prop: KProperty<*>): ReadOnlyProperty<CliktCommand, T>
}

/**
 * A group of options that can be shown together in help output, or restricted to be [cooccurring].
 *
 * Declare a subclass with option delegate properties, then use an instance of your subclass is a
 * delegate property in your command with [provideDelegate].
 *
 * ### Example:
 *
 * ```
 * class UserOptions : OptionGroup(name = "User Options", help = "Options controlling the user") {
 *   val name by option()
 *   val age by option().int()
 * }
 *
 * class Tool : CliktCommand() {
 *   val userOptions by UserOptions()
 * }
 * ```
 */
public open class OptionGroup(
    name: String? = null,
    help: String? = null,
) : ParameterGroup, ParameterHolder {
    internal val options: MutableList<GroupableOption> = mutableListOf()
    override val groupName: String? = name
    override val groupHelp: String? = help

    override fun registerOption(option: GroupableOption) {
        option.parameterGroup = this
        options += option
    }

    override fun finalize(context: Context, invocationsByOption: Map<Option, List<OptionParser.Invocation>>) {
        finalizeOptions(context, options, invocationsByOption)
    }

    override fun postValidate(context: Context): Unit = options.forEach { it.postValidate(context) }
}

public operator fun <T : OptionGroup> T.provideDelegate(
    thisRef: CliktCommand,
    prop: KProperty<*>,
): ReadOnlyProperty<CliktCommand, T> {
    thisRef.registerOptionGroup(this)
    options.forEach { thisRef.registerOption(it) }
    return ReadOnlyProperty { _, _ -> this@provideDelegate }
}