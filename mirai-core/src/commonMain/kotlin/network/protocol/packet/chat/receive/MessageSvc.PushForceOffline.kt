/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.packet.chat.receive

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.event.events.BotOfflineEvent
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.protocol.data.jce.RequestPushForceOffline
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacketFactory
import net.mamoe.mirai.internal.utils.io.serialization.readUniPacket


/**
 * 被挤下线
 */
internal object MessageSvcPushForceOffline :
    OutgoingPacketFactory<BotOfflineEvent.Force>("MessageSvc.PushForceOffline") {
    override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): BotOfflineEvent.Force {
        val struct = this.readUniPacket(RequestPushForceOffline.serializer())
        @Suppress("INVISIBLE_MEMBER")
        return BotOfflineEvent.Force(bot, title = struct.title ?: "", message = struct.tips ?: "")
    }
}
