package net.mamoe.mirai.console.command.parse

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.extensions.CommandCallParserProvider
import net.mamoe.mirai.console.internal.extension.GlobalComponentStorage
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.message.data.MessageChain

/**
 * @see CommandCallParserProvider
 */
@ConsoleExperimentalApi
@ExperimentalCommandDescriptors
public interface CommandCallParser {
    public fun parse(sender: CommandSender, message: MessageChain): CommandCall?

    public companion object {
        @JvmStatic
        public fun MessageChain.parseCommandCall(sender: CommandSender): CommandCall? {
            GlobalComponentStorage.run {
                CommandCallParserProvider.useExtensions { provider ->
                    provider.instance.parse(sender, this@parseCommandCall)?.let { return it }
                }
            }
            return null
        }
    }
}