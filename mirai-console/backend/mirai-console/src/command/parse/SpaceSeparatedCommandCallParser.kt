/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

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

    @ConsoleExperimentalApi
    @ExperimentalCommandDescriptors
    public object Provider : CommandCallParserProvider {
        override val instance: CommandCallParser get() = SpaceSeparatedCommandCallParser
        override val priority: Int get() = -1
    }

    override fun parse(caller: CommandSender, message: MessageChain): CommandCall? {
        val flatten = message.flattenCommandComponents().filterIsInstance<MessageContent>()
        if (flatten.isEmpty()) return null
        return CommandCallImpl(
            caller = caller,
            calleeName = flatten.first().content,
            valueArguments = flatten.drop(1).map(::DefaultCommandValueArgument)
        )
    }
}