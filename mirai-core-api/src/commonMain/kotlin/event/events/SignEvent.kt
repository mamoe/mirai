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

package net.mamoe.mirai.event.events

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.event.AbstractEvent
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.utils.MiraiInternalApi
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName


/**
 * 打卡事件
 * @property user 打卡发起人
 * @property sign 打卡标记
 * @property hasRank 有排名的打卡，通常是前三名
 */
public class SignEvent @MiraiInternalApi constructor(
    public val user: UserOrBot,
    public val sign: String,
    public val hasRank: Boolean
) : AbstractEvent(), BotEvent, Packet {
    override val bot: Bot get() = user.bot
}