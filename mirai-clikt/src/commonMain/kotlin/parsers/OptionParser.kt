package net.mamoe.mirai.clikt.parsers

import net.mamoe.mirai.clikt.parameters.options.Option

/**
 * A parser for [Option]s.
 *
 * All functions should be pure, since the same command instance can parse arguments multiple times.
 */
public interface OptionParser {
    /**
     * Parse a single short option and its value.
     *
     * @param name The name of the flag used to invoke this option
     * @param argv The entire list of command line arguments for the command
     * @param index The index of the option flag in [argv], which may contain multiple short parameters.
     * @param optionIndex The index of the option within `argv\[index]`
     */
    public fun parseShortOpt(
        option: Option,
        name: String,
        argv: List<String>,
        index: Int,
        optionIndex: Int
    ): ParseResult

    /**
     * Parse a single long option and its value.
     *
     * @param name The name of the flag used to invoke this option
     * @param argv The entire list of command line arguments for the command
     * @param index The index of the option flag in [argv], which may contain an '=' with the first value
     */
    public fun parseLongOpt(
        option: Option,
        name: String,
        argv: List<String>,
        index: Int,
        explicitValue: String?
    ): ParseResult

    /**
     * The input from a single instance of an option input.
     *
     * @param name The name that was used to invoke the option. May be empty if the value was not retrieved
     *   from the command line (e.g. values from environment variables).
     * @param values The values provided to the option. All instances passed to [Option.finalize]
     *   will have a size equal to [Option.nvalues].
     */
    public data class Invocation(val name: String, val values: List<String>)

    /**
     * @param consumedCount The number of items in argv that were consumed. This number must be >= 1 if the
     *   entire option was consumed, or 0 if there are other options in the same index (e.g. flag options)
     * @param invocation The data from this invocation.
     */
    public data class ParseResult(val consumedCount: Int, val invocation: Invocation) {
        public constructor(consumedCount: Int, name: String, values: List<String>)
                : this(consumedCount, Invocation(name, values))
    }
}