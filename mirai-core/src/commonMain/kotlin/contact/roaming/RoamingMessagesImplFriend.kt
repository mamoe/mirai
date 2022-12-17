/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.contact.roaming

import net.mamoe.mirai.internal.contact.FriendImpl
import net.mamoe.mirai.internal.network.protocol.packet.chat.receive.MessageSvcPbGetRoamMsgReq
import net.mamoe.mirai.utils.check

internal class RoamingMessagesImplFriend(
    override val contact: FriendImpl
) : TimeBasedRoamingMessagesImpl() {
    override suspend fun requestRoamMsg(
        timeStart: Long,
        lastMessageTime: Long,
        random: Long
    ): MessageSvcPbGetRoamMsgReq.Response {
        return contact.bot.network.sendAndExpect(
            MessageSvcPbGetRoamMsgReq.createForFriend(
                client = contact.bot.client,
                uin = contact.id,
                timeStart = timeStart,
                lastMsgTime = lastMessageTime,
                random = random,
                maxCount = 1000,
                sig = byteArrayOf(),
                pwd = byteArrayOf()
            )
        ).value.check()
    }
}