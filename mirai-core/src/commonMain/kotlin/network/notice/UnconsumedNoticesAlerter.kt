/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.notice

import net.mamoe.mirai.internal.message.contextualBugReportException
import net.mamoe.mirai.internal.network.components.MixedNoticeProcessor
import net.mamoe.mirai.internal.network.components.NoticePipelineContext
import net.mamoe.mirai.internal.network.notice.decoders.MsgType0x2DC
import net.mamoe.mirai.internal.network.protocol.data.jce.MsgType0x210
import net.mamoe.mirai.internal.network.protocol.data.jce.RequestPushStatus
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgOnlinePush
import net.mamoe.mirai.internal.network.protocol.data.proto.OnlinePushTrans
import net.mamoe.mirai.internal.network.protocol.data.proto.Structmsg
import net.mamoe.mirai.internal.network.protocol.packet.chat.NewContact
import net.mamoe.mirai.utils.*

internal class UnconsumedNoticesAlerter(
    logger: MiraiLogger,
) : MixedNoticeProcessor() {
    private val logger: MiraiLogger = logger.withSwitch(systemProp("mirai.network.notice.unconsumed.logging", false))

    override suspend fun NoticePipelineContext.processImpl(data: MsgType0x210) {
        if (isConsumed) return
        when (data.uSubMsgType) {
            0x26L, // VIP 进群提示
            0x111L, // 提示共同好友
            0xD4L, // bot 在其他客户端被踢或主动退出而同步情况
            -> {
                // Network(1994701021) 16:03:54 : unknown group 528 type 0x0000000000000026, data: 08 01 12 40 0A 06 08 F4 EF BB 8F 04 10 E7 C1 AD B8 02 18 01 22 2C 10 01 1A 1A 18 B4 DC F8 9B 0C 20 E7 C1 AD B8 02 28 06 30 02 A2 01 04 08 93 D6 03 A8 01 08 20 00 28 00 32 08 18 01 20 FE AF AF F5 05 28 00
            }

            0xE2L -> {
                // unknown

                // 0A 35 08 00 10 A2 FF 8C F0 03 1A 1B E5 90 8C E6 84 8F E4 BD A0 E7 9A 84 E5 8A A0 E5 A5 BD E5 8F 8B E8 AF B7 E6 B1 82 22 0C E6 BD 9C E6 B1 9F E7 BE A4 E5 8F 8B 28 01
                // vProtobuf.loadAs(Msgtype0x210.serializer())
            }
            else -> {
                logger.debug { "Unknown group 528 type 0x${data.uSubMsgType.toUHexString("")}, data: " + data.vProtobuf.toUHexString() }
            }
        }
    }

    override suspend fun NoticePipelineContext.processImpl(data: MsgType0x2DC) {
        if (isConsumed) return
        logger.debug { "Unknown group 732 type ${data.kind}, data: " + data.buf.toUHexString() }
    }

    override suspend fun NoticePipelineContext.processImpl(data: OnlinePushTrans.PbMsgInfo) {
        if (isConsumed) return
        when {
            data.msgType == 529 && data.msgSubtype == 9 -> {
                /*
                PbMsgInfo#1773430973 {
                    fromUin=0x0000000026BA1173(649728371)
                    generalFlag=0x00000001(1)
                    msgData=0A 07 70 72 69 6E 74 65 72 10 02 1A CD 02 0A 1F 53 61 6D 73 75 6E 67 20 4D 4C 2D 31 38 36 30 20 53 65 72 69 65 73 20 28 55 53 42 30 30 31 29 0A 16 4F 6E 65 4E 6F 74 65 20 66 6F 72 20 57 69 6E 64 6F 77 73 20 31 30 0A 19 50 68 61 6E 74 6F 6D 20 50 72 69 6E 74 20 74 6F 20 45 76 65 72 6E 6F 74 65 0A 11 4F 6E 65 4E 6F 74 65 20 28 44 65 73 6B 74 6F 70 29 0A 1D 4D 69 63 72 6F 73 6F 66 74 20 58 50 53 20 44 6F 63 75 6D 65 6E 74 20 57 72 69 74 65 72 0A 16 4D 69 63 72 6F 73 6F 66 74 20 50 72 69 6E 74 20 74 6F 20 50 44 46 0A 15 46 6F 78 69 74 20 50 68 61 6E 74 6F 6D 20 50 72 69 6E 74 65 72 0A 03 46 61 78 32 09 0A 03 6A 70 67 10 01 18 00 32 0A 0A 04 6A 70 65 67 10 01 18 00 32 09 0A 03 70 6E 67 10 01 18 00 32 09 0A 03 67 69 66 10 01 18 00 32 09 0A 03 62 6D 70 10 01 18 00 32 09 0A 03 64 6F 63 10 01 18 01 32 0A 0A 04 64 6F 63 78 10 01 18 01 32 09 0A 03 74 78 74 10 00 18 00 32 09 0A 03 70 64 66 10 01 18 01 32 09 0A 03 70 70 74 10 01 18 01 32 0A 0A 04 70 70 74 78 10 01 18 01 32 09 0A 03 78 6C 73 10 01 18 01 32 0A 0A 04 78 6C 73 78 10 01 18 01
                    msgSeq=0x00001AFF(6911)
                    msgSubtype=0x00000009(9)
                    msgTime=0x5FDF21A3(1608458659)
                    msgType=0x00000211(529)
                    msgUid=0x010000005FDEE04C(72057595646369868)
                    realMsgTime=0x5FDF21A3(1608458659)
                    svrIp=0x3E689409(1047041033)
                    toUin=0x0000000026BA1173(649728371)
                }
                 */
                return
            }
        }
        if (logger.isEnabled && logger.isDebugEnabled) {
            logger.debug(
                contextualBugReportException(
                    "解析 OnlinePush.PbPushTransMsg, msgType=${data.msgType}",
                    data.structureToString(),
                    null,
                    "并描述此时机器人是否被踢出, 或是否有成员列表变更等动作.",
                )
            )
        }
    }

    override suspend fun NoticePipelineContext.processImpl(data: MsgOnlinePush.PbPushMsg) {
        if (isConsumed) return

    }

    override suspend fun NoticePipelineContext.processImpl(data: MsgComm.Msg) {
        if (isConsumed) return
        when (data.msgHead.msgType) {
            732 -> {
                // 732:  27 0B 60 E7 0C 01 3E 03 3F A2 5E 90 60 E2 00 01 44 71 47 90 00 00 02 58
                // 732:  27 0B 60 E7 11 00 40 08 07 20 E7 C1 AD B8 02 5A 36 08 B4 E7 E0 F0 09 1A 1A 08 9C D4 16 10 F7 D2 D8 F5 05 18 D0 E2 85 F4 06 20 00 28 00 30 B4 E7 E0 F0 09 2A 0E 08 00 12 0A 08 9C D4 16 10 00 18 01 20 00 30 00 38 00
                // 732:  27 0B 60 E7 11 00 33 08 07 20 E7 C1 AD B8 02 5A 29 08 EE 97 85 E9 01 1A 19 08 EE D6 16 10 FF F2 D8 F5 05 18 E9 E7 A3 05 20 00 28 00 30 EE 97 85 E9 01 2A 02 08 00 30 00 38 00

                // unknown
                // 前 4 byte 是群号
            }
            84, 87 -> { // 请求入群验证 和 被邀请入群
                bot.network.sendWithoutExpect(NewContact.SystemMsgNewGroup(bot.client))
            }
            187 -> { // 请求加好友验证
                bot.network.sendWithoutExpect(NewContact.SystemMsgNewFriend(bot.client))
            }
            else -> {
                logger.debug { "unknown PbGetMsg type ${data.msgHead.msgType}, data=${data.msgBody.msgContent.toUHexString()}" }
            }
        }
    }

    override suspend fun NoticePipelineContext.processImpl(data: Structmsg.StructMsg) {
        if (isConsumed) return
        if (logger.isEnabled && logger.isDebugEnabled) {
            data.msg?.context {
                throw contextualBugReportException(
                    "解析 NewContact.SystemMsgNewGroup, subType=$subType, groupMsgType=$groupMsgType",
                    forDebug = this.structureToString(),
                    additional = "并尽量描述此时机器人是否正被邀请加入群, 或者是有有新群员加入此群",
                )
            }
        }
    }

    override suspend fun NoticePipelineContext.processImpl(data: RequestPushStatus) {
        if (isConsumed) return
        if (logger.isEnabled && logger.isDebugEnabled) {
            throw contextualBugReportException(
                "decode SvcRequestPushStatus (PC Client status change)",
                data.structureToString(),
                additional = "unknown status=${data.status}",
            )
        }
    }
}