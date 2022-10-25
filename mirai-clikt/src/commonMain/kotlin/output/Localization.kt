package net.mamoe.mirai.clikt.output

import net.mamoe.mirai.clikt.core.*
import net.mamoe.mirai.clikt.parameters.groups.ChoiceGroup
import net.mamoe.mirai.clikt.parameters.groups.MutuallyExclusiveOptions

/**
 * Strings to use for help output and error messages
 */
public interface Localization {
    /** [Abort] was thrown */
    public fun aborted(): String = "Aborted!"

    /** Prefix for any [UsageError] */
    public fun usageError(message: String): String = "Error: $message"

    /** Message for [BadParameterValue] */
    public fun badParameter(): String = "Invalid value"

    /** Message for [BadParameterValue] */
    public fun badParameterWithMessage(message: String): String = "Invalid value: $message"

    /** Message for [BadParameterValue] */
    public fun badParameterWithParam(paramName: String): String = "Invalid value for \"$paramName\""

    /** Message for [BadParameterValue] */
    public fun badParameterWithMessageAndParam(paramName: String, message: String): String =
        "Invalid value for \"$paramName\": $message"

    /** Message for [MissingOption] */
    public fun missingOption(paramName: String): String = "Missing option \"$paramName\""

    /** Message for [MissingArgument] */
    public fun missingArgument(paramName: String): String = "Missing argument \"$paramName\""

    /** Message for [NoSuchSubcommand] */
    public fun noSuchSubcommand(name: String, possibilities: List<String>): String {
        return "no such subcommand: \"$name\"" + when (possibilities.size) {
            0 -> ""
            1 -> ". Did you mean \"${possibilities[0]}\"?"
            else -> possibilities.joinToString(prefix = ". (Possible subcommands: ", postfix = ")")
        }
    }

    /** Message for [NoSuchOption] */
    public fun noSuchOption(name: String, possibilities: List<String>): String {
        return "no such option: \"$name\"" + when (possibilities.size) {
            0 -> ""
            1 -> ". Did you mean \"${possibilities[0]}\"?"
            else -> possibilities.joinToString(prefix = ". (Possible options: ", postfix = ")")
        }
    }

    /**
     * Message for [IncorrectOptionValueCount]
     *
     * @param count non-negative count of required values
     */
    public fun incorrectOptionValueCount(name: String, count: Int): String {
        return when (count) {
            0 -> "option $name does not take a value"
            1 -> "option $name requires a value"
            else -> "option $name requires $count values"
        }
    }

    /**
     * Message for [IncorrectArgumentValueCount]
     *
     * @param count non-negative count of required values
     */
    public fun incorrectArgumentValueCount(name: String, count: Int): String {
        return when (count) {
            0 -> "argument $name does not take a value"
            1 -> "argument $name requires a value"
            else -> "argument $name requires $count values"
        }
    }

    /**
     * Message for [MutuallyExclusiveGroupException]
     *
     * @param others non-empty list of other options in the group
     */
    public fun mutexGroupException(name: String, others: List<String>): String {
        return "option $name cannot be used with ${others.joinToString(" or ")}"
    }

    /** Message for [FileNotFound] */
    public fun fileNotFound(filename: String): String = "$filename not found"

    /** Message for [InvalidFileFormat]*/
    public fun invalidFileFormat(filename: String, message: String): String =
        "incorrect format in file $filename: $message"

    /** Message for [InvalidFileFormat]*/
    public fun invalidFileFormat(filename: String, lineNumber: Int, message: String): String =
        "incorrect format in file $filename line $lineNumber: $message"

    /** Error in message for [InvalidFileFormat] */
    public fun unclosedQuote(): String = "unclosed quote"

    /** Error in message for [InvalidFileFormat] */
    public fun fileEndsWithSlash(): String = "file ends with \\"

    /** One extra argument is present */
    public fun extraArgumentOne(name: String): String = "Got unexpected extra argument $name"

    /** More than one extra argument is present */
    public fun extraArgumentMany(name: String, count: Int): String = "Got unexpected extra arguments $name"

    /** Error message when reading flag option from a file */
    public fun invalidFlagValueInFile(name: String): String = "Invalid flag value in file for option $name"

    /** Error message when reading switch option from environment variable */
    public fun switchOptionEnvvar(): String = "environment variables not supported for switch options"

    /** Required [MutuallyExclusiveOptions] was not provided */
    public fun requiredMutexOption(options: String): String = "Must provide one of $options"

    /**
     * [ChoiceGroup] value was invalid
     *
     * @param choices non-empty list of possible choices
     */
    public fun invalidGroupChoice(value: String, choices: List<String>): String {
        return "invalid choice: $value. (choose from ${choices.joinToString()})"
    }

    /** Invalid value for a parameter of type [Double] or [Float] */
    public fun floatConversionError(value: String): String = "$value is not a valid floating point value"

    /** Invalid value for a parameter of type [Int] or [Long] */
    public fun intConversionError(value: String): String = "$value is not a valid integer"

    /** Invalid value for a parameter of type [Boolean] */
    public fun boolConversionError(value: String): String = "$value is not a valid boolean"

    /** Invalid value falls outside range */
    public fun rangeExceededMax(value: String, limit: String): String =
        "$value is larger than the maximum valid value of $limit."

    /** Invalid value falls outside range */
    public fun rangeExceededMin(value: String, limit: String): String =
        "$value is smaller than the minimum valid value of $limit."

    /** Invalid value falls outside range */
    public fun rangeExceededBoth(value: String, min: String, max: String): String =
        "$value is not in the valid range of $min to $max."

    /**
     * Invalid value for `choice` parameter
     *
     * @param choices non-empty list of possible choices
     */
    public fun invalidChoice(choice: String, choices: List<String>): String {
        return "invalid choice: $choice. (choose from ${choices.joinToString()})"
    }

    /** Metavar used for options with unspecified value type */
    public fun defaultMetavar(): String = "VALUE"

    /** Metavar used for options that take [String] values */
    public fun stringMetavar(): String = "TEXT"

    /** Metavar used for options that take [Float] or [Double] values */
    public fun floatMetavar(): String = "FLOAT"

    /** Metavar used for options that take [Int] or [Long] values */
    public fun intMetavar(): String = "INT"

    /** Metavar used for options that take `File` or `Path` values */
    public fun pathMetavar(): String = "PATH"

    /** Metavar used for options that take `InputStream` or `OutputStream` values */
    public fun fileMetavar(): String = "FILE"

    /** The title for the usage section of help output */
    public fun usageTitle(): String = "Usage:"

    /** The title for the options section of help output */
    public fun optionsTitle(): String = "Options:"

    /** The title for the arguments section of help output */
    public fun argumentsTitle(): String = "Arguments:"

    /** The title for the subcommands section of help output */
    public fun commandsTitle(): String = "Commands:"

    /** The that indicates where options may be present in the usage help output */
    public fun optionsMetavar(): String = "[OPTIONS]"

    /** The that indicates where subcommands may be present in the usage help output */
    public fun commandMetavar(): String = "COMMAND [ARGS]..."

    /** Text rendered for parameters tagged with [HelpFormatter.Tags.DEFAULT] */
    public fun helpTagDefault(): String = "default"

    /** Text rendered for parameters tagged with [HelpFormatter.Tags.REQUIRED] */
    public fun helpTagRequired(): String = "required"

    /** The default message for the `--help` option. */
    public fun helpOptionMessage(): String = "Show this message and exit"
}

internal val defaultLocalization = object : Localization {}