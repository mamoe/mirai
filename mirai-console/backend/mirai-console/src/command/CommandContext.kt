/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.command

import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.utils.NotStableForInheritance

/**
 * 指令执行环境
 * @since 2.12
 */
@NotStableForInheritance
public interface CommandContext {
    /**
     * 指令发送者
     */
    public val sender: CommandSender

    /**
     * 触发指令的原消息链，包含元数据，也包含指令名。
     *
     * 示例内容：`messageChainOf(MessageSource(...), PlainText("/test"), PlainText("arg1"))`
     */
    public val originalMessage: MessageChain
}

internal class CommandContextImpl(
    override val sender: CommandSender,
    override val originalMessage: MessageChain
) : CommandContext