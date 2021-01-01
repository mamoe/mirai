/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */
package net.mamoe.mirai.message.action

import net.mamoe.kjbb.JvmBlockingBridge
import net.mamoe.mirai.Bot
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.event.events.BotNudgedEvent
import net.mamoe.mirai.event.events.MemberNudgedEvent
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.BotConfiguration.MiraiProtocol

/**
 * 一个 "戳一戳" 消息.
 *
 * 仅在手机 QQ 8.4.0 左右版本才受支持. 其他客户端会忽略这些消息.
 *
 * @see User.nudge 创建 [Nudge] 对象
 * @see Bot.nudge 创建 [Nudge] 对象
 */
public sealed class Nudge {
    /**
     * 戳的对象. 即 "A 戳了 B" 中的 "B".
     */
    public abstract val target: UserOrBot

    /**
     * 发送戳一戳该成员的消息.
     *
     * 需要 [使用协议][BotConfiguration.protocol] [MiraiProtocol.ANDROID_PHONE].
     *
     * @param receiver 这条 "戳一戳" 消息的接收对象. (不是 "戳" 动作的对象, 而是接收 "A 戳了 B" 这条消息的对象)
     * @return 成功发送时为 `true`. 若对方禁用 "戳一戳" 功能, 返回 `false`.
     * @throws UnsupportedOperationException 当未使用 [安卓协议][MiraiProtocol.ANDROID_PHONE] 时抛出
     *
     * @see MemberNudgedEvent 成员被戳事件
     * @see BotNudgedEvent [Bot] 被戳事件
     * @see Contact.sendNudge
     */
    @JvmBlockingBridge
    public suspend fun sendTo(receiver: Contact): Boolean {
        @Suppress("DEPRECATION_ERROR")
        return Mirai.sendNudge(receiver.bot, this, receiver)
    }

    public companion object {
        /**
         * 发送戳一戳该成员的消息.
         *
         * 需要 [使用协议][BotConfiguration.protocol] [MiraiProtocol.ANDROID_PHONE].
         *
         * @return 成功发送时为 `true`. 若对方禁用 "戳一戳" 功能, 返回 `false`.
         *
         * @throws UnsupportedOperationException 当未使用 [安卓协议][MiraiProtocol.ANDROID_PHONE] 时抛出
         *
         * @see MemberNudgedEvent 成员被戳事件
         * @see BotNudgedEvent [Bot] 被戳事件
         */
        @JvmSynthetic
        @JvmStatic
        public suspend fun Contact.sendNudge(nudge: Nudge): Boolean = nudge.sendTo(this)
    }
}

/**
 * @see Bot.nudge
 * @see Nudge
 */
public data class BotNudge(
    public override val target: Bot
) : Nudge()

/**
 * @see User.nudge
 * @see Nudge
 */
public sealed class UserNudge : Nudge() {
    public abstract override val target: UserOrBot
}

/**
 * @see Member.nudge
 * @see Nudge
 */
public data class MemberNudge(
    public override val target: NormalMember
) : UserNudge()

/**
 * @see Friend.nudge
 * @see Nudge
 */
public data class FriendNudge(
    public override val target: Friend
) : UserNudge()

/**
 * @see Stranger.nudge
 * @see Nudge
 */
public data class StrangerNudge(
    public override val target: UserOrBot
) : UserNudge()