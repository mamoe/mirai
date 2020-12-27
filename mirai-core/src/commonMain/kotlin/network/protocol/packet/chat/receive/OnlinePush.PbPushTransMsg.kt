/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
@file:OptIn(JavaFriendlyAPI::class)

package net.mamoe.mirai.internal.network.protocol.packet.chat.receive

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.discardExact
import kotlinx.io.core.readUByte
import kotlinx.io.core.readUInt
import net.mamoe.mirai.JavaFriendlyAPI
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.contact.*
import net.mamoe.mirai.internal.message.contextualBugReportException
import net.mamoe.mirai.internal.network.MultiPacketByIterable
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.protocol.data.proto.OnlinePushTrans
import net.mamoe.mirai.internal.network.protocol.packet.IncomingPacketFactory
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.internal.network.protocol.packet.buildResponseUniPacket
import net.mamoe.mirai.internal.utils._miraiContentToString
import net.mamoe.mirai.internal.utils.io.serialization.readProtoBuf
import net.mamoe.mirai.utils.*


internal object OnlinePushPbPushTransMsg :
    IncomingPacketFactory<Packet?>("OnlinePush.PbPushTransMsg", "OnlinePush.RespPush") {


    override suspend fun ByteReadPacket.decode(bot: QQAndroidBot, sequenceId: Int): Packet? {
        val content = this.readProtoBuf(OnlinePushTrans.PbMsgInfo.serializer())

        if (!bot.client.syncingController.pbPushTransMsgCacheList.addCache(
                content.run {
                    QQAndroidClient.MessageSvcSyncData.PbPushTransMsgSyncId(msgUid, msgSeq, msgTime)
                }
            )
        ) {
            return null
        }
        // bot.network.logger.debug { content._miraiContentToString() }


        content.msgData.read<Unit> {
            when (content.msgType) {
                44 -> {
                    //                  3D C4 33 DD 01 FF CD 76 F4 03 C3 7E 2E 34
                    //      群转让
                    //      start with  3D C4 33 DD 01 FF
                    //                  3D C4 33 DD 01 FF C3 7E 2E 34 CD 76 F4 03
                    // 权限变更
                    //                  3D C4 33 DD 01 00/01 .....
                    //                  3D C4 33 DD 01 01 C3 7E 2E 34 01
                    this.discardExact(5)
                    when (val mode = readUByte().toInt()) {
                        0xFF -> {
                            // 群转让 / huifu.qq.com
                            // From -> to
                            val from = readUInt().toLong()
                            val to = readUInt().toLong()
                            val results = ArrayList<Packet>()
                            // println("$from -> $to")
                            if (to == bot.id) {
                                if (bot.getGroupByUinOrNull(content.fromUin) == null) {
                                    MessageSvcPbGetMsg.run {
                                        results.add(
                                            BotJoinGroupEvent.Retrieve(
                                                bot.createGroupForBot(content.fromUin)!!
                                            )
                                        )
                                    }
                                }
                            }
                            val group = bot.getGroupByUin(content.fromUin) as GroupImpl
                            if (from == bot.id) {
                                if (group.botPermission != MemberPermission.MEMBER)
                                    results.add(
                                        BotGroupPermissionChangeEvent(
                                            group, group.botPermission.also {
                                                group.botAsMember.checkIsMemberImpl().permission =
                                                    MemberPermission.MEMBER
                                            },
                                            MemberPermission.MEMBER
                                        )
                                    )
                            } else {
                                val member = group[from] as NormalMemberImpl
                                if (member.permission != MemberPermission.MEMBER) {
                                    results.add(
                                        MemberPermissionChangeEvent(
                                            member,
                                            member.permission.also { member.permission = MemberPermission.MEMBER },
                                            MemberPermission.MEMBER
                                        )
                                    )
                                }
                            }
                            if (to == bot.id) {
                                if (group.botPermission != MemberPermission.OWNER) {
                                    results.add(
                                        BotGroupPermissionChangeEvent(
                                            group,
                                            group.botAsMember.permission.also {
                                                group.botAsMember.checkIsMemberImpl().permission =
                                                    MemberPermission.OWNER
                                            },
                                            MemberPermission.OWNER
                                        )
                                    )
                                }
                            } else {
                                val newOwner = (group[to] ?: group.newMember(
                                    MemberInfoImpl(
                                        to,
                                        "",
                                        MemberPermission.OWNER,
                                        "",
                                        "",
                                        "",
                                        0,
                                        null
                                    )
                                )).also { owner ->
                                    owner.checkIsMemberImpl().permission = MemberPermission.OWNER
                                    group.members.delegate.add(owner)
                                    results.add(MemberJoinEvent.Retrieve(owner))
                                }
                                if (newOwner.permission != MemberPermission.OWNER) {
                                    results.add(
                                        MemberPermissionChangeEvent(
                                            newOwner,
                                            newOwner.permission.also {
                                                newOwner.checkIsMemberImpl().permission = MemberPermission.OWNER
                                            },
                                            MemberPermission.OWNER
                                        )
                                    )
                                }
                            }
                            return MultiPacketByIterable(results)
                        }
                        else -> {
                            var var5 = 0L
                            val target = readUInt().toLong()
                            if (mode != 0 && mode != 1) {
                                var5 = readUInt().toLong()
                            }

                            val group = bot.getGroupByUin(content.fromUin) as GroupImpl

                            if (var5 == 0L && this.remaining == 1L) {//管理员变更
                                val newPermission =
                                    if (this.readByte().toInt() == 1) MemberPermission.ADMINISTRATOR
                                    else MemberPermission.MEMBER

                                if (target == bot.id) {
                                    if (group.botPermission == newPermission) {
                                        return null
                                    }

                                    return BotGroupPermissionChangeEvent(
                                        group,
                                        group.botPermission.also {
                                            group.botAsMember.checkIsMemberImpl().permission = newPermission
                                        },
                                        newPermission
                                    )
                                } else {
                                    val member = group[target] as NormalMemberImpl
                                    if (member.permission == newPermission) {
                                        return null
                                    }

                                    return MemberPermissionChangeEvent(
                                        member,
                                        member.permission.also { member.permission = newPermission },
                                        newPermission
                                    )
                                }
                            }
                        }
                    }
                }
                34 -> {
                    /* quit
                    27 0B 60 E7
                    01
                    2F 55 7C B8
                    82
                    00 30 42 33 32 46 30 38 33 32 39 32 35 30 31 39 33 45 46 32 45 30 36 35 41 35 41 33 42 37 35 43 41 34 46 37 42 38 42 38 42 44 43 35 35 34 35 44 38 30
                     */
                    /* kick
                    27 0B 60 E7
                    01
                    A8 32 51 A1
                    83 3E 03 3F A2 06 B4 B4 BD A8 D5 DF 00 30 39 32 46 45 30 36 31 41 33 37 36 43 44 35 37 35 37 39 45 37 32 34 44 37 37 30 36 46 39 39 43 35 35 33 33 31 34 44 32 44 46 35 45 42 43 31 31 36
                     */
                    readUInt().toLong() // groupUin
                    readByte().toInt() // follow type
                    val target = readUInt().toLong()
                    val type = readUByte().toInt()
                    val operator = readUInt().toLong()
                    val groupUin = content.fromUin

                    when (type) {
                        2, 0x82 -> bot.getGroupByUinOrNull(groupUin)?.let { group ->
                            if (target == bot.id) {
                                return BotLeaveEvent.Active(group).also {
                                    group.cancel(CancellationException("Leaved actively"))
                                    bot.groups.delegate.remove(group)
                                }
                            } else {
                                val member = group.get(target) as? NormalMemberImpl ?: return null
                                return MemberLeaveEvent.Quit(member.also {
                                    member.cancel(CancellationException("Leaved actively"))
                                    group.members.delegate.remove(member)
                                })
                            }
                        }
                        3, 0x83 -> bot.getGroupByUin(groupUin).let { group ->
                            if (target == bot.id) {
                                val member = group.members[operator] ?: return@let null
                                return BotLeaveEvent.Kick(member).also {
                                    group.cancel(CancellationException("Being kicked"))
                                    bot.groups.delegate.remove(group)
                                }
                            } else {
                                val member = group.get(target) as? NormalMemberImpl ?: return null
                                return MemberLeaveEvent.Kick(member.also {
                                    member.cancel(CancellationException("Being kicked"))
                                    group.members.delegate.remove(member)
                                }, group.members[operator])
                            }
                        }
                    }
                }
                else -> {
                    throw contextualBugReportException(
                        "解析 OnlinePush.PbPushTransMsg, msgType=${content.msgType}",
                        content._miraiContentToString(),
                        null,
                        "并描述此时机器人是否被踢出, 或是否有成员列表变更等动作."
                    )
                }
            }
        }
        return null
    }

    override suspend fun QQAndroidBot.handle(packet: Packet?, sequenceId: Int): OutgoingPacket? {
        return buildResponseUniPacket(client, sequenceId = sequenceId) {}
    }

}
