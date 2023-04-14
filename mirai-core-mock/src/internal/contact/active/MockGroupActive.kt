/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.internal.contact.active

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.active.*
import net.mamoe.mirai.contact.checkBotPermission
import net.mamoe.mirai.data.GroupHonorType
import net.mamoe.mirai.event.events.GroupTalkativeChangeEvent
import net.mamoe.mirai.event.events.MemberHonorChangeEvent
import net.mamoe.mirai.mock.contact.MockNormalMember
import net.mamoe.mirai.mock.contact.active.MockGroupActive
import net.mamoe.mirai.mock.internal.contact.MockGroupImpl
import net.mamoe.mirai.mock.utils.broadcastBlocking
import net.mamoe.mirai.utils.ConcurrentHashMap
import net.mamoe.mirai.utils.JavaFriendlyAPI
import net.mamoe.mirai.utils.asImmutable
import java.util.stream.Stream
import kotlin.collections.set

internal class MockGroupActiveImpl(
    private val group: MockGroupImpl
) : MockGroupActive {
    @Volatile
    override var isHonorVisible: Boolean = false
    override suspend fun setHonorVisible(newValue: Boolean) {
        group.checkBotPermission(MemberPermission.ADMINISTRATOR)
        isHonorVisible = newValue
    }

    @Volatile
    override var isTitleVisible: Boolean = false
    override suspend fun setTitleVisible(newValue: Boolean) {
        group.checkBotPermission(MemberPermission.ADMINISTRATOR)
        isTitleVisible = newValue
    }

    @Volatile
    override var isTemperatureVisible: Boolean = false
    override suspend fun setTemperatureVisible(newValue: Boolean) {
        group.checkBotPermission(MemberPermission.ADMINISTRATOR)
        isTemperatureVisible = newValue
    }

    @Volatile
    override var rankTitles: Map<Int, String> = ConcurrentHashMap()
    override suspend fun setRankTitles(newValue: Map<Int, String>) {
        group.checkBotPermission(MemberPermission.ADMINISTRATOR)
        rankTitles = newValue
    }

    @Volatile
    override var temperatureTitles: Map<Int, String> = ConcurrentHashMap()
    override suspend fun setTemperatureTitles(newValue: Map<Int, String>) {
        group.checkBotPermission(MemberPermission.ADMINISTRATOR)
        temperatureTitles = newValue
    }

    override suspend fun refresh() {
    }

    @Volatile
    private var records: Collection<ActiveRecord> = listOf()

    override fun asFlow(): Flow<ActiveRecord> = records.asFlow()

    @JavaFriendlyAPI
    override fun asStream(): Stream<ActiveRecord> = records.stream()

    @Volatile
    private var activeChart: ActiveChart = ActiveChart(mapOf(), mapOf(), mapOf(), mapOf(), mapOf())

    override suspend fun queryChart(): ActiveChart = activeChart

    private var honorHistories: MutableMap<GroupHonorType, ActiveHonorList> = ConcurrentHashMap()

    @Suppress("INVISIBLE_MEMBER") // for ActiveHonorInfo
    override fun changeHonorMember(member: MockNormalMember, honorType: GroupHonorType) {
        val old = honorHistories[honorType]

        val info = ActiveHonorInfo(member.nameCard, member.id, member.avatarUrl, member, 0, 0, 0)
        if (old == null) {
            // if not history record found, add a new one with current honor member
            honorHistories[honorType] = ActiveHonorList(honorType, info, emptyList())
        } else if (old.current?.memberId != info.memberId) {
            honorHistories[honorType] =
                ActiveHonorList(honorType, info, old.current?.let {
                    old.records.plus(it)
                } ?: old.records)
            if (old.current != null) {
                if (honorType == GroupHonorType.TALKATIVE) {
                    GroupTalkativeChangeEvent(
                        this.group,
                        member,
                        old.current!!.member!!
                    ).broadcastBlocking()
                }
                MemberHonorChangeEvent.Lose(old.current!!.member!!, honorType).broadcastBlocking()
            }
        }
        MemberHonorChangeEvent.Achieve(member, honorType).broadcastBlocking()
    }

    override suspend fun queryHonorHistory(type: GroupHonorType): ActiveHonorList {
        return honorHistories.getOrElse(type) { ActiveHonorList(type, null, listOf()) }
    }

    @Volatile
    private var ranks: List<ActiveRankRecord> = listOf()

    override suspend fun queryActiveRank(): List<ActiveRankRecord> = ranks.asImmutable()


    ///////////////////////////////////////////////////////////////////////////
    // mock API
    ///////////////////////////////////////////////////////////////////////////

    override fun mockSetActiveRecords(records: Collection<ActiveRecord>) {
        this.records = records
    }

    override fun mockSetChart(chart: ActiveChart) {
        activeChart = chart
    }

    override fun mockSetHonorHistory(type: GroupHonorType, activeHonorList: ActiveHonorList?) {
        if (activeHonorList != null) {
            honorHistories[type] = activeHonorList
        } else {
            honorHistories.remove(type)
        }
    }

    override fun mockSetRankRecords(list: List<ActiveRankRecord>) {
        this.ranks = list
    }
}