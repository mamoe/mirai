package net.mamoe.mirai.clikt.parameters.options

import net.mamoe.mirai.clikt.core.*
import net.mamoe.mirai.clikt.output.HelpFormatter
import net.mamoe.mirai.clikt.parameters.groups.ParameterGroup
import net.mamoe.mirai.clikt.parameters.internal.NullableLateinit
import net.mamoe.mirai.clikt.parameters.types.valueToInt
import net.mamoe.mirai.clikt.parsers.FlagOptionParser
import net.mamoe.mirai.clikt.parsers.OptionParser
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/** A block that converts a flag value from one type to another */
public typealias FlagConverter<InT, OutT> = OptionTransformContext.(InT) -> OutT

/**
 * An [Option] that has no values.
 *
 * @property envvar The name of the environment variable for this option. Overrides automatic names.
 * @property transformEnvvar Called to transform string values from envvars and value sources into the option type.
 * @property transformAll Called to transform all invocations of this option into the final option type.
 */
// `T` is deliberately not an out parameter.
public class FlagOption<T> internal constructor(
    names: Set<String>,
    override val secondaryNames: Set<String>,
    override val optionHelp: String,
    override val hidden: Boolean,
    override val helpTags: Map<String, String>,
    override val valueSourceKey: String?,
    public val envvar: String?,
    public val transformEnvvar: OptionTransformContext.(String) -> T,
    public val transformAll: CallsTransformer<String, T>,
    public val validator: OptionValidator<T>,
) : OptionDelegate<T> {
    override var parameterGroup: ParameterGroup? = null
    override var groupName: String? = null
    override fun metavar(context: Context): String? = null
    override val nvalues: Int get() = 0
    override val parser: FlagOptionParser = FlagOptionParser
    override var value: T by NullableLateinit("Cannot read from option delegate before parsing command line")
        private set
    override var names: Set<String> = names
        private set

    override operator fun provideDelegate(
        thisRef: ParameterHolder,
        prop: KProperty<*>,
    ): ReadOnlyProperty<ParameterHolder, T> {
        names = inferOptionNames(names, prop.name)
        thisRef.registerOption(this)
        return this
    }

    override fun finalize(context: Context, invocations: List<OptionParser.Invocation>) {
        val transformContext = OptionTransformContext(this, context)
        value = when (val v = getFinalValue(context, invocations)) {
            is FinalValue.Parsed -> transformAll(transformContext, invocations.map { it.name })
            is FinalValue.Sourced -> {
                if (v.values.size != 1 || v.values[0].values.size != 1) {
                    val message = context.localization.invalidFlagValueInFile(longestName() ?: "")
                    throw UsageError(message, this)
                }
                transformEnvvar(transformContext, v.values[0].values[0])
            }

            is FinalValue.Envvar -> transformEnvvar(transformContext, v.value)
        }
    }

    override fun postValidate(context: Context) {
        validator(OptionTransformContext(this, context), value)
    }

    /** Create a new option that is a copy of this one with different transforms. */
    public fun <T> copy(
        transformEnvvar: OptionTransformContext.(String) -> T,
        transformAll: CallsTransformer<String, T>,
        validator: OptionValidator<T>,
        names: Set<String> = this.names,
        secondaryNames: Set<String> = this.secondaryNames,
        help: String = this.optionHelp,
        hidden: Boolean = this.hidden,
        helpTags: Map<String, String> = this.helpTags,
        valueSourceKey: String? = this.valueSourceKey,
        envvar: String? = this.envvar,
    ): FlagOption<T> {
        return FlagOption(
            names = names,
            secondaryNames = secondaryNames,
            optionHelp = help,
            hidden = hidden,
            helpTags = helpTags,
            valueSourceKey = valueSourceKey,
            envvar = envvar,
            transformEnvvar = transformEnvvar,
            transformAll = transformAll,
            validator = validator
        )
    }

    /** Create a new option that is a copy of this one with the same transforms. */
    public fun copy(
        validator: OptionValidator<T> = this.validator,
        names: Set<String> = this.names,
        secondaryNames: Set<String> = this.secondaryNames,
        help: String = this.optionHelp,
        hidden: Boolean = this.hidden,
        helpTags: Map<String, String> = this.helpTags,
        valueSourceKey: String? = this.valueSourceKey,
        envvar: String? = this.envvar,
    ): FlagOption<T> {
        return FlagOption(
            names = names,
            secondaryNames = secondaryNames,
            optionHelp = help,
            hidden = hidden,
            helpTags = helpTags,
            valueSourceKey = valueSourceKey,
            envvar = envvar,
            transformEnvvar = transformEnvvar,
            transformAll = transformAll,
            validator = validator
        )
    }
}

/**
 * Turn an option into a boolean flag.
 *
 * @param secondaryNames additional names for that option that cause the option value to be false. It's good
 *   practice to provide secondary names so that users can disable an option that was previously enabled.
 * @param default the value for this property if the option is not given on the command line.
 * @param defaultForHelp The help text for this option's default value if the help formatter is configured
 *   to show them. By default, an empty string is being used to suppress the "default" help text.
 *
 * ### Example:
 *
 * ```
 * val flag by option(help = "flag option").flag("--no-flag", default = true, defaultForHelp = "enable")
 * // Options:
 * // --flag / --no-flag  flag option (default: enable)
 * ```
 */
public fun RawOption.flag(
    vararg secondaryNames: String,
    default: Boolean = false,
    defaultForHelp: String = "",
): FlagOption<Boolean> {
    val tags = helpTags + mapOf(HelpFormatter.Tags.DEFAULT to defaultForHelp)
    return FlagOption(
        names = names,
        secondaryNames = secondaryNames.toSet(),
        optionHelp = optionHelp,
        hidden = hidden,
        helpTags = tags,
        valueSourceKey = valueSourceKey,
        envvar = envvar,
        transformEnvvar = {
            when (it.lowercase()) {
                "true", "t", "1", "yes", "y", "on" -> true
                "false", "f", "0", "no", "n", "off" -> false
                else -> throw BadParameterValue(context.localization.boolConversionError(it), this)
            }
        },
        transformAll = {
            if (it.isEmpty()) default else it.last() !in secondaryNames
        },
        validator = {}
    )
}

/**
 * Set the help for this option.
 *
 * Although you would normally pass the help string as an argument to [option], this function
 * can be more convenient for long help strings.
 *
 * ### Example:
 *
 * ```
 * val number by option()
 *      .flag()
 *      .help("This option is a flag")
 * ```
 */
public fun <T> FlagOption<T>.help(help: String): FlagOption<T> {
    return copy(help = help)
}

/**
 * Convert the option's value type.
 *
 * The [conversion] is called once with the final value of the option. If any errors are thrown,
 * they are caught and a [BadParameterValue] is thrown with the error message. You can call
 * [fail][OptionTransformContext.fail] to throw a [BadParameterValue] manually.
 *
 * ## Example
 *
 * ```
 * val loud by option().flag().convert { if (it) Volume.Loud else Volume.Soft }
 * ```
 */
public inline fun <InT, OutT> FlagOption<InT>.convert(crossinline conversion: FlagConverter<InT, OutT>): FlagOption<OutT> {
    val envTransform: OptionTransformContext.(String) -> OutT = {
        val orig = transformEnvvar(it)
        try {
            conversion(orig)
        } catch (err: UsageError) {
            throw err
        } catch (err: Exception) {
            fail(err.message ?: "")
        }
    }
    val allTransform: OptionTransformContext.(List<String>) -> OutT = {
        val orig = transformAll(it)
        try {
            conversion(orig)
        } catch (err: UsageError) {
            throw err
        } catch (err: Exception) {
            fail(err.message ?: "")
        }
    }
    return copy(
        transformEnvvar = envTransform,
        transformAll = allTransform,
        validator = {}
    )
}

/**
 * Turn an option into a flag that counts the number of times the option occurs on the command line.
 */
public fun RawOption.counted(): FlagOption<Int> {
    return FlagOption(
        names = names,
        secondaryNames = emptySet(),
        optionHelp = optionHelp,
        hidden = hidden,
        helpTags = helpTags,
        valueSourceKey = valueSourceKey,
        envvar = envvar,
        transformEnvvar = { valueToInt(context, it) },
        transformAll = { it.size },
        validator = {}
    )
}

/**
 * Turn an option into a set of flags that each map to a value.
 *
 * ### Example:
 *
 * ```
 * option().switch(mapOf("--foo" to Foo(), "--bar" to Bar()))
 * ```
 */
public fun <T : Any> RawOption.switch(choices: Map<String, T>): FlagOption<T?> {
    require(choices.isNotEmpty()) { "Must specify at least one choice" }
    return FlagOption(
        names = choices.keys,
        secondaryNames = emptySet(),
        optionHelp = optionHelp,
        hidden = hidden,
        helpTags = helpTags,
        valueSourceKey = null,
        envvar = null,
        transformEnvvar = {
            throw UsageError(context.localization.switchOptionEnvvar(), this)
        },
        transformAll = { names -> names.map { choices.getValue(it) }.lastOrNull() },
        validator = {}
    )
}

/**
 * Turn an option into a set of flags that each map to a value.
 *
 * ### Example:
 *
 * ```
 * option().switch("--foo" to Foo(), "--bar" to Bar())
 * ```
 */
public fun <T : Any> RawOption.switch(vararg choices: Pair<String, T>): FlagOption<T?> = switch(mapOf(*choices))

/**
 * Set a default [value] for an option.
 *
 * @param defaultForHelp The help text for this option's default value if the help formatter is configured
 *   to show them. Use an empty string to suppress the "default" help text.
 */
public fun <T : Any> FlagOption<T?>.default(
    value: T,
    defaultForHelp: String = value.toString(),
): FlagOption<T> {
    return copy(
        transformEnvvar = { transformEnvvar(it) ?: value },
        transformAll = { transformAll(it) ?: value },
        validator = validator,
        helpTags = helpTags + mapOf(HelpFormatter.Tags.DEFAULT to defaultForHelp)
    )
}

/**
 * Set a default [value] for an option from a lazy builder which is only called if the default value is used.
 *
 * @param defaultForHelp The help text for this option's default value if the help formatter is configured
 *   to show them. By default, the default value is not shown in help.
 */
public inline fun <T : Any> FlagOption<T?>.defaultLazy(
    defaultForHelp: String = "",
    crossinline value: () -> T,
): FlagOption<T> {
    return copy(
        transformEnvvar = { transformEnvvar(it) ?: value() },
        transformAll = { transformAll(it) ?: value() },
        validator = validator,
        helpTags = helpTags + mapOf(HelpFormatter.Tags.DEFAULT to defaultForHelp)
    )
}

/**
 * If the option is not called on the command line (and is not set in an envvar), throw a [MissingOption].
 *
 * This must be applied after all other transforms.
 *
 * ### Example:
 *
 * ```
 * option().switch("--foo" to Foo(), "--bar" to Bar()).required()
 * ```
 */
public fun <T : Any> FlagOption<T?>.required(): FlagOption<T> {
    return copy(
        transformEnvvar = { transformEnvvar(it) ?: throw MissingOption(option) },
        transformAll = { transformAll(it) ?: throw MissingOption(option) },
        validator = validator,
        helpTags = helpTags + mapOf(HelpFormatter.Tags.REQUIRED to "")
    )
}

/**
 * Check the final option value and raise an error if it's not valid.
 *
 * The [validator] is called with the final option type (the output of [transformAll]), and should call `fail`
 * if the value is not valid. It is not called if the delegate value is null.
 */
public fun <T : Any> FlagOption<T>.validate(validator: OptionValidator<T>): OptionDelegate<T> = copy(validator)

/**
 * Mark this option as deprecated in the help output.
 *
 * By default, a tag is added to the help message and a warning is printed if the option is used.
 *
 * This should be called after any validation.
 *
 * @param message The message to show in the warning or error. If null, no warning is issued.
 * @param tagName The tag to add to the help message
 * @param tagValue An extra message to add to the tag
 * @param error If true, when the option is invoked, a [CliktError] is raised immediately instead of issuing a warning.
 */
public fun <T> FlagOption<T>.deprecated(
    message: String? = "",
    tagName: String? = "deprecated",
    tagValue: String = "",
    error: Boolean = false,
): OptionDelegate<T> {
    val helpTags = if (tagName.isNullOrBlank()) helpTags else helpTags + mapOf(tagName to tagValue)
    return copy(transformEnvvar, deprecationTransformer(message, error, transformAll), validator, helpTags = helpTags)
}