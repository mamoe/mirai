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
import net.mamoe.mirai.mock.contact.active.MockGroupActive
import net.mamoe.mirai.mock.internal.contact.MockGroupImpl
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

    @Suppress("INVISIBLE_MEMBER")
    override suspend fun queryHonorHistory(type: GroupHonorType): ActiveHonorList {
        // for dev: b/c mock api will not sync with real group honor member record automatically
        // honor member in mock api record
        val current = this.group.honorMembers[type]
        // honor member in real group honor member history
        val old = honorHistories[type]
        if (current == null) {
            if (old == null) {
                // add an empty honorList as default placeholder
                honorHistories[type] = ActiveHonorList(type, null, emptyList())
            } else {
                // change current honor member in honorHistories to null (as same as this.group.honorMembers)
                // and add old honor member into record
                honorHistories[type] = ActiveHonorList(type, null, old.current?.let {
                    old.records.plus(it)
                } ?: old.records)
            }
        } else {
            // use mock api data to build a honor info
            val info = ActiveHonorInfo(current.nameCard, current.id, current.avatarUrl, current, 0, 0, 0)
            if (old == null) {
                // if not history record found, add a new one with current honor member
                honorHistories[type] = ActiveHonorList(type, info, emptyList())
            } else {
                // if mock api honor member different from real group honor member history,
                // add old member into record and set current member as current in history
                if (old.current?.memberId != info.memberId) {
                    honorHistories[type] =
                        ActiveHonorList(type, info, old.current?.let {
                            old.records.plus(it)
                        } ?: old.records)
                }
            }
        }
        return honorHistories[type]!!
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