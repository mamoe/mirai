/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.notice

import kotlinx.coroutines.sync.withLock
import net.mamoe.mirai.internal.network.components.ContactUpdater
import net.mamoe.mirai.internal.network.components.MsgCommonMsgProcessor
import net.mamoe.mirai.internal.network.components.PipelineContext
import net.mamoe.mirai.internal.network.components.SsoProcessor
import net.mamoe.mirai.internal.network.handler.logger
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.internal.network.protocol.packet.chat.NewContact
import net.mamoe.mirai.utils.TypeKey
import net.mamoe.mirai.utils.debug
import net.mamoe.mirai.utils.toUHexString

internal class SystemMessageProcessor : MsgCommonMsgProcessor(), NewContactSupport {
    companion object {
        val KEY_FROM_SYNC = TypeKey<Boolean>("fromSync")
        val PipelineContext.fromSync get() = attributes[KEY_FROM_SYNC]
    }

    override suspend fun PipelineContext.processImpl(data: MsgComm.Msg): Unit = data.run {
        // TODO: 2021/6/26 extract logic into multiple processors
        when (msgHead.msgType) {
            33 -> bot.components[ContactUpdater].groupListModifyLock.withLock {
            }

            34 -> { // 与 33 重复
                return
            }

            38 -> bot.components[ContactUpdater].groupListModifyLock.withLock { // 建群
                TODO("removed")
            }

            85 -> bot.components[ContactUpdater].groupListModifyLock.withLock { // 其他客户端入群
                TODO("removed")
            }

            /*
            34 -> { // 主动入群

                // 回答了问题, 还需要管理员审核
                // msgContent=27 0B 60 E7 01 76 E4 B8 DD 82 00 30 45 41 31 30 35 35 42 44 39 39 42 35 37 46 44 31 41 31 46 36 42 43 42 43 33 43 42 39 34 34 38 31 33 34 42 36 31 46 38 45 43 39 38 38 43 39 37 33
                // msgContent=27 0B 60 E7 01 76 E4 B8 DD 02 00 30 44 44 41 43 44 33 35 43 31 39 34 30 46 42 39 39 34 46 43 32 34 43 39 32 33 39 31 45 42 35 32 33 46 36 30 37 35 42 41 38 42 30 30 37 42 36 42 41
                // 回答正确问题, 直接加入

                //            27 0B 60 E7 01 76 E4 B8 DD 82 00 30 43 37 37 39 41 38 32 44 38 33 30 35 37 38 31 33 37 45 42 39 35 43 42 45 36 45 43 38 36 34 38 44 34 35 44 42 33 44 45 37 34 41 36 30 33 37 46 45
                // 提交验证消息加入, 需要审核

                // 被踢了??
                // msgContent=27 0B 60 E7 01 76 E4 B8 DD 83 3E 03 3F A2 06 B4 B4 BD A8 D5 DF 00 30 46 46 32 33 36 39 35 33 31 37 42 44 46 37 43 36 39 34 37 41 45 38 39 43 45 43 42 46 33 41 37 35 39 34 39 45 36 37 33 37 31 41 39 44 33 33 45 33

                /*
                // 搜索后直接加入群

                soutv 17:43:32 : 33类型的content = 27 0B 60 E7 01 07 6E 47 BA 82 3E 03 3F A2 06 B4 B4 BD A8 D5 DF 00 30 32 30 39 39 42 39 41 46 32 39 41 35 42 33 46 34 32 30 44 36 44 36 39 35 44 38 45 34 35 30 46 30 45 30 38 45 31 41 39 42 46 46 45 32 30 32 34 35
                soutv 17:43:32 : 主动入群content = 2A 3D F5 69 01 35 D7 10 EA 83 4C EF 4F DD 06 B9 DC C0 ED D4 B1 00 30 37 41 39 31 39 34 31 41 30 37 46 38 32 31 39 39 43 34 35 46 39 30 36 31 43 37 39 37 33 39 35 43 34 44 36 31 33 43 31 35 42 37 32 45 46 43 43 36
                 */

                val group = bot.getGroupByUinOrNull(msgHead.fromUin)
                group ?: return

                msgBody.msgContent.soutv("主动入群content")

                if (msgBody.msgContent.read {
                        discardExact(4) // group code
                        discardExact(1) // 1
                        discardExact(4) // requester uin
                        readByte().toInt().and(0xff)
                        // 0x02: 回答正确问题直接加入
                        // 0x82: 回答了问题, 或者有验证消息, 需要管理员审核
                        // 0x83: 回答正确问题直接加入
                    } != 0x82) {

                    if (group.members.contains(msgHead.authUin)) {
                        return
                    }
                    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
                    return MemberJoinEvent.Active(group.newMember(getNewMemberInfo())
                        .also { group.members.delegate.addLast(it) })
                } else return
            }
            */

            //167 单向好友
            166, 167 -> {
                TODO("removed")
            }
            208 -> {
                // friend ptt
                TODO("removed")
            }
            529 -> {

                // top_package/awbk.java:3765

                when (msgHead.c2cCmd) {
                    // other client sync
                    7 -> {
                        TODO("removed")
                    }
                }

                // 各种垃圾
                // 08 04 12 1E 08 E9 07 10 B7 F7 8B 80 02 18 E9 07 20 00 28 DD F1 92 B7 07 30 DD F1 92 B7 07 48 02 50 03 32 1E 08 88 80 F8 92 CD 84 80 80 10 10 01 18 00 20 01 2A 0C 0A 0A 08 01 12 06 E5 95 8A E5 95 8A
            }
            141 -> {

                if (!bot.components[SsoProcessor].firstLoginSucceed || msgHead.fromUin == bot.id && !fromSync) {
                    return
                }
                TODO("removed")
            }
            84, 87 -> { // 请求入群验证 和 被要求入群
                bot.network.run {
                    NewContact.SystemMsgNewGroup(bot.client).sendWithoutExpect()
                }
                return
            }
            187 -> { // 请求加好友验证
                bot.network.run {
                    NewContact.SystemMsgNewFriend(bot.client).sendWithoutExpect()
                }
                return
            }
            732 -> {
                // unknown
                // 前 4 byte 是群号
                return
            }
            //陌生人添加信息
            191 -> {
                TODO("removed")
            }
            // 732:  27 0B 60 E7 0C 01 3E 03 3F A2 5E 90 60 E2 00 01 44 71 47 90 00 00 02 58
            // 732:  27 0B 60 E7 11 00 40 08 07 20 E7 C1 AD B8 02 5A 36 08 B4 E7 E0 F0 09 1A 1A 08 9C D4 16 10 F7 D2 D8 F5 05 18 D0 E2 85 F4 06 20 00 28 00 30 B4 E7 E0 F0 09 2A 0E 08 00 12 0A 08 9C D4 16 10 00 18 01 20 00 30 00 38 00
            // 732:  27 0B 60 E7 11 00 33 08 07 20 E7 C1 AD B8 02 5A 29 08 EE 97 85 E9 01 1A 19 08 EE D6 16 10 FF F2 D8 F5 05 18 E9 E7 A3 05 20 00 28 00 30 EE 97 85 E9 01 2A 02 08 00 30 00 38 00
            else -> {
                bot.network.logger.debug { "unknown PbGetMsg type ${msgHead.msgType}, data=${msgBody.msgContent.toUHexString()}" }
                return
            }
        }
    }

    // kotlin bug, don't remove
    private inline fun kotlinx.atomicfu.AtomicInt.loop(action: (Int) -> Unit): Nothing {
        while (true) {
            action(value)
        }
    }

}