/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.event.events

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.ClientKind
import net.mamoe.mirai.contact.OtherClient
import net.mamoe.mirai.event.AbstractEvent
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.utils.MiraiInternalApi

public interface OtherClientEvent : BotEvent, Packet {
    public val client: OtherClient
    override val bot: Bot
        get() = client.bot
}

/**
 * 其他设备上线
 */
public data class OtherClientOnlineEvent @MiraiInternalApi constructor(
    override val client: OtherClient,
    /**
     * 详细设备类型，通常非 `null`.
     */
    val kind: ClientKind?
) : OtherClientEvent, AbstractEvent()

/**
 * 其他设备离线
 */
public data class OtherClientOfflineEvent(
    override val client: OtherClient,
) : OtherClientEvent, AbstractEvent()