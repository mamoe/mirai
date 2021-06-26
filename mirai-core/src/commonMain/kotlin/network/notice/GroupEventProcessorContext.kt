/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.notice

import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.contact.GroupImpl
import net.mamoe.mirai.internal.contact.info.GroupInfoImpl
import net.mamoe.mirai.internal.contact.info.MemberInfoImpl
import net.mamoe.mirai.internal.getGroupByUin
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.internal.network.protocol.packet.list.FriendList
import net.mamoe.mirai.internal.network.protocol.packet.sendAndExpect

internal interface GroupEventProcessorContext {

    fun MsgComm.Msg.getNewMemberInfo(): MemberInfoImpl {
        return MemberInfoImpl(
            nameCard = msgHead.authNick.ifEmpty { msgHead.fromNick },
            permission = MemberPermission.MEMBER,
            specialTitle = "",
            muteTimestamp = 0,
            uin = msgHead.authUin,
            nick = msgHead.authNick.ifEmpty { msgHead.fromNick },
            remark = "",
            anonymousId = null
        )
    }

    suspend fun QQAndroidBot.createGroupForBot(groupUin: Long): GroupImpl? {
        val group = getGroupByUin(groupUin)
        if (group != null) {
            return null
        }

        return getNewGroup(Mirai.calculateGroupCodeByGroupUin(groupUin))?.apply { groups.delegate.add(this) }
    }

    suspend fun QQAndroidBot.getNewGroup(groupCode: Long): GroupImpl? {
        val troopNum = FriendList.GetTroopListSimplify(client)
            .sendAndExpect(network, timeoutMillis = 10_000, retry = 5)
            .groups.firstOrNull { it.groupCode == groupCode } ?: return null

        return GroupImpl(
            bot = this,
            coroutineContext = coroutineContext,
            id = groupCode,
            groupInfo = GroupInfoImpl(troopNum),
            members = Mirai.getRawGroupMemberList(
                this,
                troopNum.groupUin,
                troopNum.groupCode,
                troopNum.dwGroupOwnerUin
            )
        )
    }

}