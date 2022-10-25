package net.mamoe.mirai.clikt.parameters.groups

import net.mamoe.mirai.clikt.core.BadParameterValue
import net.mamoe.mirai.clikt.core.CliktCommand
import net.mamoe.mirai.clikt.core.Context
import net.mamoe.mirai.clikt.core.MissingOption
import net.mamoe.mirai.clikt.parameters.internal.NullableLateinit
import net.mamoe.mirai.clikt.parameters.options.Option
import net.mamoe.mirai.clikt.parameters.options.OptionDelegate
import net.mamoe.mirai.clikt.parameters.options.RawOption
import net.mamoe.mirai.clikt.parameters.options.switch
import net.mamoe.mirai.clikt.parameters.types.choice
import net.mamoe.mirai.clikt.parsers.OptionParser
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

public class ChoiceGroup<GroupT : OptionGroup, OutT> internal constructor(
    internal val option: OptionDelegate<String?>,
    internal val groups: Map<String, GroupT>,
    internal val transform: (GroupT?) -> OutT,
) : ParameterGroupDelegate<OutT> {
    override val groupName: String? = null
    override val groupHelp: String? = null
    private var value: OutT by NullableLateinit("Cannot read from option delegate before parsing command line")
    private var chosenGroup: OptionGroup? = null

    override fun provideDelegate(thisRef: CliktCommand, prop: KProperty<*>): ReadOnlyProperty<CliktCommand, OutT> {
        option.provideDelegate(thisRef, prop) // infer the option name and register it
        thisRef.registerOptionGroup(this)
        for ((_, group) in groups) {
            for (option in group.options) {
                option.parameterGroup = this
                option.groupName = group.groupName
                thisRef.registerOption(option)
            }
        }
        return this
    }

    override fun getValue(thisRef: CliktCommand, property: KProperty<*>): OutT = value

    override fun finalize(context: Context, invocationsByOption: Map<Option, List<OptionParser.Invocation>>) {
        val key = option.value
        if (key == null) {
            value = transform(null)
            // Finalize the group so that default groups have their options finalized
            (value as? OptionGroup)?.let { g ->
                g.finalize(context, invocationsByOption.filterKeys { it in g.options })
                chosenGroup = g
            }
            return
        }

        val group = groups[key] ?: throw BadParameterValue(
            context.localization.invalidGroupChoice(key, groups.keys.toList()),
            option,
            context
        )
        group.finalize(context, invocationsByOption.filterKeys { it in group.options })
        chosenGroup = group
        value = transform(group)
    }

    override fun postValidate(context: Context) {
        chosenGroup?.options?.forEach { it.postValidate(context) }
    }
}

/**
 * Convert the option to an option group based on a fixed set of values.
 *
 * ### Example:
 *
 * ```
 * option().groupChoice(mapOf("foo" to FooOptionGroup(), "bar" to BarOptionGroup()))
 * ```
 *
 * @see net.mamoe.mirai.clikt.parameters.types.choice
 */
public fun <T : OptionGroup> RawOption.groupChoice(choices: Map<String, T>): ChoiceGroup<T, T?> {
    return ChoiceGroup(choice(choices.mapValues { it.key }), choices) { it }
}

/**
 * Convert the option to an option group based on a fixed set of values.
 *
 * ### Example:
 *
 * ```
 * option().groupChoice("foo" to FooOptionGroup(), "bar" to BarOptionGroup())
 * ```
 *
 * @see net.mamoe.mirai.clikt.parameters.types.choice
 */
public fun <T : OptionGroup> RawOption.groupChoice(vararg choices: Pair<String, T>): ChoiceGroup<T, T?> {
    return groupChoice(choices.toMap())
}

/**
 * If a [groupChoice] or [groupSwitch] option is not called on the command line, throw a
 * [MissingOption] exception.
 *
 * ### Example:
 *
 * ```
 * option().groupChoice("foo" to FooOptionGroup(), "bar" to BarOptionGroup()).required()
 * ```
 */
public fun <T : OptionGroup> ChoiceGroup<T, T?>.required(): ChoiceGroup<T, T> {
    return ChoiceGroup(option, groups) { it ?: throw MissingOption(option) }
}

/**
 * Convert the option into a set of flags that each map to an option group.
 *
 * ### Example:
 *
 * ```
 * option().groupSwitch(mapOf("--foo" to FooOptionGroup(), "--bar" to BarOptionGroup()))
 * ```
 */
public fun <T : OptionGroup> RawOption.groupSwitch(choices: Map<String, T>): ChoiceGroup<T, T?> {
    return ChoiceGroup(switch(choices.mapValues { it.key }), choices) { it }
}

/**
 * Convert the option into a set of flags that each map to an option group.
 *
 * ### Example:
 *
 * ```
 * option().groupSwitch("--foo" to FooOptionGroup(), "--bar" to BarOptionGroup())
 * ```
 */
public fun <T : OptionGroup> RawOption.groupSwitch(vararg choices: Pair<String, T>): ChoiceGroup<T, T?> {
    return groupSwitch(choices.toMap())
}

/**
 * If a [groupChoice] or [groupSwitch] option is not called on the command line, use the value of
 * the group with a switch or choice [name].
 *
 * ### Example:
 *
 * ```
 * option().groupChoice("foo" to FooOptionGroup(), "bar" to BarOptionGroup()).defaultByName("foo")
 * option().groupSwitch("--foo" to FooOptionGroup(), "--bar" to BarOptionGroup()).defaultByName("--bar")
 * ```
 *
 * @throws IllegalArgumentException if [name] is not one of the option's choice/switch names.
 */
public fun <T : OptionGroup> ChoiceGroup<T, T?>.defaultByName(name: String): ChoiceGroup<T, T> {
    require(name in groups) { "invalid default name $name (must be one of ${groups.keys})" }
    return ChoiceGroup(option, groups) { it ?: groups.getValue(name) }
}