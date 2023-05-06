/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("BotEventsKt")
@file:OptIn(MiraiInternalApi::class)

package net.mamoe.mirai.event.events

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.event.AbstractEvent
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.utils.MiraiInternalApi
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName


/**
 * 戳一戳事件
 */
public data class NudgeEvent @MiraiInternalApi constructor(
    /**
     * 戳一戳发起人
     */
    public val from: UserOrBot,
    /**
     * 戳一戳目标, 可能与 [from] 相同.
     */
    public val target: UserOrBot,
    /**
     * 消息语境, 同 [MessageEvent.subject]. 可能为 [Group], [Stranger], [Friend], [Member].
     */
    public val subject: Contact,
    public val action: String,
    public val suffix: String,
) : AbstractEvent(), BotEvent, Packet {
    override val bot: Bot get() = from.bot
}