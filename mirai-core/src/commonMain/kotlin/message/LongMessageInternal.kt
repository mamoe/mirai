/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.message

import io.ktor.client.request.*
import kotlinx.io.core.discardExact
import kotlinx.io.core.readBytes
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.internal.asQQAndroidBot
import net.mamoe.mirai.internal.network.highway.ChannelKind
import net.mamoe.mirai.internal.network.highway.ResourceKind
import net.mamoe.mirai.internal.network.highway.tryDownload
import net.mamoe.mirai.internal.network.highway.tryServersDownload
import net.mamoe.mirai.internal.network.protocol.data.proto.LongMsg
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgTransmit
import net.mamoe.mirai.internal.network.protocol.packet.chat.MultiMsg
import net.mamoe.mirai.internal.network.protocol.packet.sendAndExpect
import net.mamoe.mirai.internal.utils.crypto.TEA
import net.mamoe.mirai.internal.utils.io.serialization.loadAs
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.*

// internal runtime value, not serializable
internal data class LongMessageInternal internal constructor(override val content: String, val resId: String) :
    AbstractServiceMessage(), RefinableMessage {
    override val serviceId: Int get() = 35

    override suspend fun refine(contact: Contact, context: MessageChain): Message {
        val bot = contact.bot.asQQAndroidBot()
        when (val resp = MultiMsg.ApplyDown(bot.client, 1, resId, 1).sendAndExpect(bot)) {
            is MultiMsg.ApplyDown.Response.RequireDownload -> {
                val http = Mirai.Http
                val origin = resp.origin

                val data = if (origin.msgExternInfo?.channelType == 2) {
                    tryDownload(
                        bot = bot,
                        host = "https://ssl.htdata.qq.com",
                        port = 0,
                        resourceKind = ResourceKind.LONG_MESSAGE,
                        channelKind = ChannelKind.HTTP
                    ) { host, port ->
                        http.get<ByteArray>("$host${origin.thumbDownPara}:$port")
                    }
                } else tryServersDownload(
                    bot = bot,
                    servers = origin.uint32DownIp.zip(origin.uint32DownPort),
                    resourceKind = ResourceKind.LONG_MESSAGE,
                    channelKind = ChannelKind.HTTP
                ) { ip, port ->
                    http.get("http://$ip${origin.thumbDownPara}:$port")
                }

                val body = data.read {
                    check(readByte() == 40.toByte()) {
                        "bad data while MultiMsg.ApplyDown: ${data.toUHexString()}"
                    }
                    val headLength = readInt()
                    val bodyLength = readInt()
                    discardExact(headLength)
                    readBytes(bodyLength)
                }

                val decrypted = TEA.decrypt(body, origin.msgKey)
                val longResp =
                    decrypted.loadAs(LongMsg.RspBody.serializer())

                val down = longResp.msgDownRsp.single()
                check(down.result == 0) {
                    "Message download failed, result=${down.result}, resId=${down.msgResid}, msgContent=${down.msgContent.toUHexString()}"
                }

                val content = down.msgContent.ungzip()
                val transmit = content.loadAs(MsgTransmit.PbMultiMsgTransmit.serializer())

                val source = context.source
                return transmit.msg.toMessageChainNoSource(bot.id, contact.castOrNull<Group>()?.id ?: 0, source.kind)
            }
            MultiMsg.ApplyDown.Response.MessageTooLarge -> {
                error("Message is too large and cannot download")
            }
        }
    }

    companion object Key :
        AbstractPolymorphicMessageKey<ServiceMessage, LongMessageInternal>(ServiceMessage, { it.safeCast() })
}

// internal runtime value, not serializable
internal data class ForwardMessageInternal(override val content: String) : AbstractServiceMessage(), RefinableMessage {
    override val serviceId: Int get() = 35

    override suspend fun refine(contact: Contact, context: MessageChain): Message {
        // val bot = contact.bot.asQQAndroidBot()
        // TODO: 2021/2/2 Support forward message refinement
        // https://github.com/mamoe/mirai/issues/623
        return this
    }

    companion object Key :
        AbstractPolymorphicMessageKey<ServiceMessage, ForwardMessageInternal>(ServiceMessage, { it.safeCast() })
}

internal interface RefinableMessage : SingleMessage {

    suspend fun refine(
        contact: Contact,
        context: MessageChain,
    ): Message?
}