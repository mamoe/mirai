package net.mamoe.mirai.clikt.core

import net.mamoe.mirai.clikt.output.CliktHelpFormatter
import net.mamoe.mirai.clikt.output.HelpFormatter
import net.mamoe.mirai.clikt.output.Localization
import net.mamoe.mirai.clikt.output.defaultLocalization
import net.mamoe.mirai.clikt.sources.ChainedValueSource
import net.mamoe.mirai.clikt.sources.ValueSource
import kotlin.properties.ReadOnlyProperty

public typealias TypoSuggestor = (enteredValue: String, possibleValues: List<String>) -> List<String>

/**
 * An object used to control command line parsing and pass data between commands.
 *
 * A new Context instance is created for each command each time the command line is parsed.
 *
 * @property parent If this context is the child of another command, [parent] is the parent command's context.
 * @property command The command that this context associated with.
 * @property allowInterspersedArgs If false, options and arguments cannot be mixed; the first time an argument is
 *   encountered, all remaining tokens are parsed as arguments.
 * @property helpOptionNames The names to use for the help option. If any names in the set conflict with other
 *   options, the conflicting name will not be used for the help option. If the set is empty, or contains no
 *   unique names, no help option will be added.
 * @property helpFormatter The help formatter for this command.
 * @property tokenTransformer An optional transformation function that is called to transform command line
 *   tokens (options and commands) before parsing. This can be used to implement e.g. case insensitive
 *   behavior.
 * @property expandArgumentFiles If true, arguments starting with `@` will be expanded as argument
 *   files. If false, they will be treated as normal arguments.
 * @property correctionSuggestor A callback called when the command line contains an invalid option or
 *   subcommand name. It takes the entered name and a list of all registered names option/subcommand
 *   names and filters the list down to values to suggest to the user.
 */

public class Context constructor(
    public val parent: Context?,
    public val command: CliktCommand,
    public val allowInterspersedArgs: Boolean,
    public val helpOptionNames: Set<String>,
    public val helpFormatter: HelpFormatter,
    public val tokenTransformer: Context.(String) -> String,
    public val expandArgumentFiles: Boolean,
    public val readEnvvarBeforeValueSource: Boolean,
    public val valueSource: ValueSource?,
    public val correctionSuggestor: TypoSuggestor,
    public val localization: Localization,
) {
    public var invokedSubcommand: CliktCommand? = null
        internal set
    public var obj: Any? = null

    /** Find the closest object of type [T] */
    public inline fun <reified T : Any> findObject(): T? {
        return ancestors().mapNotNull { it.obj as? T }.firstOrNull()
    }

    /** Find the closest object of type [T], setting `this.`[obj] if one is not found. */
    public inline fun <reified T : Any> findOrSetObject(defaultValue: () -> T): T {
        return findObject() ?: defaultValue().also { obj = it }
    }

    /** Find the outermost context */
    public fun findRoot(): Context {
        var ctx = this
        while (ctx.parent != null) {
            ctx = ctx.parent!!
        }
        return ctx
    }

    /** Return a list of command names, starting with the topmost command and ending with this Context's parent. */
    public fun parentNames(): List<String> {
        return ancestors().drop(1)
            .map { it.command.commandName }
            .toList().asReversed()
    }

    /** Return a list of command names, starting with the topmost command and ending with this Context's command. */
    public fun commandNameWithParents(): List<String> {
        return parentNames() + command.commandName
    }

    /** Throw a [UsageError] with the given message */
    public fun fail(message: String = ""): Nothing = throw UsageError(message)

    @PublishedApi
    internal fun ancestors(): Sequence<Context> = generateSequence(this) { it.parent }

    public class Builder(parent: Context? = null) {
        /**
         * If false, options and arguments cannot be mixed; the first time an argument is encountered, all
         * remaining tokens are parsed as arguments.
         */
        public var allowInterspersedArgs: Boolean = parent?.allowInterspersedArgs ?: true

        /**
         * The names to use for the help option.
         *
         * If any names in the set conflict with other options, the conflicting name will not be used for the
         * help option. If the set is empty, or contains no unique names, no help option will be added.
         */
        public var helpOptionNames: Set<String> = parent?.helpOptionNames ?: setOf("-h", "--help")

        /** The help formatter for this command, or null to use the default */
        public var helpFormatter: HelpFormatter? = parent?.helpFormatter

        /** An optional transformation function that is called to transform command line */
        public var tokenTransformer: Context.(String) -> String = parent?.tokenTransformer ?: { it }

        /**
         * If true, arguments starting with `@` will be expanded as argument files. If false, they
         * will be treated as normal arguments.
         */
        public var expandArgumentFiles: Boolean = parent?.expandArgumentFiles ?: true

        /**
         * If `false`,the [valueSource] is searched before environment variables.
         *
         * By default, environment variables will be searched for option values before the [valueSource].
         */
        public var readEnvvarBeforeValueSource: Boolean = parent?.readEnvvarBeforeValueSource ?: true

        /**
         * The source that will attempt to read values for options that aren't present on the command line.
         *
         * You can set multiple sources with [valueSources]
         */
        public var valueSource: ValueSource? = parent?.valueSource

        /**
         * Set multiple sources that will attempt to read values for options not present on the command line.
         *
         * Values are read from the first source, then if it doesn't return a value, later sources
         * are read successively until one returns a value or all sources have been read.
         */
        public fun valueSources(vararg sources: ValueSource) {
            valueSource = ChainedValueSource(sources.toList())
        }

        /**
         * A callback called when the command line contains an invalid option or
         * subcommand name. It takes the entered name and a list of all registered names option/subcommand
         * names and filters the list down to values to suggest to the user.
         */
        public var correctionSuggestor: TypoSuggestor = DEFAULT_CORRECTION_SUGGESTOR

        /**
         * Localized strings to use for help output and error reporting.
         */
        public var localization: Localization = defaultLocalization
    }

    public companion object {
        internal fun build(
            command: CliktCommand,
            parent: Context?,
            block: Builder.() -> Unit,
        ): Context {
            with(Builder(parent)) {
                block()
                val interspersed = allowInterspersedArgs && !command.allowMultipleSubcommands &&
                        parent?.let { p -> p.ancestors().any { it.command.allowMultipleSubcommands } } != true
                val formatter = helpFormatter ?: CliktHelpFormatter(localization)
                return Context(
                    parent, command, interspersed,
                    helpOptionNames, formatter, tokenTransformer, expandArgumentFiles,
                    readEnvvarBeforeValueSource, valueSource, correctionSuggestor, localization,
                )
            }
        }
    }
}

/** Find the closest object of type [T], or throw a [NullPointerException] */
@Suppress("unused") // these extensions don't use their receiver, but we want to limit where they can be called
public inline fun <reified T : Any> CliktCommand.requireObject(): ReadOnlyProperty<CliktCommand, T> {
    return ReadOnlyProperty { thisRef, _ -> thisRef.currentContext.findObject()!! }
}

/** Find the closest object of type [T], or null */
@Suppress("unused")
public inline fun <reified T : Any> CliktCommand.findObject(): ReadOnlyProperty<CliktCommand, T?> {
    return ReadOnlyProperty { thisRef, _ -> thisRef.currentContext.findObject() }
}

/**
 * Find the closest object of type [T], setting `context.obj` if one is not found.
 *
 * Note that this function returns a delegate, and so the object will not be set on the context
 * until the delegated property's value is accessed. If you want to set a value for subcommands
 * without accessing the property, call [Context.findOrSetObject] in your [run][CliktCommand.run]
 * function instead.
 */
@Suppress("unused")
public inline fun <reified T : Any> CliktCommand.findOrSetObject(crossinline default: () -> T): ReadOnlyProperty<CliktCommand, T> {
    return ReadOnlyProperty { thisRef, _ -> thisRef.currentContext.findOrSetObject(default) }
}

private val DEFAULT_CORRECTION_SUGGESTOR: TypoSuggestor = { enteredValue, possibleValues ->
    possibleValues.map { it to jaroWinklerSimilarity(enteredValue, it) }
        .filter { it.second > 0.8 }
        .sortedByDescending { it.second }
        .map { it.first }
}