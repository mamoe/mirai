package net.mamoe.mirai.clikt.output

@Suppress("MemberVisibilityCanBePrivate")
public open class CliktHelpFormatter(
    protected val localization: Localization = defaultLocalization,
    protected val indent: String = "  ",
    width: Int? = null,
    maxWidth: Int = 78,
    maxColWidth: Int? = null,
    protected val colSpacing: Int = 2,
    protected val requiredOptionMarker: String? = null,
    protected val showDefaultValues: Boolean = false,
    protected val showRequiredTag: Boolean = false,
) : HelpFormatter {
    protected val width: Int = width ?: maxWidth

    protected val maxColWidth: Int = maxColWidth ?: (this.width / 2.5).toInt()

    override fun formatUsage(parameters: List<HelpFormatter.ParameterHelp>, programName: String): String {
        return buildString { this.addUsage(parameters, programName) }
    }

    override fun formatHelp(
        prolog: String,
        epilog: String,
        parameters: List<HelpFormatter.ParameterHelp>,
        programName: String,
    ): String = buildString {
        addUsage(parameters, programName)
        addProlog(prolog)
        addOptions(parameters)
        addArguments(parameters)
        addCommands(parameters)
        addEpilog(epilog)
    }

    protected open fun StringBuilder.addUsage(
        parameters: List<HelpFormatter.ParameterHelp>,
        programName: String,
    ) {
        val prog = "${renderSectionTitle(localization.usageTitle())} $programName"
        val usage = buildString {
            if (parameters.any { it is HelpFormatter.ParameterHelp.Option }) {
                append(localization.optionsMetavar())
            }

            parameters.filterIsInstance<HelpFormatter.ParameterHelp.Argument>().forEach {
                append(" ")
                if (!it.required) append("[")
                append(it.name)
                if (!it.required) append("]")
                if (it.repeatable) append("...")
            }

            if (parameters.any { it is HelpFormatter.ParameterHelp.Subcommand }) {
                append(" ").append(localization.commandMetavar())
            }
        }

        if (usage.isEmpty()) {
            append(prog)
        } else if (prog.graphemeLength >= width - 20 && prog.graphemeLength + usage.graphemeLength > width - 2) {
            append(prog).append("\n")
            val usageIndent = " ".repeat(minOf(width / 3, 11))
            usage.wrapText(this, width, usageIndent, usageIndent)
        } else {
            val usageIndent = " ".repeat(prog.length + 1)
            usage.wrapText(this, width, "$prog ", usageIndent)
        }
    }

    protected open fun StringBuilder.addProlog(prolog: String) {
        if (prolog.isNotEmpty()) {
            append("\n\n")
            prolog.wrapText(this, width, initialIndent = "  ", subsequentIndent = "  ")
        }
    }

    protected open fun StringBuilder.addOptions(parameters: List<HelpFormatter.ParameterHelp>) {
        val groupsByName = parameters.filterIsInstance<HelpFormatter.ParameterHelp.Group>().associateBy { it.name }
        parameters.filterIsInstance<HelpFormatter.ParameterHelp.Option>()
            .groupBy { it.groupName }
            .toList()
            .sortedBy { it.first == null }
            .forEach { (title, params) ->
                addOptionGroup(title?.let { "$it:" }
                    ?: localization.optionsTitle(), groupsByName[title]?.help, params)
            }
    }

    protected open fun StringBuilder.addOptionGroup(
        title: String,
        help: String?,
        parameters: List<HelpFormatter.ParameterHelp.Option>,
    ) {
        val options = parameters.map {
            val names = mutableListOf(joinNamesForOption(it.names))
            if (it.secondaryNames.isNotEmpty()) names += joinNamesForOption(it.secondaryNames)
            DefinitionRow(
                col1 = names.joinToString(" / ", postfix = optionMetavar(it)),
                col2 = renderHelpText(it.help, it.tags),
                marker = if (HelpFormatter.Tags.REQUIRED in it.tags) requiredOptionMarker else null
            )
        }
        if (options.isNotEmpty()) {
            append("\n")
            section(title)
            if (help != null) append("\n")
            help?.wrapText(this, width, initialIndent = "  ", subsequentIndent = "  ")
            if (help != null) append("\n\n")
            appendDefinitionList(options)
        }
    }

    protected open fun StringBuilder.addArguments(parameters: List<HelpFormatter.ParameterHelp>) {
        val arguments = parameters.filterIsInstance<HelpFormatter.ParameterHelp.Argument>().map {
            DefinitionRow(renderArgumentName(it.name), renderHelpText(it.help, it.tags))
        }
        if (arguments.isNotEmpty() && arguments.any { it.col2.isNotEmpty() }) {
            append("\n")
            section(localization.argumentsTitle())
            appendDefinitionList(arguments)
        }
    }

    protected open fun StringBuilder.addCommands(parameters: List<HelpFormatter.ParameterHelp>) {
        val commands = parameters.filterIsInstance<HelpFormatter.ParameterHelp.Subcommand>().map {
            DefinitionRow(renderSubcommandName(it.name), renderHelpText(it.help, it.tags))
        }
        if (commands.isNotEmpty()) {
            append("\n")
            section(localization.commandsTitle())
            appendDefinitionList(commands)
        }
    }

    protected open fun StringBuilder.addEpilog(epilog: String) {
        if (epilog.isNotEmpty()) {
            append("\n\n")
            epilog.wrapText(this, width)
        }
    }

    protected open fun renderHelpText(help: String, tags: Map<String, String>): String {
        val renderedTags = tags.asSequence()
            .filter { (k, v) -> shouldShowTag(k, v) }
            .joinToString(" ") { (k, v) -> renderTag(k, v) }
        return if (renderedTags.isEmpty()) help else "$help $renderedTags"

    }

    protected open fun shouldShowTag(tag: String, value: String): Boolean {
        return when (tag) {
            HelpFormatter.Tags.DEFAULT -> showDefaultValues && value.isNotBlank()
            HelpFormatter.Tags.REQUIRED -> showRequiredTag
            else -> true
        }
    }

    protected open fun joinNamesForOption(names: Set<String>): String {
        return names.sortedBy { it.startsWith("--") }.joinToString(", ") { renderOptionName(it) }
    }

    protected open fun renderTag(tag: String, value: String): String {
        val t = when (tag) {
            HelpFormatter.Tags.DEFAULT -> localization.helpTagDefault()
            HelpFormatter.Tags.REQUIRED -> localization.helpTagRequired()
            else -> tag
        }
        return if (value.isBlank()) "($t)" else "($t: $value)"
    }

    protected open fun renderOptionName(name: String): String = name
    protected open fun renderArgumentName(name: String): String = name
    protected open fun renderSubcommandName(name: String): String = name
    protected open fun renderSectionTitle(title: String): String = title

    protected open fun optionMetavar(option: HelpFormatter.ParameterHelp.Option): String {
        if (option.metavar == null) return ""
        val metavar = " " + option.metavar
        if (option.nvalues > 1) return "$metavar..."
        return metavar
    }

    protected fun StringBuilder.appendDefinitionList(rows: List<DefinitionRow>) {
        if (rows.isEmpty()) return
        val firstWidth = measureFirstColumn(rows)
        for ((i, row) in rows.withIndex()) {
            val (col1, col2, marker) = row
            if (i > 0) append("\n")

            val firstIndent = when {
                marker.isNullOrEmpty() -> indent
                else -> marker + indent.drop(marker.graphemeLength).ifEmpty { " " }
            }
            val subsequentIndent = " ".repeat(firstIndent.graphemeLength + firstWidth + colSpacing)

            if (col2.isBlank()) {
                append(firstIndent).append(col1)
            } else {
                val initialIndent = if (col1.graphemeLength > maxColWidth) {
                    // If the first column is too wide, append it and start the second column on a new line
                    append(firstIndent).append(col1).append("\n")
                    subsequentIndent
                } else {
                    // If the first column fits, use it as the initial indent for wrapping
                    buildString {
                        append(firstIndent).append(col1)
                        // Pad the difference between this column's width and the table's first column width
                        repeat(firstWidth - col1.graphemeLength + colSpacing) { append(" ") }
                    }
                }

                col2.wrapText(this, width, initialIndent, subsequentIndent)
            }
        }
    }

    private fun measureFirstColumn(rows: List<DefinitionRow>): Int =
        rows.maxByOrNull { it.col1.graphemeLength }?.col1?.graphemeLength?.coerceAtMost(maxColWidth) ?: maxColWidth

    private fun StringBuilder.section(title: String) {
        append("\n").append(renderSectionTitle(title)).append("\n")
    }

    private val ansiCodeRegex = Regex("${"\u001B"}\\[[^m]*m")

    /** The number of visible characters in a string */
    protected val String.graphemeLength: Int get() = replace(ansiCodeRegex, "").length

    protected data class DefinitionRow(val col1: String, val col2: String, val marker: String? = null)
}