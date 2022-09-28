/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.packet.guild.receive

import io.ktor.utils.io.core.*
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.components.NoticeProcessorPipeline.Companion.processPacketThroughPipeline
import net.mamoe.mirai.internal.network.components.SsoProcessor
import net.mamoe.mirai.internal.network.protocol.data.proto.GuildMsg
import net.mamoe.mirai.internal.network.protocol.packet.IncomingPacketFactory
import net.mamoe.mirai.internal.utils.Lz4
import net.mamoe.mirai.internal.utils.io.serialization.readProtoBuf
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray

internal object MsgPushPushGroupProMsg : IncomingPacketFactory<Packet?>("MsgPush.PushGroupProMsg") {
    override suspend fun ByteReadPacket.decode(bot: QQAndroidBot, sequenceId: Int): Packet? {
        if (!bot.components[SsoProcessor].firstLoginSucceed) return null
        val data = readProtoBuf(GuildMsg.MsgOnlinePush.serializer())


        if (data.compressFlag.toInt() == 1) {
            val decode = Lz4.decode(data.compressMsg)
            if (null != decode) {
                val pressMsgByteArray = ByteReadPacket(decode)
                val pressMsg = pressMsgByteArray.readProtoBuf(GuildMsg.PressMsg.serializer())
                return bot.processPacketThroughPipeline(pressMsg)
            }
            return null
        } else {
            if (data.msgs.first().head?.routingHead?.directMessageFlag?.toInt() == 1) {
                return bot.processPacketThroughPipeline(data)
            }
            val pressMsgByteArray = ByteReadPacket(data.toByteArray(GuildMsg.MsgOnlinePush.serializer()))
            val pressMsg = pressMsgByteArray.readProtoBuf(GuildMsg.PressMsg.serializer())
            return bot.processPacketThroughPipeline(pressMsg)
        }
    }
}