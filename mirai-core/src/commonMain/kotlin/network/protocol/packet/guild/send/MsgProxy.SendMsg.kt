/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.packet.guild.send

import io.ktor.utils.io.core.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import net.mamoe.mirai.contact.Channel
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.message.image.OfflineGuildImage
import net.mamoe.mirai.internal.message.source.OnlineMessageSourceToChannelImpl
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.components.ClockHolder.Companion.clock
import net.mamoe.mirai.internal.network.protocol.data.proto.Guild
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.network.protocol.data.proto.Oidb0xf62
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacketFactory
import net.mamoe.mirai.internal.network.protocol.packet.buildOutgoingUniPacket
import net.mamoe.mirai.internal.utils.io.serialization.readProtoBuf
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray
import net.mamoe.mirai.internal.utils.io.serialization.writeProtoBuf
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.content
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.math.absoluteValue
import kotlin.random.Random

internal object MsgProxySendMsg : OutgoingPacketFactory<MsgProxySendMsg.Response>("MsgProxy.SendMsg") {
    override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
        val data = readProtoBuf(Oidb0xf62.RspBody.serializer())
        if (null != data.result && data.result?.toInt() != 0) {
            return Response.Failed(
                resultCode = data.result,
                resultType = data.errType,
                message = data.errmsg.decodeToString()
            )
        }
        return Response.Success(data)
    }

    sealed class Response : Packet {

        class Success(
            val origin: Oidb0xf62.RspBody
        ) : Response() {
            override fun toString(): String = "Response($origin)"
        }

        data class Failed(
            val resultCode: Short?,
            val resultType: Short?,
            val message: String
        ) : Response() {
            override fun toString(): String {
                return "Failed(resultCode=$resultCode, resultType=$resultType, message='$message')"
            }
        }
    }

    operator fun invoke(
        client: QQAndroidClient,
        channel: Channel,
        message: MessageChain
    ) = buildOutgoingUniPacket(client) {
        val routingHead = Guild.ChannelRoutingHead(
            channelId = channel.id,
            guildId = channel.guildId,
            fromTinyId = client.account.tinyId,
            directMessageFlag = 0
        )

        val random = Random.nextInt()
        val msgUid = random or (1 shl 56)

        val head = Guild.ChannelContentHead(type = 3840, random = msgUid.toLong())
        val channelMsgHead = Guild.ChannelMsgHead(contentHead = head, routingHead = routingHead)

        val messageBody = Guild.MessageBody()
        val rich = ImMsgBody.RichText()

        for (item in message) {
            if (item is PlainText) {
                rich.elems.add(ImMsgBody.Elem(text = ImMsgBody.Text(str = item.content)))
            } else if (item is Image) {
                if (item is OfflineGuildImage) {
                    rich.elems.add(
                        ImMsgBody.Elem(
                            customFace = ImMsgBody.CustomFace(
                                serverIp = item.serverIp,
                                serverPort = item.serverPort,
                                fileId = item.fileId ?: 0,
                                filePath = item.imageId,
                                picMd5 = item.md5,
                                imageType = 1000,
                                width = item.width,
                                height = item.height,
                                size = item.size.toInt(),
                                source = 200,
                                useful = 1,
                                fileType = 1000,
                                bizType = 0,
                                origin = 0,
                                showLen = 0,
                                downloadLen = 0,
                                thumbHeight = item.height * 10 / 3,
                                thumbWidth = item.width * 10 / 3,
                                flag = byteArrayOf(4),
                                pbReserve = Guild.PbReserve(0, Guild.DownloadIndex(item.downloadIndex))
                                    .toByteArray(Guild.PbReserve.serializer())
                            )
                        )
                    )
                }
            } else {
                rich.elems.add(ImMsgBody.Elem(text = ImMsgBody.Text(str = item.content)))
            }
        }

        messageBody.richText = rich

        val msg = Guild.ChannelMsgContent(head = channelMsgHead, body = messageBody, ctrlHead = null, extInfo = null)

        writeProtoBuf(
            Oidb0xf62.RsqBody.serializer(),
            Oidb0xf62.RsqBody(
                msg = msg
            )
        )
    }
}

internal inline fun MsgProxySendMsg.createToChannel(
    client: QQAndroidClient,
    channel: Channel,
    message: MessageChain,
    originalMessage: MessageChain,
    fragmented: Boolean,
    crossinline sourceCallback: (Deferred<OnlineMessageSourceToChannelImpl>) -> Unit,
): List<OutgoingPacket> {
    contract {
        callsInPlace(sourceCallback, InvocationKind.EXACTLY_ONCE)
    }
    val source = OnlineMessageSourceToChannelImpl(
        channel,
        internalIds = intArrayOf(Random.nextInt().absoluteValue),
        sender = client.bot,
        target = channel,
        time = client.bot.clock.server.currentTimeSeconds().toInt(),
        originalMessage = originalMessage,
    )
    sourceCallback(CompletableDeferred(source))
    return listOf(MsgProxySendMsg(client, channel, message))
}