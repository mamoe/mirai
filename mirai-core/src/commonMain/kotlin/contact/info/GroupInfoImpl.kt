/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.contact.info

import kotlinx.serialization.Serializable
import net.mamoe.mirai.data.GroupInfo
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.protocol.data.jce.StGroupRankInfo
import net.mamoe.mirai.internal.network.protocol.data.jce.StTroopNum

@Serializable
internal data class GroupInfoImpl(
    override val uin: Long,
    override val owner: Long,
    override val groupCode: Long,
    override val memo: String,
    override val name: String,
    override val allowMemberInvite: Boolean,
    override val allowAnonymousChat: Boolean,
    override val autoApprove: Boolean,
    override val confessTalk: Boolean,
    override val muteAll: Boolean,
    override val botMuteTimestamp: Int,
    override val isHonorVisible: Boolean,
    override val isTitleVisible: Boolean,
    override val isTemperatureVisible: Boolean,
    override val rankTitles: Map<Int, String>,
    override val temperatureTitles: Map<Int, String>
) : GroupInfo, Packet, Packet.NoLog {
    constructor(stTroopNum: StTroopNum, stGroupRankInfo: StGroupRankInfo?) : this(
        uin = stTroopNum.groupUin,
        owner = stTroopNum.dwGroupOwnerUin,
        groupCode = stTroopNum.groupCode,
        memo = stTroopNum.groupMemo,
        name = stTroopNum.groupName,
        allowMemberInvite = stTroopNum.dwGroupFlagExt?.and(0x000000c0) != 0L,
        allowAnonymousChat = stTroopNum.dwGroupFlagExt?.and(0x40000000) != 0L,
        autoApprove = stTroopNum.dwGroupFlagExt3?.and(0x00100000) == 0L,
        confessTalk = stTroopNum.dwGroupFlagExt3?.and(0x00002000) == 0L,
        muteAll = stTroopNum.dwShutUpTimestamp != 0L,
        botMuteTimestamp = stTroopNum.dwMyShutUpTimestamp?.toInt() ?: 0,
        isHonorVisible = stGroupRankInfo?.groupRankSysFlag?.toInt() == 1,
        isTitleVisible = stGroupRankInfo?.groupRankUserFlag?.toInt() == 1,
        isTemperatureVisible = stGroupRankInfo?.groupRankUserFlagNew?.toInt() == 1,
        rankTitles = buildMap {
            for (pair in stGroupRankInfo?.vecRankMap.orEmpty()) {
                val level = pair.dwLevel?.toInt() ?: continue
                val title = pair.rank.orEmpty()

                put(level, title)
            }
        },
        temperatureTitles = buildMap {
            for (pair in stGroupRankInfo?.vecRankMapNew.orEmpty()) {
                val level = pair.dwLevel?.toInt() ?: continue
                val title = pair.rank.orEmpty()

                put(level, title)
            }
        }
    )
}