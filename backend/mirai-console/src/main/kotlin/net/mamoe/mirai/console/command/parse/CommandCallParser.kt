package net.mamoe.mirai.console.command.parse

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.command.resolve.CommandCallResolver
import net.mamoe.mirai.console.command.resolve.ResolvedCommandCall
import net.mamoe.mirai.console.extensions.CommandCallParserProvider
import net.mamoe.mirai.console.internal.extension.GlobalComponentStorage
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.message.data.MessageChain

/**
 * Lexical and syntactical parser for transforming a [MessageChain] into [CommandCall]
 *
 * @see CommandCallResolver The call resolver for [CommandCall] to become [ResolvedCommandCall]
 * @see CommandCallParserProvider The extension point
 *
 * @see SpaceSeparatedCommandCallParser
 */
@ConsoleExperimentalApi
@ExperimentalCommandDescriptors
public interface CommandCallParser {

    /**
     * Lexically and syntactically parse a [message] into [CommandCall], but performs nothing about resolving a call.
     *
     * @return `null` if unable to parse (i.e. due to syntax errors).
     */
    public fun parse(caller: CommandSender, message: MessageChain): CommandCall?

    public companion object {
        /**
         * Calls [CommandCallParser]s provided by [CommandCallParserProvider] in [GlobalComponentStorage] sequentially,
         * returning the first non-null result, `null` otherwise.
         */
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