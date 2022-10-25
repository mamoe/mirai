package net.mamoe.mirai.clikt.parameters.options

import net.mamoe.mirai.clikt.core.*
import net.mamoe.mirai.clikt.output.HelpFormatter
import net.mamoe.mirai.clikt.parsers.OptionParser
import net.mamoe.mirai.clikt.sources.ValueSource
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * An optional command line parameter that takes a fixed number of values.
 *
 * Options can take any fixed number of values, including 0.
 */
public interface Option {
    /** A name representing the values for this option that can be displayed to the user. */
    public fun metavar(context: Context): String?

    /** The description of this option, usually a single line. */
    public val optionHelp: String

    /** The parser for this option's values. */
    public val parser: OptionParser

    /** The names that can be used to invoke this option. They must start with a punctuation character. */
    public val names: Set<String>

    /** Names that can be used for a secondary purpose, like disabling flag options. */
    public val secondaryNames: Set<String>

    /** The number of values that must be given to this option. */
    public val nvalues: Int

    /** If true, this option should not appear in help output. */
    public val hidden: Boolean

    /** Extra information about this option to pass to the help formatter. */
    public val helpTags: Map<String, String>

    /** Optional explicit key to use when looking this option up from a [ValueSource] */
    public val valueSourceKey: String?

    /** Information about this option for the help output. */
    public fun parameterHelp(context: Context): HelpFormatter.ParameterHelp.Option? = when {
        hidden -> null
        else -> HelpFormatter.ParameterHelp.Option(
            names,
            secondaryNames,
            metavar(context),
            optionHelp,
            nvalues,
            helpTags,
            groupName = (this as? StaticallyGroupedOption)?.groupName
                ?: (this as? GroupableOption)?.parameterGroup?.groupName
        )
    }

    /**
     * Called after this command's argv is parsed to transform and store the option's value.
     *
     * You cannot refer to other parameter values during this call, since they might not have been
     * finalized yet.
     *
     * @param context The context for this parse
     * @param invocations A possibly empty list of invocations of this option.
     */
    public fun finalize(context: Context, invocations: List<OptionParser.Invocation>)

    /**
     * Called after all of a command's parameters have been [finalize]d to perform validation of the final value.
     */
    public fun postValidate(context: Context)
}

/** An option that functions as a property delegate */
public interface OptionDelegate<T> : GroupableOption, ReadOnlyProperty<ParameterHolder, T> {
    /**
     * The value for this option.
     *
     * An exception should be thrown if this property is accessed before [finalize] is called.
     */
    public val value: T

    /** Implementations must call [ParameterHolder.registerOption] */
    public operator fun provideDelegate(
        thisRef: ParameterHolder,
        prop: KProperty<*>
    ): ReadOnlyProperty<ParameterHolder, T>

    override fun getValue(thisRef: ParameterHolder, property: KProperty<*>): T = value
}

internal fun inferOptionNames(names: Set<String>, propertyName: String): Set<String> {
    if (names.isNotEmpty()) {
        val invalidName = names.find { !it.matches(Regex("""[\-@/+]{1,2}[\w\-_]+""")) }
        require(invalidName == null) { "Invalid option name \"$invalidName\"" }
        return names
    }
    val normalizedName = "--" + propertyName.replace(Regex("""[a-z][A-Z]""")) {
        "${it.value[0]}-${it.value[1]}"
    }.lowercase()
    return setOf(normalizedName)
}

private val LETTER_OR_DIGIT_RE = Regex("""[a-zA-Z0-9]""")

private fun isLetterOrDigit(c: Char): Boolean = LETTER_OR_DIGIT_RE.matches(c.toString())

/** Split an option token into a pair of prefix to simple name. */
internal fun splitOptionPrefix(name: String): Pair<String, String> =
    when {
        name.length < 2 || isLetterOrDigit(name[0]) -> "" to name
        name.length > 2 && name[0] == name[1] -> name.slice(0..1) to name.substring(2)
        else -> name.substring(0, 1) to name.substring(1)
    }

internal fun <EachT, AllT> deprecationTransformer(
    message: String? = "",
    error: Boolean = false,
    transformAll: CallsTransformer<EachT, AllT>,
): CallsTransformer<EachT, AllT> = {
    if (it.isNotEmpty()) {
        val msg = when (message) {
            null -> ""
            "" -> "${if (error) "ERROR" else "WARNING"}: option ${option.longestName()} is deprecated"
            else -> message
        }
        if (error) {
            throw CliktError(msg)
        } else if (message != null) {
            message(msg)
        }
    }
    transformAll(it)
}

internal fun Option.longestName(): String? = names.maxByOrNull { it.length }

internal sealed class FinalValue {
    data class Parsed(val values: List<OptionParser.Invocation>) : FinalValue()
    data class Sourced(val values: List<ValueSource.Invocation>) : FinalValue()
    data class Envvar(val key: String, val value: String) : FinalValue()
}

internal fun Option.getFinalValue(
    context: Context,
    invocations: List<OptionParser.Invocation>,
): FinalValue {
    return when {
        invocations.isNotEmpty() -> FinalValue.Parsed(invocations)
        context.readEnvvarBeforeValueSource -> {
            readValueSource(context)
        }

        else -> {
            readValueSource(context)
        }
    } ?: FinalValue.Parsed(emptyList())
}

private fun Option.readValueSource(context: Context): FinalValue? {
    return context.valueSource?.getValues(context, this)?.ifEmpty { null }
        ?.let { FinalValue.Sourced(it) }
}