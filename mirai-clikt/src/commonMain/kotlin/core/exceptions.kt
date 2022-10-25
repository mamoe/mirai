package net.mamoe.mirai.clikt.core

import net.mamoe.mirai.clikt.output.Localization
import net.mamoe.mirai.clikt.output.defaultLocalization
import net.mamoe.mirai.clikt.parameters.arguments.Argument
import net.mamoe.mirai.clikt.parameters.arguments.convert
import net.mamoe.mirai.clikt.parameters.options.Option
import net.mamoe.mirai.clikt.parameters.options.longestName

/**
 * An internal error that signals Clikt to abort.
 *
 * @property error If true, print "Aborted" and exit with an error code. Otherwise, exit with no error code.
 */
internal class Abort(val error: Boolean = true) : RuntimeException()

/**
 * An exception during command line processing that should be shown to the user.
 *
 * If calling [CliktCommand.main], these exceptions will be caught and the appropriate info will be printed.
 */
public open class CliktError(message: String? = null, cause: Exception? = null) : RuntimeException(message, cause)

/**
 * An exception that indicates that the command's help should be printed.
 *
 * Execution should be immediately halted.
 *
 * @property error If true, execution should halt with an error. Otherwise, execution halt with no error code.
 */
public class PrintHelpMessage(
    public val command: CliktCommand,
    public val error: Boolean = false,
) : CliktError()

/**
 * An exception that indicates that a message should be printed.
 *
 * Execution should be immediately halted.
 *
 * @property error If true, execution should halt with an error. Otherwise, execution halt with no error code.
 */
public open class PrintMessage(
    message: String,
    public val error: Boolean = false,
) : CliktError(message)

/**
 * An internal exception that signals a usage error.
 *
 * The [option] and [argument] properties are used in message formatting, and can be set after the exception
 * is created. If this is thrown inside a call to [convert], the [argument] or [option] value will be set
 * automatically
 *
 * @property text Extra text to add to the message. Not all subclasses uses this.
 * @property paramName The name of the parameter that caused the error. If possible, this should be set to the
 *   actual name used. If not set, it will be inferred from [argument] or [option] if either is set.
 * @property option The option that caused this error. This may be set after the error is thrown.
 * @property argument The argument that caused this error. This may be set after the error is thrown.
 * @property statusCode The value to use as the exit code for the process. If you use
 *   [CliktCommand.main], it will pass this value to `exitProcess` after printing [message]. Defaults to 1.
 */
public open class UsageError private constructor(
    public val text: String? = null,
    public var paramName: String? = null,
    public var option: Option? = null,
    public var argument: Argument? = null,
    public var context: Context? = null,
    public val statusCode: Int = 1,
) : CliktError() {
    public constructor(text: String, paramName: String? = null, context: Context? = null, statusCode: Int = 1)
            : this(text, paramName, null, null, context, statusCode)

    public constructor(text: String, argument: Argument, context: Context? = null, statusCode: Int = 1)
            : this(text, null, null, argument, context, statusCode)

    public constructor(text: String, option: Option, context: Context? = null, statusCode: Int = 1)
            : this(text, null, option, null, context, statusCode)

    public fun helpMessage(): String = buildString {
        context?.let { append(it.command.getFormattedUsage()).append("\n\n") }
        append(localization.usageError(formatMessage()))
    }

    override val message: String? get() = formatMessage()

    protected open fun formatMessage(): String = text ?: ""

    protected fun inferParamName(): String = when {
        paramName != null -> paramName!!
        option != null -> option?.longestName() ?: ""
        argument != null -> argument!!.name
        else -> ""
    }

    protected val localization: Localization get() = context?.localization ?: defaultLocalization
}

/**
 * A parameter was given the correct number of values, but of invalid format or type.
 */
public class BadParameterValue : UsageError {
    public constructor(text: String, context: Context? = null) : super(text, null, context)
    public constructor(text: String, paramName: String, context: Context? = null) : super(text, paramName, context)
    public constructor(text: String, argument: Argument, context: Context? = null) : super(text, argument, context)
    public constructor(text: String, option: Option, context: Context? = null) : super(text, option, context)

    override fun formatMessage(): String {
        val m = text.takeUnless { it.isNullOrBlank() }
        val p = inferParamName().takeIf { it.isNotBlank() }

        return when {
            m == null && p == null -> localization.badParameter()
            m == null && p != null -> localization.badParameterWithParam(p)
            m != null && p == null -> localization.badParameterWithMessage(m)
            m != null && p != null -> localization.badParameterWithMessageAndParam(p, m)
            else -> error("impossible")
        }
    }
}

/** A required option was not provided */
public class MissingOption(option: Option, context: Context? = null) : UsageError("", option, context) {
    override fun formatMessage(): String = localization.missingOption(inferParamName())
}

/** A required argument was not provided */
public class MissingArgument(argument: Argument, context: Context? = null) : UsageError("", argument, context) {
    override fun formatMessage(): String = localization.missingArgument(inferParamName())
}

/** A parameter was provided that does not exist. */
public open class NoSuchParameter protected constructor(context: Context?) : UsageError("", context = context)

/** A subcommand was provided that does not exist. */
public class NoSuchSubcommand(
    private val givenName: String,
    private val possibilities: List<String> = emptyList(),
    context: Context? = null,
) : NoSuchParameter(context) {
    override fun formatMessage(): String {
        return localization.noSuchSubcommand(givenName, possibilities)
    }
}


/** An option was provided that does not exist. */
public class NoSuchOption(
    private val givenName: String,
    private val possibilities: List<String> = emptyList(),
    context: Context? = null,
) : NoSuchParameter(context) {
    override fun formatMessage(): String {
        return localization.noSuchOption(givenName, possibilities)
    }
}


/** An option was supplied but the number of values supplied to the option was incorrect. */
public class IncorrectOptionValueCount(
    option: Option,
    private val givenName: String,
    context: Context? = null,
) : UsageError("", option, context) {
    override fun formatMessage(): String {
        return localization.incorrectOptionValueCount(givenName, option!!.nvalues)
    }
}

/** An argument was supplied but the number of values supplied was incorrect. */
public class IncorrectArgumentValueCount(
    argument: Argument,
    context: Context? = null,
) : UsageError("", argument, context) {
    override fun formatMessage(): String {
        return localization.incorrectArgumentValueCount(inferParamName(), argument!!.nvalues)
    }
}

public class MutuallyExclusiveGroupException(
    private val names: List<String>,
    context: Context? = null,
) : UsageError("", context = context) {
    init {
        require(names.size > 1) { "must provide at least two names" }
    }

    override fun formatMessage(): String {
        return localization.mutexGroupException(names.first(), names.drop(1))
    }
}

/** A required configuration file was not found. */
public class FileNotFound(
    private val filename: String,
    context: Context? = null,
) : UsageError("", context = context) {
    override fun formatMessage(): String {
        return localization.fileNotFound(filename)
    }
}

/** A configuration file failed to parse correctly */
public class InvalidFileFormat(
    private val filename: String,
    message: String,
    private val lineno: Int? = null,
    context: Context? = null,
) : UsageError(message, context = context) {
    override fun formatMessage(): String {
        return when (lineno) {
            null -> localization.invalidFileFormat(filename, text!!)
            else -> localization.invalidFileFormat(filename, lineno, text!!)
        }
    }
}