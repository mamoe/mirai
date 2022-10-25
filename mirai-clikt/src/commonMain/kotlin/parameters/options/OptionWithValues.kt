@file:JvmMultifileClass
@file:JvmName("OptionWithValuesKt")

package net.mamoe.mirai.clikt.parameters.options

import net.mamoe.mirai.clikt.core.*
import net.mamoe.mirai.clikt.parameters.arguments.transformAll
import net.mamoe.mirai.clikt.parameters.groups.ParameterGroup
import net.mamoe.mirai.clikt.parameters.internal.NullableLateinit
import net.mamoe.mirai.clikt.parameters.types.int
import net.mamoe.mirai.clikt.parsers.OptionParser.Invocation
import net.mamoe.mirai.clikt.parsers.OptionWithValuesParser
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * A receiver for options transformers.
 *
 * @property name The name that was used to invoke this option.
 * @property option The option that was invoked
 */
public class OptionCallTransformContext(
    public val name: String,
    public val option: Option,
    public val context: Context,
) : Option by option {
    /** Throw an exception indicating that an invalid value was provided. */
    public fun fail(message: String): Nothing = throw BadParameterValue(message, name)

    /** Issue a message that can be shown to the user */
    public fun message(message: String): Unit = context.command.issueMessage(message)

    /** If [value] is false, call [fail] with the output of [lazyMessage] */
    public inline fun require(value: Boolean, lazyMessage: () -> String = { "" }) {
        if (!value) fail(lazyMessage())
    }
}

/**
 * A receiver for options transformers.
 *
 * @property option The option that was invoked
 */
public class OptionTransformContext(
    public val option: Option,
    public val context: Context,
) : Option by option {
    /** Throw an exception indicating that usage was incorrect. */
    public fun fail(message: String): Nothing = throw BadParameterValue(message, option)

    /** Issue a message that can be shown to the user */
    public fun message(message: String): Unit = context.command.issueMessage(message)

    /** If [value] is false, call [fail] with the output of [lazyMessage] */
    public inline fun require(value: Boolean, lazyMessage: () -> String = { "" }) {
        if (!value) fail(lazyMessage())
    }
}

/** A callback that transforms a single value from a string to the value type */
public typealias ValueTransformer<ValueT> = ValueConverter<String, ValueT>

/** A block that converts a single value from one type to another */
public typealias ValueConverter<InT, ValueT> = OptionCallTransformContext.(InT) -> ValueT

/**
 * A callback that transforms all the values for a call to the call type.
 *
 * The input list will always have a size equal to `nvalues`
 */
public typealias ArgsTransformer<ValueT, EachT> = OptionCallTransformContext.(List<ValueT>) -> EachT

/**
 * A callback that transforms all of the calls to the final option type.
 *
 * The input list will have a size equal to the number of times the option appears on the command line.
 */
public typealias CallsTransformer<EachT, AllT> = OptionTransformContext.(List<EachT>) -> AllT

/** A callback validates the final option type */
public typealias OptionValidator<AllT> = OptionTransformContext.(AllT) -> Unit

/**
 * An [Option] that takes one or more values.
 *
 * @property metavarWithDefault The metavar to use. Specified at option creation.
 * @property envvar The environment variable name to use.
 * @property valueSplit The pattern to split values from the command line on. By default, values are
 *   split on whitespace.
 * @property transformValue Called in [finalize] to transform each value provided to each invocation.
 * @property transformEach Called in [finalize] to transform each invocation.
 * @property transformAll Called in [finalize] to transform all invocations into the final value.
 * @property transformValidator Called after all parameters have been [finalized][finalize] to validate the output of [transformAll]
 */
// `AllT` is deliberately not an out parameter. If it was, it would allow undesirable combinations such as
// default("").int()
public class OptionWithValues<AllT, EachT, ValueT> internal constructor(
    names: Set<String>,
    public val metavarWithDefault: ValueWithDefault<Context.() -> String?>,
    override val nvalues: Int,
    override val optionHelp: String,
    override val hidden: Boolean,
    override val helpTags: Map<String, String>,
    override val valueSourceKey: String?,
    public val envvar: String?,
    public val valueSplit: Regex?,
    override val parser: OptionWithValuesParser,
    public val transformValue: ValueTransformer<ValueT>,
    public val transformEach: ArgsTransformer<ValueT, EachT>,
    public val transformAll: CallsTransformer<EachT, AllT>,
    public val transformValidator: OptionValidator<AllT>,
) : OptionDelegate<AllT>, GroupableOption {
    override var parameterGroup: ParameterGroup? = null
    override var groupName: String? = null
    override fun metavar(context: Context): String? = metavarWithDefault.value.invoke(context)
    override var value: AllT by NullableLateinit("Cannot read from option delegate before parsing command line")
        private set
    override val secondaryNames: Set<String> get() = emptySet()
    override var names: Set<String> = names
        private set

    override fun finalize(context: Context, invocations: List<Invocation>) {
        val inv = when (val v = getFinalValue(context, invocations)) {
            is FinalValue.Parsed -> {
                when (valueSplit) {
                    null -> invocations
                    else -> invocations.map { inv -> inv.copy(values = inv.values.flatMap { it.split(valueSplit) }) }
                }
            }

            is FinalValue.Sourced -> {
                if (v.values.any { it.values.size != nvalues }) throw IncorrectOptionValueCount(this, longestName()!!)
                v.values.map { Invocation("", it.values) }
            }

            is FinalValue.Envvar -> {
                when (valueSplit) {
                    null -> listOf(Invocation(v.key, listOf(v.value)))
                    else -> listOf(Invocation(v.key, v.value.split(valueSplit)))
                }
            }
        }

        value = transformAll(OptionTransformContext(this, context), inv.map {
            val tc = OptionCallTransformContext(it.name, this, context)
            transformEach(tc, it.values.map { v -> transformValue(tc, v) })
        })
    }

    override operator fun provideDelegate(
        thisRef: ParameterHolder,
        prop: KProperty<*>,
    ): ReadOnlyProperty<ParameterHolder, AllT> {
        require(secondaryNames.isEmpty()) {
            "Secondary option names are only allowed on flag options."
        }
        names = inferOptionNames(names, prop.name)
        thisRef.registerOption(this)
        return this
    }

    override fun postValidate(context: Context) {
        transformValidator(OptionTransformContext(this, context), value)
    }

    /** Create a new option that is a copy of this one with different transforms. */
    public fun <AllT, EachT, ValueT> copy(
        transformValue: ValueTransformer<ValueT>,
        transformEach: ArgsTransformer<ValueT, EachT>,
        transformAll: CallsTransformer<EachT, AllT>,
        validator: OptionValidator<AllT>,
        names: Set<String> = this.names,
        metavarWithDefault: ValueWithDefault<Context.() -> String?> = this.metavarWithDefault,
        nvalues: Int = this.nvalues,
        help: String = this.optionHelp,
        hidden: Boolean = this.hidden,
        helpTags: Map<String, String> = this.helpTags,
        valueSourceKey: String? = this.valueSourceKey,
        envvar: String? = this.envvar,
        valueSplit: Regex? = this.valueSplit,
        parser: OptionWithValuesParser = this.parser,
    ): OptionWithValues<AllT, EachT, ValueT> {
        return OptionWithValues(
            names = names,
            metavarWithDefault = metavarWithDefault,
            nvalues = nvalues,
            optionHelp = help,
            hidden = hidden,
            helpTags = helpTags,
            valueSourceKey = valueSourceKey,
            envvar = envvar,
            valueSplit = valueSplit,
            parser = parser,
            transformValue = transformValue,
            transformEach = transformEach,
            transformAll = transformAll,
            transformValidator = validator
        )
    }

    /** Create a new option that is a copy of this one with the same transforms. */
    public fun copy(
        validator: OptionValidator<AllT> = this.transformValidator,
        names: Set<String> = this.names,
        metavarWithDefault: ValueWithDefault<Context.() -> String?> = this.metavarWithDefault,
        nvalues: Int = this.nvalues,
        help: String = this.optionHelp,
        hidden: Boolean = this.hidden,
        helpTags: Map<String, String> = this.helpTags,
        envvar: String? = this.envvar,
        valueSourceKey: String? = this.valueSourceKey,
        valueSplit: Regex? = this.valueSplit,
        parser: OptionWithValuesParser = this.parser,
    ): OptionWithValues<AllT, EachT, ValueT> {
        return OptionWithValues(
            names = names,
            metavarWithDefault = metavarWithDefault,
            nvalues = nvalues,
            optionHelp = help,
            hidden = hidden,
            helpTags = helpTags,
            valueSourceKey = valueSourceKey,
            envvar = envvar,
            valueSplit = valueSplit,
            parser = parser,
            transformValue = transformValue,
            transformEach = transformEach,
            transformAll = transformAll,
            transformValidator = validator
        )
    }
}

public typealias NullableOption<EachT, ValueT> = OptionWithValues<EachT?, EachT, ValueT>
public typealias RawOption = NullableOption<String, String>

@PublishedApi
internal fun <T : Any> defaultEachProcessor(): ArgsTransformer<T, T> = { it.single() }

@PublishedApi
internal fun <T : Any> defaultAllProcessor(): CallsTransformer<T, T?> = { it.lastOrNull() }

@PublishedApi
internal fun <T> defaultValidator(): OptionValidator<T> = { }

/**
 * Create a property delegate option.
 *
 * By default, the property will return null if the option does not appear on the command line. If the option
 * is invoked multiple times, the value from the last invocation will be used The option can be modified with
 * functions like [int], [pair], and [multiple].
 *
 * @param names The names that can be used to invoke this option. They must start with a punctuation character.
 *   If not given, a name is inferred from the property name.
 * @param help The description of this option, usually a single line.
 * @param metavar A name representing the values for this option that can be displayed to the user.
 *   Automatically inferred from the type.
 * @param hidden Hide this option from help outputs.
 * @param envvar The environment variable that will be used for the value if one is not given on the command
 *   line.
 * @param helpTags Extra information about this option to pass to the help formatter
 */
@Suppress("unused")
public fun ParameterHolder.option(
    vararg names: String,
    help: String = "",
    metavar: String? = null,
    hidden: Boolean = false,
    envvar: String? = null,
    helpTags: Map<String, String> = emptyMap(),
    valueSourceKey: String? = null,
): RawOption = OptionWithValues(
    names = names.toSet(),
    metavarWithDefault = ValueWithDefault(metavar?.let { { it } }) { localization.stringMetavar() },
    nvalues = 1,
    optionHelp = help,
    hidden = hidden,
    helpTags = helpTags,
    valueSourceKey = valueSourceKey,
    envvar = envvar,
    valueSplit = null,
    parser = OptionWithValuesParser,
    transformValue = { it },
    transformEach = defaultEachProcessor(),
    transformAll = defaultAllProcessor(),
    transformValidator = defaultValidator()
)

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
 *      .int()
 *      .help("This is an option that takes a number")
 * ```
 */
public fun <AllT, EachT, ValueT> OptionWithValues<AllT, EachT, ValueT>.help(help: String): OptionWithValues<AllT, EachT, ValueT> {
    return copy(help = help)
}

/**
 * Check the final option value and raise an error if it's not valid.
 *
 * The [validator] is called with the final option type (the output of [transformAll]), and should
 * call [fail][OptionTransformContext.fail] if the value is not valid.
 *
 * Your [validator] can also call [require][OptionTransformContext.require] to fail automatically if
 * an expression is false, or [message][OptionTransformContext.message] to show the user a warning
 * message without aborting.
 *
 * ### Example:
 *
 * ```
 * val opt by option().int().validate { require(it % 2 == 0) { "value must be even" } }
 * ```
 */
public fun <AllT : Any, EachT, ValueT> OptionWithValues<AllT, EachT, ValueT>.validate(
    validator: OptionValidator<AllT>,
): OptionDelegate<AllT> {
    return copy(transformValue, transformEach, transformAll, validator)
}

/**
 * Check the final option value and raise an error if it's not valid.
 *
 * The [validator] is called with the final option type (the output of [transformAll]), and should
 * call [fail][OptionTransformContext.fail] if the value is not valid. The [validator] is not called
 * if the delegate value is null.
 *
 * Your [validator] can also call [require][OptionTransformContext.require] to fail automatically if
 * an expression is false, or [message][OptionTransformContext.message] to show the user a warning
 * message without aborting.
 *
 * ### Example:
 *
 * ```
 * val opt by option().int().validate { require(it % 2 == 0) { "value must be even" } }
 * ```
 */
@JvmName("nullableValidate")
public inline fun <AllT : Any, EachT, ValueT> OptionWithValues<AllT?, EachT, ValueT>.validate(
    crossinline validator: OptionValidator<AllT>,
): OptionDelegate<AllT?> {
    return copy(transformValue, transformEach, transformAll, { if (it != null) validator(it) })
}

/**
 * Check the final option value and raise an error if it's not valid.
 *
 * The [validator] is called with the final option type (the output of [transformAll]), and should
 * return `false` if the value is not valid. You can specify a [message] to include in the error
 * output.
 *
 * You can use [validate] for more complex checks.
 *
 * ### Example:
 *
 * ```
 * val opt by option().int().check("value must be even") { it % 2 == 0 }
 * ```
 */
public inline fun <AllT : Any, EachT, ValueT> OptionWithValues<AllT, EachT, ValueT>.check(
    message: String,
    crossinline validator: (AllT) -> Boolean,
): OptionDelegate<AllT> {
    return check({ message }, validator)
}

/**
 * Check the final argument value and raise an error if it's not valid.
 *
 * The [validator] is called with the final option type (the output of [transformAll]), and should
 * return `false` if the value is not valid. You can specify a [lazyMessage] the returns a message
 * to include in the error output.
 *
 * You can use [validate] for more complex checks.
 *
 * ### Example:
 *
 * ```
 * val opt by option().int().check(lazyMessage={"$it is not even"}) { it % 2 == 0 }
 * ```
 */
public inline fun <AllT : Any, EachT, ValueT> OptionWithValues<AllT, EachT, ValueT>.check(
    crossinline lazyMessage: (AllT) -> String = { it.toString() },
    crossinline validator: (AllT) -> Boolean,
): OptionDelegate<AllT> {
    return validate { require(validator(it)) { lazyMessage(it) } }
}

/**
 * Check the final option value and raise an error if it's not valid.
 *
 * The [validator] is called with the final option type (the output of [transformAll]), and should
 * return `false` if the value is not valid. You can specify a [message] to include in the error
 * output. The [validator] is not called if the delegate value is null.
 *
 * You can use [validate] for more complex checks.
 *
 * ### Example:
 *
 * ```
 * val opt by option().int().check("value must be even") { it % 2 == 0 }
 * ```
 */
@JvmName("nullableCheck")
public inline fun <AllT : Any, EachT, ValueT> OptionWithValues<AllT?, EachT, ValueT>.check(
    message: String,
    crossinline validator: (AllT) -> Boolean,
): OptionDelegate<AllT?> {
    return check({ message }, validator)
}

/**
 * Check the final argument value and raise an error if it's not valid.
 *
 * The [validator] is called with the final option type (the output of [transformAll]), and should
 * return `false` if the value is not valid. You can specify a [lazyMessage] the returns a message
 * to include in the error output. The [validator] is not called if the delegate value is null.
 *
 * You can use [validate] for more complex checks.
 *
 * ### Example:
 *
 * ```
 * val opt by option().int().check(lazyMessage={"$it is not even"}) { it % 2 == 0 }
 * ```
 */
@JvmName("nullableLazyCheck")
public inline fun <AllT : Any, EachT, ValueT> OptionWithValues<AllT?, EachT, ValueT>.check(
    crossinline lazyMessage: (AllT) -> String = { it.toString() },
    crossinline validator: (AllT) -> Boolean,
): OptionDelegate<AllT?> {
    return validate { require(validator(it)) { lazyMessage(it) } }
}

/**
 * Mark this option as deprecated in the help output.
 *
 * By default, a tag is added to the help message and a warning is printed if the option is used.
 *
 * This should be called after any conversion and validation.
 *
 * ### Example:
 *
 * ```
 * val opt by option().int().validate { require(it % 2 == 0) { "value must be even" } }
 *    .deprecated("WARNING: --opt is deprecated, use --new-opt instead")
 * ```
 *
 * @param message The message to show in the warning or error. If null, no warning is issued.
 * @param tagName The tag to add to the help message
 * @param tagValue An extra message to add to the tag
 * @param error If true, when the option is invoked, a [CliktError] is raised immediately instead of issuing a warning.
 */
public fun <AllT, EachT, ValueT> OptionWithValues<AllT, EachT, ValueT>.deprecated(
    message: String? = "",
    tagName: String? = "deprecated",
    tagValue: String = "",
    error: Boolean = false,
): OptionDelegate<AllT> {
    val helpTags = if (tagName.isNullOrBlank()) helpTags else helpTags + mapOf(tagName to tagValue)
    return copy(
        transformValue,
        transformEach,
        deprecationTransformer(message, error, transformAll),
        transformValidator,
        helpTags = helpTags
    )
}