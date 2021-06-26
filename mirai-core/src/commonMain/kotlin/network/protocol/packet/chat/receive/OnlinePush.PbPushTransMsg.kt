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
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.components.NoticeProcessorPipeline.Companion.noticeProcessorPipeline
import net.mamoe.mirai.internal.network.protocol.data.proto.OnlinePushTrans
import net.mamoe.mirai.internal.network.protocol.packet.IncomingPacketFactory
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.internal.network.protocol.packet.buildResponseUniPacket
import net.mamoe.mirai.internal.utils.io.serialization.readProtoBuf


internal object OnlinePushPbPushTransMsg :
    IncomingPacketFactory<Packet?>("OnlinePush.PbPushTransMsg", "OnlinePush.RespPush") {


    override suspend fun ByteReadPacket.decode(bot: QQAndroidBot, sequenceId: Int): Packet? {
        val content = this.readProtoBuf(OnlinePushTrans.PbMsgInfo.serializer())

        if (!bot.client.syncingController.pbPushTransMsgCacheList.addCache(
                content.run {
                    QQAndroidClient.MessageSvcSyncData.PbPushTransMsgSyncId(msgUid, msgSeq, msgTime)
                }
            )
        ) {
            return null
        }
        // bot.network.logger.debug { content._miraiContentToString() }


        bot.components.noticeProcessorPipeline.process(bot, content)
        return null
    }

    override suspend fun QQAndroidBot.handle(packet: Packet?, sequenceId: Int): OutgoingPacket {
        return buildResponseUniPacket(client, sequenceId = sequenceId)
    }

}
