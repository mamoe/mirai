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
import net.mamoe.mirai.internal.network.components.AccountSecretsManager
import net.mamoe.mirai.internal.network.components.BotInitProcessor
import net.mamoe.mirai.internal.network.impl.ForceOfflineException
import net.mamoe.mirai.internal.network.protocol.data.jce.RequestPushForceOffline
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacketFactory
import net.mamoe.mirai.internal.utils.io.serialization.readUniPacket


/**
 * 被挤下线
 */
internal object MessageSvcPushForceOffline :
    OutgoingPacketFactory<RequestPushForceOffline>("MessageSvc.PushForceOffline") {

    override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): RequestPushForceOffline {
        return readUniPacket(RequestPushForceOffline.serializer())
    }

    override suspend fun QQAndroidBot.handle(packet: RequestPushForceOffline) {
        components[AccountSecretsManager].invalidate() // otherwise you receive `MessageSvc.PushForceOffline` again just after logging in.
        components[BotInitProcessor].setLoginHalted() // so that BotInitProcessor will be run on successful reconnection.
        network.close(ForceOfflineException(packet.title, packet.tips))
    }
}
