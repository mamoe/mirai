/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:OptIn(MiraiInternalApi::class)

package net.mamoe.mirai.event.events

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Stranger
import net.mamoe.mirai.event.AbstractEvent
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.utils.MiraiInternalApi

/**
 * 新增陌生人的事件
 *
 */
public data class StrangerAddEvent @MiraiInternalApi public constructor(
    /**
     * 新的陌生人. 已经添加到 [Bot.strangers]
     */
    public override val stranger: Stranger,
) : StrangerEvent, Packet, AbstractEvent()


/**
 * 陌生人关系改变事件
 *
 */
public sealed class StrangerRelationChangeEvent(
    public override val stranger: Stranger,
) : StrangerEvent, Packet, AbstractEvent() {
    /**
     * 主动删除陌生人或陌生人被删除的事件, 不一定能接收到被动删除的事件
     */
    public class Deleted(
        /**
         * 被删除的陌生人
         */
        stranger: Stranger,
    ) : StrangerRelationChangeEvent(stranger)

    /**
     * 与陌生人成为好友
     */
    public class Friended(
        /**
         * 成为好友的陌生人
         *
         * 成为好友后该陌生人会从陌生人列表中删除
         */
        public override val stranger: Stranger,
        /**
         * 成为好友后的实例
         *
         * 已经添加到Bot的好友列表中
         */
        public val friend: Friend,
    ) : StrangerRelationChangeEvent(stranger)

}