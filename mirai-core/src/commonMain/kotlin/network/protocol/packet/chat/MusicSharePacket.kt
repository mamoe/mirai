package net.mamoe.mirai.internal.network.protocol.packet.chat

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.readBytes
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.protocol.data.proto.*
import net.mamoe.mirai.internal.network.protocol.data.proto.Cmd0xb77
import net.mamoe.mirai.internal.network.protocol.data.proto.FavoriteCKVData
import net.mamoe.mirai.internal.network.protocol.data.proto.OidbSso
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacketFactory
import net.mamoe.mirai.internal.network.protocol.packet.buildOutgoingUniPacket
import net.mamoe.mirai.internal.utils.io.serialization.loadAs
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray
import net.mamoe.mirai.internal.utils.io.serialization.writeProtoBuf
import kotlin.math.absoluteValue
import kotlin.random.Random


internal object MusicSharePacket : OutgoingPacketFactory<MusicSharePacket.Response>("OidbSvc.0xb77_9") {
    override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
        with(readBytes().loadAs(OidbSso.OIDBSSOPkg.serializer())) {
            return Response(result == 0, result)
        }
    }

    class Response(val success: Boolean, val code: Int) : Packet {
        override fun toString(): String = "MusicShareResponse(success=$success,code=$code)"
    }

    fun friendInvoke(
        client: QQAndroidClient,
        musicType: MusicType,
        msgStyle: Int, // 有播放连接为4, 无播放连接为0
        userId: Long,
        title: String,
        summary: String,
        brief: String,
        url: String,
        pictureUrl: String,
        musicUrl: String
    ): OutgoingPacket {
        return buildOutgoingUniPacket(client) {
            writeProtoBuf(
                OidbSso.OIDBSSOPkg.serializer(),
                OidbSso.OIDBSSOPkg(
                    command = 2935,
                    serviceType = 9,
                    bodybuffer = Cmd0xb77.ReqBody(
                        appId = musicType.appID,
                        appType = 1,
                        msgStyle = msgStyle,
                        clientInfo = Cmd0xb77.ClientInfo(
                            platform = musicType.platform,
                            sdkVersion = musicType.sdkVersion,
                            androidPackageName = musicType.packageName,
                            androidSignature = musicType.signature
                        ),
                        extInfo = Cmd0xb77.ExtInfo(
                            msgSeq = Random.nextLong().absoluteValue
                        ),
                        sendType = 0,
                        recvUin = userId,
                        richMsgBody = Cmd0xb77.RichMsgBody(
                            title = title,
                            summary = summary,
                            brief = brief,
                            url = url,
                            pictureUrl = pictureUrl,
                            musicUrl = musicUrl
                        )
                    ).toByteArray(Cmd0xb77.ReqBody.serializer())
                )
            )
        }
    }

    fun troopInvoke(
        client: QQAndroidClient,
        musicType: MusicType,
        msgStyle: Int, // 有播放连接为4, 无播放连接为0
        groupId: Long,
        title: String,
        summary: String,
        brief: String,
        url: String,
        pictureUrl: String,
        musicUrl: String
    ): OutgoingPacket {
        return buildOutgoingUniPacket(client) {
            writeProtoBuf(
                OidbSso.OIDBSSOPkg.serializer(),
                OidbSso.OIDBSSOPkg(
                    command = 2935,
                    serviceType = 9,
                    bodybuffer = Cmd0xb77.ReqBody(
                        appId = musicType.appID,
                        appType = 1,
                        msgStyle = msgStyle,
                        clientInfo = Cmd0xb77.ClientInfo(
                            platform = musicType.platform,
                            sdkVersion = musicType.sdkVersion,
                            androidPackageName = musicType.packageName,
                            androidSignature = musicType.signature
                        ),
                        extInfo = Cmd0xb77.ExtInfo(
                            msgSeq = Random.nextLong().absoluteValue
                        ),
                        sendType = 1,
                        recvUin = groupId,
                        richMsgBody = Cmd0xb77.RichMsgBody(
                            title = title,
                            summary = summary,
                            brief = brief,
                            url = url,
                            pictureUrl = pictureUrl,
                            musicUrl = musicUrl
                        )
                    ).toByteArray(Cmd0xb77.ReqBody.serializer())
                )
            )
        }
    }

}