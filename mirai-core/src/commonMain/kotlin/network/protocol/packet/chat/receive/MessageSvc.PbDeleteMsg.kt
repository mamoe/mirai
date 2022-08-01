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
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgSvc
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacketFactory
import net.mamoe.mirai.internal.network.protocol.packet.buildOutgoingUniPacket
import net.mamoe.mirai.internal.utils.io.serialization.writeProtoBuf

internal object MessageSvcPbDeleteMsg : OutgoingPacketFactory<Nothing?>("MessageSvc.PbDeleteMsg") {

    internal operator fun invoke(client: QQAndroidClient, items: List<MsgSvc.PbDeleteMsgReq.MsgItem>) =
        buildOutgoingUniPacket(client) {

            writeProtoBuf(
                MsgSvc.PbDeleteMsgReq.serializer(),
                MsgSvc.PbDeleteMsgReq(
                    msgItems = items
                )
            )
        }

    internal suspend fun delete(bot: QQAndroidBot, messages: List<MsgComm.Msg>) {
        val map = messages.map {
            MsgSvc.PbDeleteMsgReq.MsgItem(
                fromUin = it.msgHead.fromUin,
                toUin = it.msgHead.toUin,
                // 群为84、好友为187。群通过其他方法删除，但测试结果显示通过187也能删除群消息。
                msgType = 187,
                msgSeq = it.msgHead.msgSeq,
                msgUid = it.msgHead.msgUid,
            )
        }

        bot.network.sendWithoutExpect(MessageSvcPbDeleteMsg(bot.client, map))
    }


    override suspend fun ByteReadPacket.decode(bot: QQAndroidBot) = null
}