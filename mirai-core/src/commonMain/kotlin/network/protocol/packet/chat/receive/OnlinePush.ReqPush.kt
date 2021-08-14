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
import net.mamoe.mirai.internal.contact.GroupImpl
import net.mamoe.mirai.internal.network.MultiPacket
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.components.NoticeProcessorPipeline.Companion.processPacketThroughPipeline
import net.mamoe.mirai.internal.network.protocol.data.jce.MsgInfo
import net.mamoe.mirai.internal.network.protocol.data.jce.MsgType0x210
import net.mamoe.mirai.internal.network.protocol.data.jce.OnlinePushPack
import net.mamoe.mirai.internal.network.protocol.data.proto.Submsgtype0x122
import net.mamoe.mirai.internal.network.protocol.packet.IncomingPacketFactory
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.internal.network.protocol.packet.buildResponseUniPacket
import net.mamoe.mirai.internal.utils._miraiContentToString
import net.mamoe.mirai.internal.utils.io.serialization.loadAs
import net.mamoe.mirai.internal.utils.io.serialization.readUniPacket
import net.mamoe.mirai.internal.utils.io.serialization.writeJceRequestPacket
import net.mamoe.mirai.utils.debug


//0C 01 B1 89 BE 09 5E 3D 72 A6 00 01 73 68 FC 06 00 00 00 3C
internal object OnlinePushReqPush : IncomingPacketFactory<OnlinePushReqPush.ReqPushDecoded>(
    "OnlinePush.ReqPush",
    "OnlinePush.RespPush",
) {
    override suspend fun ByteReadPacket.decode(bot: QQAndroidBot, sequenceId: Int): ReqPushDecoded {
        val reqPushMsg = readUniPacket(OnlinePushPack.SvcReqPushMsg.serializer(), "req")
        return ReqPushDecoded(reqPushMsg, bot.processPacketThroughPipeline(reqPushMsg))
    }

    internal class ReqPushDecoded(val request: OnlinePushPack.SvcReqPushMsg, packet: Packet) :
        MultiPacket by MultiPacket(packet), Packet.NoLog {
        override fun toString(): String = "OnlinePush.ReqPush.ReqPushDecoded"
    }

    override suspend fun QQAndroidBot.handle(packet: ReqPushDecoded, sequenceId: Int): OutgoingPacket {
        return buildResponseUniPacket(client) {
            writeJceRequestPacket(
                servantName = "OnlinePush",
                funcName = "SvcRespPushMsg",
                name = "resp",
                serializer = OnlinePushPack.SvcRespPushMsg.serializer(),
                body = OnlinePushPack.SvcRespPushMsg(
                    packet.request.uin,
                    packet.request.vMsgInfos.map { msg ->
                        OnlinePushPack.DelMsgInfo(
                            fromUin = msg.lFromUin,
                            shMsgSeq = msg.shMsgSeq,
                            vMsgCookies = msg.vMsgCookies,
                            uMsgTime = msg.uMsgTime, // captured 0
                        )
                    },
                ),
            )
        }
    }
}


internal interface Lambda732 {
    operator fun invoke(pk: ByteReadPacket, group: GroupImpl, bot: QQAndroidBot): Sequence<Packet>
}

internal inline fun lambda732(crossinline block: ByteReadPacket.(GroupImpl, QQAndroidBot) -> Sequence<Packet>): Lambda732 {
    return object : Lambda732 {
        override fun invoke(pk: ByteReadPacket, group: GroupImpl, bot: QQAndroidBot): Sequence<Packet> {
            return block(pk, group, bot)
        }
    }
}

internal object Transformers732 : Map<Int, Lambda732> by mapOf(
    // mute
    0x0c to lambda732 { group: GroupImpl, bot: QQAndroidBot ->
        TODO("removed")
    },

    // anonymous
    0x0e to lambda732 { group: GroupImpl, _: QQAndroidBot ->
        TODO("removed")
    },

    //系统提示
    0x14 to lambda732 { group: GroupImpl, bot: QQAndroidBot ->
        TODO("removed")
    },
    // 传字符串信息
    0x10 to lambda732 { group: GroupImpl, bot: QQAndroidBot ->
        TODO("removed")
        /*
        val dataBytes = readBytes(26)

        when (dataBytes[0].toInt() and 0xFF) {
            59 -> { // TODO 应该在 Transformers528 处理
                val size = readByte().toInt() // orthodox, don't `readUByte`
                if (size < 0) {
                    // java.lang.IllegalStateException: negative array size: -100, remaining bytes=B0 E6 99 90 D8 E8 02 98 06 01
                    // java.lang.IllegalStateException: negative array size: -121, remaining bytes=03 10 D9 F7 A2 93 0D 18 E0 DB E8 CA 0B 32 22 61 34 64 31 34 64 61 64 65 65 38 32 32 34 62 64 32 35 34 65 63 37 62 62 30 33 30 66 61 36 66 61 6D 6A 38 0E 48 00 58 01 70 C8 E8 9B 07 7A AD 02 3C 7B 22 69 63 6F 6E 22 3A 22 71 71 77 61 6C 6C 65 74 5F 63 75 73 74 6F 6D 5F 74 69 70 73 5F 69 64 69 6F 6D 5F 69 63 6F 6E 2E 70 6E 67 22 2C 22 61 6C 74 22 3A 22 22 7D 3E 3C 7B 22 63 6D 64 22 3A 31 2C 22 64 61 74 61 22 3A 22 6C 69 73 74 69 64 3D 31 30 30 30 30 34 35 32 30 31 32 30 30 34 30 38 31 32 30 30 31 30 39 36 31 32 33 31 34 35 30 30 26 67 72 6F 75 70 74 79 70 65 3D 31 22 2C 22 74 65 78 74 43 6F 6C 6F 72 22 3A 22 30 78 38 37 38 42 39 39 22 2C 22 74 65 78 74 22 3A 22 E6 8E A5 E9 BE 99 E7 BA A2 E5 8C 85 E4 B8 8B E4 B8 80 E4 B8 AA E6 8B BC E9 9F B3 EF BC 9A 22 7D 3E 3C 7B 22 63 6D 64 22 3A 31 2C 22 64 61 74 61 22 3A 22 6C 69 73 74 69 64 3D 31 30 30 30 30 34 35 32 30 31 32 30 30 34 30 38 31 32 30 30 31 30 39 36 31 32 33 31 34 35 30 30 26 67 72 6F 75 70 74 79 70 65 3D 31 22 2C 22 74 65 78 74 43 6F 6C 6F 72 22 3A 22 30 78 45 36 32 35 35 35 22 2C 22 74 65 78 74 22 3A 22 64 69 6E 67 22 7D 3E 82 01 0C E8 80 81 E5 83 A7 E5 85 A5 E5 AE 9A 88 01 03 92 01 04 64 69 6E 67 A0 01 00
                    // negative array size: -40, remaining bytes=D6 94 C3 8C D8 E8 02 98 06 01
                    error("negative array size: $size, remaining bytes=${readBytes().toUHexString()}")
                }

                // println(dataBytes.toUHexString())
                //println(message + ":" + dataBytes.toUHexString())

                val new = when (val message = readString(size)) {
                    "管理员已关闭群聊坦白说" -> false
                    "管理员已开启群聊坦白说" -> true
                    else -> {
                        bot.network.logger.debug { "Unknown server messages $message" }
                        return@lambda732 emptySequence()
                    }
                }

                // @Suppress("DEPRECATION")
                // if (group.settings.isConfessTalkEnabled == new) {
                //     return@lambda732 emptySequence()
                // }

                return@lambda732 sequenceOf(
                    GroupAllowConfessTalkEvent(
                        new,
                        false,
                        group,
                        false
                    )
                )
            }

            0x2D -> {
                // 修改群名. 在 Transformers528 0x27L 处理
                return@lambda732 emptySequence()
            }
            else -> {
                /*
                bot.network.logger.debug("unknown Transformer732 0xunknown type: ${dataBytes[0].toString(16)
                    .uppercase()}")
                bot.network.logger.debug("unknown Transformer732 0xdata= ${readBytes().toUHexString()}")
                */
                return@lambda732 emptySequence()

                /*
                if (group.name == message) {
                    return@lambda732 emptySequence()
                }

                return@lambda732 sequenceOf(
                    GroupNameChangeEvent(
                        group.name.also { group._name = message },
                        message, group, false
                    )
                )*/
            }
        }*/
    },

    // recall
    0x11 to lambda732 { group: GroupImpl, bot: QQAndroidBot ->
        TODO("removed")
    },
)

internal interface Lambda528 {
    suspend operator fun invoke(msg: MsgType0x210, bot: QQAndroidBot, msgInfo: MsgInfo): Sequence<Packet>
}

internal inline fun lambda528(crossinline block: suspend MsgType0x210.(QQAndroidBot) -> Sequence<Packet>): Lambda528 {
    return object : Lambda528 {
        override suspend fun invoke(msg: MsgType0x210, bot: QQAndroidBot, msgInfo: MsgInfo): Sequence<Packet> {
            return block(msg, bot)
        }

    }
}

internal inline fun lambda528(crossinline block: suspend MsgType0x210.(QQAndroidBot, MsgInfo) -> Sequence<Packet>): Lambda528 {
    return object : Lambda528 {
        override suspend fun invoke(msg: MsgType0x210, bot: QQAndroidBot, msgInfo: MsgInfo): Sequence<Packet> {
            return block(msg, bot, msgInfo)
        }

    }
}

// uSubMsgType to vProtobuf
// 138 or 139: top_package/akln.java:1568
// 66: top_package/nhz.java:269
/**
 * @see MsgType0x210
 */
@OptIn(ExperimentalStdlibApi::class)
internal object Transformers528 : Map<Long, Lambda528> by mapOf(

    0x8AL to lambda528 { bot ->
        TODO("removed")
    },

    //戳一戳信息等
    0x122L to lambda528 { bot, msgInfo ->
        val body = vProtobuf.loadAs(Submsgtype0x122.Submsgtype0x122.MsgBody.serializer())
        when (body.templId) {
            //戳一戳
            1132L, 1133L, 1134L, 1135L, 1136L, 10043L -> {
                TODO("removed")
            }
            else -> {
                bot.logger.debug {
                    "Unknown Transformers528 0x122L template\ntemplId=${body.templId}\nPermList=${body.msgTemplParam._miraiContentToString()}"
                }
                return@lambda528 emptySequence()
            }
        }
    },
    // 群相关,  ModFriendRemark, DelFriend, ModGroupProfile
    0x27L to lambda528 { bot ->
        TODO("removed")
    },
)
