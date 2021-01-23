/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.packet.chat

import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.internal.contact.takeSingleContent
import net.mamoe.mirai.internal.contact.uin
import net.mamoe.mirai.internal.message.OnlineMessageSourceToGroupImpl
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.internal.network.protocol.packet.chat.receive.MessageSvcPbSendMsg
import net.mamoe.mirai.internal.network.protocol.packet.chat.receive.createToGroup
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSourceKind
import net.mamoe.mirai.message.data.MusicShare
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

internal object SendMessageMultiProtocol {
    inline fun createToGroup(
        client: QQAndroidClient,
        group: Group,
        message: MessageChain,
        fragmented: Boolean,
        crossinline sourceCallback: (OnlineMessageSourceToGroupImpl) -> Unit
    ): List<OutgoingPacket> {
        contract { callsInPlace(sourceCallback, InvocationKind.AT_MOST_ONCE) }

        message.takeSingleContent<MusicShare>()?.let { musicShare ->
            return listOf(MusicSharePacket(client, musicShare, group.uin, targetKind = MessageSourceKind.GROUP))
        }

        return MessageSvcPbSendMsg.createToGroup(client, group, message, fragmented, sourceCallback)
    }
}