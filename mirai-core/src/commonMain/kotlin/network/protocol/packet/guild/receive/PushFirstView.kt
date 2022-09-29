/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.packet.guild.receive

import io.ktor.utils.io.core.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import net.mamoe.mirai.contact.ContactList
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.contact.ChannelImpl
import net.mamoe.mirai.internal.contact.GuildImpl
import net.mamoe.mirai.internal.contact.GuildMemberImpl
import net.mamoe.mirai.internal.contact.info.*
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.protocol.data.proto.Guild
import net.mamoe.mirai.internal.network.protocol.packet.IncomingPacketFactory
import net.mamoe.mirai.internal.network.protocol.packet.guild.send.OidbSvcTrpcTcp
import net.mamoe.mirai.internal.utils.io.serialization.readProtoBuf
import net.mamoe.mirai.utils.retryCatching

internal object PushFirstView : IncomingPacketFactory<Packet?>(
    "trpc.group_pro.synclogic.SyncLogic.PushFirstView", ""
) {
    override suspend fun ByteReadPacket.decode(bot: QQAndroidBot, sequenceId: Int): Packet? {
        val res = this.readProtoBuf(Guild.FirstViewMsg.serializer())
        val guildNodes = res.guildNodes
        if (guildNodes.isNotEmpty()) {
            val semaphore = Semaphore(30)
            coroutineScope {
                guildNodes.forEach { guildNode ->
                    launch {
                        semaphore.withPermit {
                            retryCatching(5) {
                                //子频道列表
                                val channel =
                                    bot.network.sendAndExpect(
                                        OidbSvcTrpcTcp.FetchChannelList(
                                            bot.client,
                                            guildNode.guildId
                                        )
                                    )
                                //频道信息
                                val guildMeta =
                                    bot.network.sendAndExpect(
                                        OidbSvcTrpcTcp.FetchGuestGuild(
                                            bot.client,
                                            guildNode.guildId
                                        )
                                    )
                                //储存子频道列表
                                val channelList = mutableListOf<ChannelImpl>()

                                channel.origin.rsp.rsp.channels.forEach {
                                    val slowModeInfosItemImpl = mutableListOf<SlowModeInfosItemImpl>()
                                    it.slowModeInfos.forEach { slow ->
                                        slowModeInfosItemImpl.add(
                                            SlowModeInfosItemImpl(
                                                slowModeCircle = slow.slowModeCircle,
                                                slowModeKey = slow.slowModeKey,
                                                slowModeText = slow.slowModeText,
                                                speakFrequency = slow.speakFrequency
                                            )
                                        )
                                    }
                                    channelList.add(
                                        ChannelImpl(
                                            bot = bot,
                                            channelInfo = ChannelInfoImpl(
                                                id = it.channelId,
                                                name = it.channelName,
                                                createTime = it.createTime,
                                                channelType = it.channelType,
                                                finalNotifyType = it.finalNotifyType,
                                                creatorTinyId = it.creatorTinyId,
                                                topMsg = TopMsgImpl(
                                                    topMsgOperatorTinyId = it.topMsg.topMsgOperatorTinyId,
                                                    topMsgSeq = it.topMsg.topMsgSeq,
                                                    topMsgTime = it.topMsg.topMsgTime
                                                ),
                                                slowModeInfos = slowModeInfosItemImpl,
                                                talkPermission = it.talkPermission,
                                                //channelSubType = it.visibleType
                                            ),
                                            id = it.channelId,
                                            guildId = guildNode.guildId,
                                            parentCoroutineContext = bot.coroutineContext
                                        )
                                    )
                                }

                                //TODO 后期鉴定一下是否已经拿到所有频道成员
                                val memberList = ContactList<GuildMemberImpl>()
                                var flag = true
                                var roleIndex = 2L
                                var startIndex: Short = 0
                                var param: String? = null

                                do {
                                    val members = bot.network.sendAndExpect(
                                        OidbSvcTrpcTcp.FetchGuildMemberListWithRole(
                                            client = bot.client,
                                            guildId = guildNode.guildId,
                                            channelId = channel.origin.rsp.rsp.channels[0].channelId,
                                            roleIdIndex = roleIndex,
                                            startIndex = startIndex,
                                            param = param
                                        )
                                    )

                                    members.origin.data.memberWithRoles.also { it ->
                                        it.forEach { guildGroupMembersInfo ->
                                            guildGroupMembersInfo.members.forEach {
                                                memberList.delegate.add(
                                                    GuildMemberImpl(
                                                        bot,
                                                        guildNode.guildId,
                                                        bot.coroutineContext,
                                                        GuildMemberInfoImpl(
                                                            title = it.title,
                                                            lastSpeakTime = it.lastSpeakTime,
                                                            tinyId = it.tinyId,
                                                            nickname = it.nickname,
                                                            role = it.role,
                                                            roleName = guildGroupMembersInfo.roleName
                                                        )
                                                    )
                                                )
                                            }

                                        }
                                    }
                                    if (null != members.origin.data.nextIndex) {
                                        param = members.origin.data.nextQueryParam
                                        startIndex = members.origin.data.nextIndex
                                        roleIndex = members.origin.data.nextRoleIdIndex
                                    } else {
                                        flag = false
                                    }
                                } while (flag)


                                val guildInfo = GuildInfoImpl(
                                    name = guildNode.guildName.decodeToString(),
                                    id = guildNode.guildId,
                                    guildCode = guildNode.guildCode,
                                    createTime = guildMeta.origin.rsp.rsp.meta.createTime,
                                    ownerId = guildMeta.origin.rsp.rsp.meta.ownerId,
                                    memberCount = guildMeta.origin.rsp.rsp.meta.memberCount,
                                    maxAdminCount = guildMeta.origin.rsp.rsp.meta.adminMaxNum,
                                    maxRobotCount = guildMeta.origin.rsp.rsp.meta.robotMaxNum,
                                    maxMemberCount = guildMeta.origin.rsp.rsp.meta.maxMemberCount,
                                    guildProfile = guildMeta.origin.rsp.rsp.meta.profile,
                                    coverUrl = "https://groupprocover-76483.picgzc.qpic.cn/${guildNode.guildId}/100",
                                    avatarUrl = "https://groupprohead-76292.picgzc.qpic.cn/${guildNode.guildId}/100"
                                )

                                val guildImpl = GuildImpl(
                                    bot = bot,
                                    parentCoroutineContext = bot.coroutineContext,
                                    id = guildNode.guildId,
                                    guildInfo = guildInfo,
                                    channelNodes = channelList,
                                    members = memberList,
                                )
                                bot.guilds.delegate.add(guildImpl)
                            }.getOrThrow()
                        }
                    }

                }
            }
        }

        if (res.channelMsgs.isNotEmpty()) {
            //TODO sync channel information
        }
        return null
    }
}