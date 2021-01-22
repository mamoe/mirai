/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.packet.chat

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.protocol.data.proto.OidbCmd0xb77
import net.mamoe.mirai.internal.network.protocol.data.proto.OidbSso
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacketFactory
import net.mamoe.mirai.internal.network.protocol.packet.buildOutgoingUniPacket
import net.mamoe.mirai.internal.utils.io.serialization.readProtoBuf
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray
import net.mamoe.mirai.internal.utils.io.serialization.writeProtoBuf
import net.mamoe.mirai.message.data.MessageSourceKind
import net.mamoe.mirai.message.data.MusicShare
import kotlin.math.absoluteValue
import kotlin.random.Random

internal object MusicSharePacket :
    OutgoingPacketFactory<OidbSso.OIDBSSOPkg>("OidbSvc.0xb77_9") {

    override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): OidbSso.OIDBSSOPkg {
        return readProtoBuf(OidbSso.OIDBSSOPkg.serializer())
    }

    operator fun invoke(
        client: QQAndroidClient,
        musicShare: MusicShare,
        targetUin: Long,
        targetKind: MessageSourceKind
    ) = buildOutgoingUniPacket(client) {
        with(musicShare) {
            val musicType = musicShare.type
            writeProtoBuf(
                OidbSso.OIDBSSOPkg.serializer(),
                OidbSso.OIDBSSOPkg(
                    command = 2935,
                    serviceType = 9,
                    bodybuffer = OidbCmd0xb77.ReqBody(
                        appid = musicType.appID,
                        appType = 1,
                        msgStyle = if (url.isNotBlank()) 4 else 0, // 有播放连接为4, 无播放连接为0
                        clientInfo = OidbCmd0xb77.ClientInfo(
                            platform = musicType.platform,
                            sdkVersion = musicType.sdkVersion,
                            androidPackageName = musicType.packageName,
                            androidSignature = musicType.signature
                        ),
                        extInfo = OidbCmd0xb77.ExtInfo( // TODO: 2021/1/22
                            msgSeq = Random.nextLong().absoluteValue
                        ),
                        sendType = when (targetKind) {
                            MessageSourceKind.FRIEND -> 0
                            MessageSourceKind.GROUP -> 1
                            else -> error("Internal error: Unsupported targetKind $targetKind")
                        },
                        recvUin = targetUin,
                        richMsgBody = OidbCmd0xb77.RichMsgBody(
                            title = title,
                            summary = summary,
                            brief = brief,
                            url = url,
                            pictureUrl = pictureUrl,
                            musicUrl = musicUrl
                        )
                    ).toByteArray(OidbCmd0xb77.ReqBody.serializer())
                )
            )
        }
    }
}