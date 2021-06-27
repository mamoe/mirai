/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.packet.chat.receive

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.components.NoticeProcessorPipeline.Companion.noticeProcessorPipeline
import net.mamoe.mirai.internal.network.components.PipelineContext.Companion.KEY_FROM_SYNC
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgOnlinePush
import net.mamoe.mirai.internal.network.protocol.packet.IncomingPacketFactory
import net.mamoe.mirai.internal.network.toPacket
import net.mamoe.mirai.internal.utils.io.serialization.readProtoBuf

internal object PbC2CMsgSync : IncomingPacketFactory<Packet>(
    "OnlinePush.PbC2CMsgSync", ""
) {
    override suspend fun ByteReadPacket.decode(bot: QQAndroidBot, sequenceId: Int): Packet {
        return bot.components.noticeProcessorPipeline.process(
            bot = bot,
            data = readProtoBuf(MsgOnlinePush.PbPushMsg.serializer()).msg,
            attributes = KEY_FROM_SYNC to true,
        ).toPacket()
    }
}