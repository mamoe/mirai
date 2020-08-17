/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.network.protocol.packet.chat.receive

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.network.QQAndroidClient
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.MsgSvc
import net.mamoe.mirai.qqandroid.network.protocol.packet.OutgoingPacketFactory
import net.mamoe.mirai.qqandroid.network.protocol.packet.buildOutgoingUniPacket
import net.mamoe.mirai.qqandroid.utils.io.serialization.writeProtoBuf

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

    internal suspend fun delete(bot: QQAndroidBot, messages: Flow<MsgComm.Msg>) =
        bot.network.run {

            val map = messages.map {
                MsgSvc.PbDeleteMsgReq.MsgItem(
                    fromUin = it.msgHead.fromUin,
                    toUin = it.msgHead.toUin,
                    // 群为84、好友为187。群通过其他方法删除，但测试结果显示通过187也能删除群消息。
                    msgType = 187,
                    msgSeq = it.msgHead.msgSeq,
                    msgUid = it.msgHead.msgUid
                )
            }.toList()

            MessageSvcPbDeleteMsg(bot.client, map).sendWithoutExpect()
        }

    override suspend fun ByteReadPacket.decode(bot: QQAndroidBot) = null
}