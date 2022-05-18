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
import net.mamoe.mirai.internal.network.MultiPacket
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.components.NoticeProcessorPipeline.Companion.processPacketThroughPipeline
import net.mamoe.mirai.internal.network.protocol.data.jce.OnlinePushPack
import net.mamoe.mirai.internal.network.protocol.packet.IncomingPacketFactory
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.internal.network.protocol.packet.buildResponseUniPacket
import net.mamoe.mirai.internal.utils.io.serialization.readUniPacket
import net.mamoe.mirai.internal.utils.io.serialization.writeJceRequestPacket

internal object OnlinePushReqPush : IncomingPacketFactory<OnlinePushReqPush.ReqPushDecoded>(
    "OnlinePush.ReqPush",
    "OnlinePush.RespPush",
) {
    override suspend fun ByteReadPacket.decode(bot: QQAndroidBot, sequenceId: Int): ReqPushDecoded {
        val reqPushMsg = readUniPacket(OnlinePushPack.SvcReqPushMsg.serializer(), "req")
        return ReqPushDecoded(reqPushMsg, bot.processPacketThroughPipeline(reqPushMsg))
    }

    internal class ReqPushDecoded(val request: OnlinePushPack.SvcReqPushMsg, packet: Packet) :
        MultiPacket by MultiPacket(packet), Packet.NoLog {
        override fun toString(): String = "OnlinePush.ReqPush.ReqPushDecoded"
    }

    override suspend fun QQAndroidBot.handle(packet: ReqPushDecoded, sequenceId: Int): OutgoingPacket {
        return buildResponseUniPacket(client) {
            writeJceRequestPacket(
                servantName = "OnlinePush",
                funcName = "SvcRespPushMsg",
                name = "resp",
                serializer = OnlinePushPack.SvcRespPushMsg.serializer(),
                body = OnlinePushPack.SvcRespPushMsg(
                    packet.request.uin,
                    packet.request.vMsgInfos.map { msg ->
                        OnlinePushPack.DelMsgInfo(
                            fromUin = msg.lFromUin,
                            shMsgSeq = msg.shMsgSeq,
                            vMsgCookies = msg.vMsgCookies,
                            uMsgTime = msg.uMsgTime, // captured 0
                        )
                    },
                ),
            )
        }
    }
}