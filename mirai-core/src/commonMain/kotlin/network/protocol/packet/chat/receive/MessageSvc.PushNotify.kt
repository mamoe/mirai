/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.packet.chat.receive

import io.ktor.utils.io.core.*
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.components.SyncController.Companion.syncController
import net.mamoe.mirai.internal.network.protocol.data.jce.RequestPushNotify
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgSvc
import net.mamoe.mirai.internal.network.protocol.packet.IncomingPacketFactory
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.internal.utils.io.serialization.readUniPacket


/**
 * 告知要刷新好友消息
 */
internal object MessageSvcPushNotify : IncomingPacketFactory<RequestPushNotify>("MessageSvc.PushNotify") {
    override suspend fun ByteReadPacket.decode(bot: QQAndroidBot, sequenceId: Int): RequestPushNotify {
        discardExact(4) // don't remove
        return readUniPacket(RequestPushNotify.serializer())
    }

    override suspend fun QQAndroidBot.handle(packet: RequestPushNotify, sequenceId: Int): OutgoingPacket {
        while (true) {
            val firstNotify = syncController.firstNotify

            val cookie = if (firstNotify) {
                if (!syncController.casFirstNotify(firstNotify, false)) {
                    continue
                }
                null
            } else {
                packet.vNotifyCookie
            }

            return MessageSvcPbGetMsg(
                client,
                MsgSvc.SyncFlag.START,
                cookie,
            )
        }
    }
}