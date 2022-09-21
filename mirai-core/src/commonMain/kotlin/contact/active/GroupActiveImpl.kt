/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.contact.active

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.active.*
import net.mamoe.mirai.contact.checkBotPermission
import net.mamoe.mirai.data.GroupHonorType
import net.mamoe.mirai.data.GroupInfo
import net.mamoe.mirai.internal.contact.GroupImpl
import net.mamoe.mirai.internal.contact.groupCode
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.check

internal expect class GroupActiveImpl(
    group: GroupImpl,
    logger: MiraiLogger,
    groupInfo: GroupInfo,
) : CommonGroupActiveImpl

internal abstract class CommonGroupActiveImpl(
    protected val group: GroupImpl,
    protected val logger: MiraiLogger,
    groupInfo: GroupInfo,
) : GroupActive {
    private var _isHonorVisible: Boolean = groupInfo.isHonorVisible
    private var _isTitleVisible: Boolean = groupInfo.isTitleVisible
    private var _isTemperatureVisible: Boolean = groupInfo.isTemperatureVisible
    private var _isRankVisible: Map<Int, String> = groupInfo.rankTitles
    private var _temperatureTitles: Map<Int, String> = groupInfo.temperatureTitles

    private suspend fun getGroupLevelInfo(): GroupLevelInfo {
        return group.bot.getRawGroupLevelInfo(groupCode = group.groupCode).check()
    }

    private suspend fun refreshRank() {
        val info = getGroupLevelInfo()
        _isTitleVisible = info.levelFlag == 1
        _isTemperatureVisible = info.levelNewFlag == 1
        _isRankVisible = info.levelName.mapKeys { (level, _) -> level.removePrefix("lvln").toInt() }
        _temperatureTitles = info.levelNewName.mapKeys { (level, _) -> level.removePrefix("lvln").toInt() }
    }

    override val isHonorVisible: Boolean get() = _isHonorVisible

    override suspend fun setHonorVisible(newValue: Boolean) {
        group.checkBotPermission(MemberPermission.ADMINISTRATOR)
        group.bot.setGroupHonourFlag(groupCode = group.groupCode, flag = newValue).check()
        _isHonorVisible = newValue
    }

    override val rankTitles: Map<Int, String> get() = _isRankVisible

    override suspend fun setRankTitles(newValue: Map<Int, String>) {
        group.checkBotPermission(MemberPermission.ADMINISTRATOR)
        group.bot.setGroupLevelInfo(groupCode = group.groupCode, new = false, titles = newValue).check()
        refreshRank()
    }

    override val isTitleVisible: Boolean get() = _isTitleVisible

    override suspend fun setTitleVisible(newValue: Boolean) {
        group.checkBotPermission(MemberPermission.ADMINISTRATOR)
        group.bot.setGroupSetting(groupCode = group.groupCode, new = false, show = newValue).check()
        refreshRank()
    }

    override val temperatureTitles: Map<Int, String> get() = _temperatureTitles

    override suspend fun setTemperatureTitles(newValue: Map<Int, String>) {
        group.checkBotPermission(MemberPermission.ADMINISTRATOR)
        group.bot.setGroupLevelInfo(groupCode = group.groupCode, new = true, titles = newValue).check()
        refreshRank()
    }

    override val isTemperatureVisible: Boolean get() = _isTemperatureVisible

    override suspend fun setTemperatureVisible(newValue: Boolean) {
        group.checkBotPermission(MemberPermission.ADMINISTRATOR)
        group.bot.setGroupSetting(groupCode = group.groupCode, new = true, show = newValue).check()
        refreshRank()
    }

    override suspend fun refresh() {
        val info = group.bot.getRawMemberLevelInfo(groupCode = group.groupCode).check()
        refreshRank()
        _isHonorVisible = info.honourFlag == 1
        _isTitleVisible = info.levelFlag == 1
        _isRankVisible = info.levelName.mapKeys { (level, _) -> level.removePrefix("lvln").toInt() }

        for (member in group.members) {
            val (_, _, point, rank) = info.lv[member.id] ?: continue

            member.info.point = point
            member.info.rank = rank
        }
    }

    protected suspend fun getGroupActiveData(page: Int?): GroupActiveData {
        return group.bot.getRawGroupActiveData(group.id, page).check()
    }

    override fun asFlow(): Flow<ActiveRecord> {
        return flow {
            var page = 0
            while (currentCoroutineContext().isActive) {
                val result = getGroupActiveData(page = page)
                val most = result.info.mostAct ?: break

                for (active in most) emit(active.toActiveRecord(group))

                if (result.info.isEnd == 1) break
                page++
            }
        }
    }

    override suspend fun queryChart(): ActiveChart {
        return getGroupActiveData(page = null).info.toActiveChart()
    }

    private suspend fun getHonorInfo(type: GroupHonorType): MemberHonorList {
        return when (type) {
            GroupHonorType.TALKATIVE -> group.bot.getRawTalkativeInfo(group.id)
            GroupHonorType.PERFORMER -> group.bot.getRawContinuousInfo(group.id, type.id)
            GroupHonorType.LEGEND -> group.bot.getRawContinuousInfo(group.id, type.id)
            GroupHonorType.STRONG_NEWBIE -> group.bot.getRawContinuousInfo(group.id, type.id)
            GroupHonorType.EMOTION -> group.bot.getRawEmotionInfo(group.id)
            GroupHonorType.BRONZE -> group.bot.getRawHomeworkExcellentInfo(group.id, 1)
            GroupHonorType.SILVER -> group.bot.getRawHomeworkExcellentInfo(group.id, 2)
            GroupHonorType.GOLDEN -> group.bot.getRawHomeworkExcellentInfo(group.id, 3)
            GroupHonorType.WHIRLWIND -> group.bot.getRawHomeworkActiveInfo(group.id)
            GroupHonorType.RICHER -> group.bot.getRawRicherHonorInfo(group.id)
            GroupHonorType.RED_PACKET -> group.bot.getRawRedPacketInfo(group.id)
            else -> group.bot.getRawContinuousInfo(group.id, type.id)
        }
    }

    override suspend fun queryHonorHistory(type: GroupHonorType): ActiveHonorList {
        val data = getHonorInfo(type)

        when (type) {
            GroupHonorType.TALKATIVE, GroupHonorType.RICHER, GroupHonorType.RED_PACKET -> {
                val current = data.current?.uin
                for (member in group.members) {
                    if (member.id != current) {
                        member.info.honors += type
                    } else {
                        member.info.honors -= type
                    }
                }
                data.current?.let { group.members[it.uin] }?.let { it.info.honors += type }
            }
            GroupHonorType.LEGEND -> {
                val current = data.list.mapTo(HashSet()) { it.uin }
                for (member in group.members) {
                    if (member.id in current) {
                        member.info.honors += GroupHonorType.LEGEND
                        member.info.honors -= GroupHonorType.PERFORMER
                    } else {
                        member.info.honors -= GroupHonorType.LEGEND
                    }
                }
            }
            else -> {
                val current = data.list.mapTo(HashSet()) { it.uin }
                for (member in group.members) {
                    if (member.id in current) {
                        member.info.honors += type
                    } else {
                        member.info.honors -= type
                    }
                }
            }
        }

        @Suppress("INVISIBLE_MEMBER")
        return ActiveHonorList(
            type = type,
            current = data.current?.toActiveHonorInfo(group),
            records = data.list.map { it.toActiveHonorInfo(group) },
        )
    }

    private suspend fun getMemberScoreData(): MemberScoreData {
        return group.bot.getRawMemberTitleList(group.id).check()
    }

    override suspend fun queryActiveRank(): List<ActiveRankRecord> {
        val data = getMemberScoreData()

        @Suppress("INVISIBLE_MEMBER")
        return data.members.map {
            ActiveRankRecord(
                memberId = it.uin,
                memberName = it.nickName,
                member = group.get(id = it.uin),
                temperature = it.levelId,
                score = it.score
            )
        }
    }

    private suspend fun getMemberMedalInfo(uid: Long): MemberMedalData {
        return group.bot.getRawMemberMedalInfo(group.id, uid)
    }

    suspend fun queryMemberMedal(uid: Long): MemberMedalInfo {
        val info = getMemberMedalInfo(uid = uid)
        val medals: MutableSet<MemberMedalType> = HashSet()
        var worn: MemberMedalType = MemberMedalType.ACTIVE

        for (item in info.list) {
            if (item.achieveTs == 0) continue
            val type = when (item.mask) {
                MemberMedalType.OWNER.mask -> MemberMedalType.OWNER
                MemberMedalType.ADMIN.mask -> MemberMedalType.ADMIN
                MemberMedalType.SPECIAL.mask -> MemberMedalType.SPECIAL
                MemberMedalType.ACTIVE.mask -> MemberMedalType.ACTIVE
                else -> continue
            }
            medals.add(type)
            if (item.wearTs != 0) worn = type
        }

        @Suppress("INVISIBLE_MEMBER")
        return MemberMedalInfo(
            title = info.weared,
            color = info.wearedColor,
            wearing = worn,
            medals = medals
        )
    }
}