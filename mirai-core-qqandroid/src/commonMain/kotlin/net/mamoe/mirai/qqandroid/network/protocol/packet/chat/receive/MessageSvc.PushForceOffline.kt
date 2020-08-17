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
import net.mamoe.mirai.event.events.BotOfflineEvent
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.network.protocol.data.jce.RequestPushForceOffline
import net.mamoe.mirai.qqandroid.network.protocol.packet.OutgoingPacketFactory
import net.mamoe.mirai.qqandroid.utils.io.serialization.readUniPacket


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
