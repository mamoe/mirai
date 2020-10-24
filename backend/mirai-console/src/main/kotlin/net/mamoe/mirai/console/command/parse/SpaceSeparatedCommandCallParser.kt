package net.mamoe.mirai.console.command.parse

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.extensions.CommandCallParserProvider
import net.mamoe.mirai.console.internal.command.flattenCommandComponents
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageContent
import net.mamoe.mirai.message.data.content

@ConsoleExperimentalApi
@ExperimentalCommandDescriptors
public object SpaceSeparatedCommandCallParser : CommandCallParser {
    override fun parse(caller: CommandSender, message: MessageChain): CommandCall? {
        val flatten = message.flattenCommandComponents().filterIsInstance<MessageContent>()
        if (flatten.isEmpty()) return null
        return CommandCallImpl(
            caller = caller,
            calleeName = flatten.first().content,
            valueArguments = flatten.drop(1).map(::DefaultCommandValueArgument)
        )
    }

    public object Provider : CommandCallParserProvider(SpaceSeparatedCommandCallParser)
}