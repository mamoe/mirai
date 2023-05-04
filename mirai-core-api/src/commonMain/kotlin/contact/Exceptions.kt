/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.contact

import net.mamoe.mirai.message.data.AtAll
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.messageChainOf
import net.mamoe.mirai.utils.DeprecatedSinceMirai
import net.mamoe.mirai.utils.MiraiInternalApi
import net.mamoe.mirai.utils.millisToHumanReadableString

/**
 * 发送消息时消息过长抛出的异常.
 *
 * @see Contact.sendMessage
 */
@OptIn(MiraiInternalApi::class)
public class MessageTooLargeException(
    public override val target: Contact,
    /**
     * 原发送消息
     */
    originalMessage: Message,
    /**
     * 经过事件拦截处理后的消息
     */
    public val messageAfterEvent: Message,
    exceptionMessage: String
) : SendMessageFailedException(target, Reason.MESSAGE_TOO_LARGE, originalMessage) {
    override val message: String = exceptionMessage
}

/**
 * 发送消息时 bot 正处于被禁言状态时抛出的异常.
 *
 * @see Group.sendMessage
 */
@OptIn(MiraiInternalApi::class)
public class BotIsBeingMutedException @MiraiInternalApi constructor(
    // this constructor is since 2.9.0-RC
    public override val target: Group,
    originalMessage: Message,
) : SendMessageFailedException(target, Reason.BOT_MUTED, originalMessage) {
    @DeprecatedSinceMirai(warningSince = "2.9", errorSince = "2.11", hiddenSince = "2.12")
    @Deprecated(
        "Deprecated without replacement. Please consider copy this exception to your code.",
        level = DeprecationLevel.HIDDEN
    )
    // this constructor is since 2.0
    public constructor(
        target: Group,
    ) : this(target, messageChainOf())

    override val message: String = "bot is being muted, remaining ${
        target.botMuteRemaining.times(1000).millisToHumanReadableString()
    } seconds"
}

public inline val BotIsBeingMutedException.botMuteRemaining: Int get() = target.botMuteRemaining

/**
 * 发送消息失败时抛出的异常
 *
 * @since 2.9.0
 */
public open class SendMessageFailedException @MiraiInternalApi constructor(
    public open val target: Contact,
    public val reason: Reason,
    public val originalMessage: Message,
    /**
     * @since 2.14
     */
    tips: String? = null,
) : RuntimeException(
    "Failed sending message to $target, reason=$reason. Tips: $tips"
) {
    public enum class Reason {
        /**
         * 消息过长
         */
        MESSAGE_TOO_LARGE,

        /**
         * 机器人被禁言
         */
        BOT_MUTED,

        /**
         * 达到群每分钟发言次数限制
         */
        GROUP_CHAT_LIMITED,

        /**
         * 达到每日发送 [AtAll] 的次数限制
         * @since 2.11
         */
        AT_ALL_LIMITED,

        /**
         * 被服务器限制发送消息, 可能是由冻结引起.
         * @since 2.14
         */
        LIMITED_MESSAGING,
    }
}