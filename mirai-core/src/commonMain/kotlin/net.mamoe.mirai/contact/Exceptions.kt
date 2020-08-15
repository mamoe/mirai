/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.contact

import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.utils.asHumanReadable
import kotlin.time.seconds

/**
 * 发送消息时消息过长抛出的异常.
 *
 * @see Contact.sendMessage
 */
public class MessageTooLargeException(
    public val target: Contact,
    /**
     * 原发送消息
     */
    public val originalMessage: Message,
    /**
     * 经过事件拦截处理后的消息
     */
    public val messageAfterEvent: Message,
    exceptionMessage: String
) : RuntimeException(exceptionMessage)

/**
 * 发送消息时 bot 正处于被禁言状态时抛出的异常.
 *
 * @see Group.sendMessage
 */
public class BotIsBeingMutedException(
    public val target: Group
) : RuntimeException("bot is being muted, remaining ${target.botMuteRemaining.seconds.asHumanReadable} seconds")

public inline val BotIsBeingMutedException.botMuteRemaining: Int get() = target.botMuteRemaining