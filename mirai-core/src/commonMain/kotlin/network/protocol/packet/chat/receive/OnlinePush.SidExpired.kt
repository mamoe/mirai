/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.packet.chat.receive

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.protocol.packet.IncomingPacketFactory
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.internal.network.protocol.packet.buildResponseUniPacket
import net.mamoe.mirai.internal.network.protocol.packet.login.StatSvc
import net.mamoe.mirai.internal.network.protocol.packet.login.wtlogin.WtLogin10
import net.mamoe.mirai.internal.network.protocol.packet.sendAndExpect

internal object OnlinePushSidExpired : IncomingPacketFactory<Packet?>("OnlinePush.SidTicketExpired") {

    override suspend fun QQAndroidBot.handle(packet: Packet?, sequenceId: Int): OutgoingPacket {
        return buildResponseUniPacket(
            client,
            sequenceId = sequenceId,
            key = client.wLoginSigInfo.d2Key
        ) {}.also {
            WtLogin10(client, mainSigMap = 3554528).sendAndExpect(bot)
            StatSvc.Register.online(client).sendAndExpect(bot)
        }
    }

    override suspend fun ByteReadPacket.decode(bot: QQAndroidBot, sequenceId: Int): Packet? {
        return null
    }
}
