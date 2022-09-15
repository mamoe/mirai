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
import net.mamoe.mirai.contact.ContactList
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.contact.ChannelImpl
import net.mamoe.mirai.internal.contact.GuildImpl
import net.mamoe.mirai.internal.contact.GuildMemberImpl
import net.mamoe.mirai.internal.contact.info.ChannelInfoImpl
import net.mamoe.mirai.internal.contact.info.GuildInfoImpl
import net.mamoe.mirai.internal.contact.info.SlowModeInfosItemImpl
import net.mamoe.mirai.internal.contact.info.TopMsgImpl
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.protocol.data.proto.Guild
import net.mamoe.mirai.internal.network.protocol.packet.IncomingPacketFactory
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.internal.network.protocol.packet.guild.send.OidbSvcTrpcTcp
import net.mamoe.mirai.internal.utils.io.serialization.readProtoBuf

internal object PushFirstView : IncomingPacketFactory<Packet?>(
    "trpc.group_pro.synclogic.SyncLogic.PushFirstView", ""
) {
    override suspend fun ByteReadPacket.decode(bot: QQAndroidBot, sequenceId: Int): Packet? {
        val res = this.readProtoBuf(Guild.FirstViewMsg.serializer())
        val guildNodes = res.guildNodes
        if (guildNodes.isNotEmpty()) {
            for (guildNode in guildNodes) {
                //子频道列表
                val channel = bot.network.sendAndExpect(OidbSvcTrpcTcp.FetchChannelList(bot.client, guildNode.guildId))
                //频道信息
                val guildMeta = bot.network.sendAndExpect(OidbSvcTrpcTcp.FetchGuestGuild(bot.client, guildNode.guildId))
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
//                            channelSubType = it.visibleType
                            ),
                            id = it.channelId,
                            parentCoroutineContext = bot.coroutineContext
                        )
                    )
                }

                //TODO 储存频道成员列表
                val memberList = ContactList<GuildMemberImpl>()
                //val members = bot.network.sendAndExpect(OidbSvcTrpcTcp.FetchGuildMemberListWithRole(bot.client,guildNode.guildId,))

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
                    coverUrl = "https://groupprocover-76483.picgzc.qpic.cn/${guildNode.guildId}",
                    avatarUrl = "https://groupprohead-76292.picgzc.qpic.cn/${guildNode.guildId}"
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
            }
        }

        if (res.channelMsgs.isNotEmpty()) {
            //TODO sync channel information
        }
        return null
    }


    override suspend fun QQAndroidBot.handle(packet: Packet?, sequenceId: Int): OutgoingPacket? {


        return null
    }
}