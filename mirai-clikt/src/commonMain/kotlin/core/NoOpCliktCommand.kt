package net.mamoe.mirai.clikt.core

/**
 * A [CliktCommand] that has a default implementation of [CliktCommand.run] that is a no-op.
 */
public open class NoOpCliktCommand(
    help: String = "",
    epilog: String = "",
    name: String? = null,
    invokeWithoutSubcommand: Boolean = false,
    printHelpOnEmptyArgs: Boolean = false,
    helpTags: Map<String, String> = emptyMap(),
    allowMultipleSubcommands: Boolean = false,
) : CliktCommand(
    help,
    epilog,
    name,
    invokeWithoutSubcommand,
    printHelpOnEmptyArgs,
    helpTags,
    allowMultipleSubcommands
) {
    override suspend fun run(): Unit = Unit
}