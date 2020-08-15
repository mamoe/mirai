/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.network.protocol.packet.chat.receive

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.discardExact
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.network.protocol.data.jce.RequestPushNotify
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.MsgSvc
import net.mamoe.mirai.qqandroid.network.protocol.packet.IncomingPacketFactory
import net.mamoe.mirai.qqandroid.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.qqandroid.utils.io.serialization.readUniPacket
import net.mamoe.mirai.utils.currentTimeSeconds


/**
 * 告知要刷新好友消息
 */
internal object MessageSvcPushNotify : IncomingPacketFactory<RequestPushNotify>("MessageSvc.PushNotify") {
    override suspend fun ByteReadPacket.decode(bot: QQAndroidBot, sequenceId: Int): RequestPushNotify {
        discardExact(4) // don't remove
        return readUniPacket(RequestPushNotify.serializer())
    }

    override suspend fun QQAndroidBot.handle(packet: RequestPushNotify, sequenceId: Int): OutgoingPacket? {

        network.run {
            return MessageSvcPbGetMsg(
                client,
                MsgSvc.SyncFlag.START,
                packet.stMsgInfo?.uMsgTime ?: currentTimeSeconds
            )
        }
    }
}