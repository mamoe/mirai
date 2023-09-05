/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */
package net.mamoe.mirai.message.action

import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.Bot
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.event.events.NudgeEvent
import net.mamoe.mirai.message.data.PokeMessage
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.BotConfiguration.MiraiProtocol
import kotlin.jvm.JvmStatic
import kotlin.jvm.JvmSynthetic

/**
 * 一个 "戳一戳" 动作.
 *
 * 备注: 这类似微信拍一拍. 消息对话框中显示的 "一个手指" 的戳一戳是 [PokeMessage]
 *
 * 仅在手机 QQ 8.4.0 左右版本才受支持. 其他客户端会忽略这些消息.
 *
 * 示例，要机器人戳一个群员并发送到群里，使用 `member.nudge().sendTo(group)`.
 * 要机器人戳一个好友并发送给该好友，使用 `friend.nudge().sendTo(friend)`.
 *
 * @see UserOrBot.nudge 创建 [Nudge] 对象
 */
public sealed class Nudge {
    /**
     * 戳的对象. 即 "A 戳了 B" 中的 "B".
     */
    public abstract val target: UserOrBot

    /**
     * 发送戳一戳消息到 [receiver].
     *
     * 需要使用支持的[协议][BotConfiguration.protocol] [MiraiProtocol.ANDROID_PHONE], [ANDROID_PAD 协议][MiraiProtocol.ANDROID_PAD] 或 [MiraiProtocol.IPAD].
     * + 自 2.10 起才支持使用 IPAD 协议发送.
     * + 自 2.16.0-RC 起才支持使用 PAD 协议发送.
     *
     * @param receiver 这条 "戳一戳" 消息的接收对象. (不是 "戳" 动作的对象, 而是接收 "A 戳了 B" 这条消息的对象)
     * @return 成功发送时为 `true`. 若对方禁用 "戳一戳" 功能, 返回 `false`.
     * @throws UnsupportedOperationException 当未使用 [ANDROID_PHONE 协议][MiraiProtocol.ANDROID_PHONE], [ANDROID_PAD 协议][MiraiProtocol.ANDROID_PAD] 或 [IPAD 协议][MiraiProtocol.IPAD] 时抛出
     *
     * @see NudgeEvent 事件
     * @see Contact.sendNudge
     */
    @JvmBlockingBridge
    public suspend fun sendTo(receiver: Contact): Boolean {
        @Suppress("DEPRECATION_ERROR")
        return Mirai.sendNudge(receiver.bot, this, receiver)
    }

    public companion object {
        /**
         * 发送戳一戳消息.
         *
         * 需要使用支持的[协议][BotConfiguration.protocol] [MiraiProtocol.ANDROID_PHONE], [ANDROID_PAD 协议][MiraiProtocol.ANDROID_PAD] 或 [MiraiProtocol.IPAD].
         * + 自 2.10 起才支持使用 IPAD 协议发送.
         * + 自 2.16.0-RC 起才支持使用 PAD 协议发送.
         *
         * @return 成功发送时为 `true`. 若对方禁用 "戳一戳" 功能或今日 "戳一戳" 次数已达到上限, 返回 `false`.
         *
         * @throws UnsupportedOperationException 当未使用 [ANDROID_PHONE 协议][MiraiProtocol.ANDROID_PHONE], [ANDROID_PAD 协议][MiraiProtocol.ANDROID_PAD] 或 [IPAD 协议][MiraiProtocol.IPAD] 时抛出
         *
         * @see NudgeEvent 事件
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
    public override val target: Stranger
) : UserNudge()
