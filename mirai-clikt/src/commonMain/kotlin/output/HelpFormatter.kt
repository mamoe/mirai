package net.mamoe.mirai.clikt.output

/**
 * Creates help and usage strings for a command.
 *
 * You can set the formatter for a command when configuring the context.
 */
public interface HelpFormatter {
    /**
     * Create the one-line usage information for a command.
     *
     * This is usually displayed when incorrect input is encountered, and as the first line of the full help.
     */
    public fun formatUsage(parameters: List<ParameterHelp>, programName: String = ""): String

    /**
     * Create the full help string.
     *
     * @param prolog Text to display before any parameter information
     * @param epilog Text to display after any parameter information
     * @param parameters Information about the command's parameters
     * @param programName The name of the currently executing program
     */
    public fun formatHelp(
        prolog: String,
        epilog: String,
        parameters: List<ParameterHelp>,
        programName: String = ""
    ): String

    public sealed class ParameterHelp {
        /**
         * @param names The names that can be used to invoke this option
         * @param secondaryNames Secondary names that can be used to e.g. disable the option
         * @param metavar The metavar to display for the option if it takes values
         * @param help The option's description
         * @param nvalues The number of values that this option takes
         * @param tags Any extra tags to display with the help message for this option
         * @param groupName The name of the group this option belongs to, if there is one and its name should be shown in the help message
         */
        public data class Option(
            val names: Set<String>,
            val secondaryNames: Set<String>,
            val metavar: String?,
            val help: String,
            val nvalues: Int,
            val tags: Map<String, String>,
            val groupName: String?,
        ) : ParameterHelp()

        /**
         * @param name The name / metavar for this argument
         * @param help The arguments's description
         * @param required True if this argument must be specified
         * @param repeatable True if this argument takes an unlimited number of values
         */
        public data class Argument(
            val name: String,
            val help: String,
            val required: Boolean,
            val repeatable: Boolean,
            val tags: Map<String, String>,
        ) : ParameterHelp()

        /**
         * @param name The name for this command
         * @param help The command's description
         */
        public data class Subcommand(
            val name: String,
            val help: String,
            val tags: Map<String, String>,
        ) : ParameterHelp()

        /**
         * Help for an option group. If the group doesn't have a name or help, you don't need to
         * create an instance of this class for it.
         *
         * @property name The group name
         * @property help The help text for this group
         */
        public data class Group(
            val name: String,
            val help: String,
        ) : ParameterHelp()
    }

    /** Standard tag names for parameter help */
    public object Tags {
        /** A value that can be displayed to the user as the default for this option, or null if there is no default. */
        public const val DEFAULT: String = "default"

        /** If true, this option is required. Only used for help output. */
        public const val REQUIRED: String = "required"
    }
}