/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.contact.active

import net.mamoe.mirai.contact.active.*
import net.mamoe.mirai.data.GroupHonorType

public interface MockGroupActive : GroupActive {
    /**
     * 设置该群内所有群活跃度记录
     * @see asFlow
     * @see asStream
     */
    public fun mockSetActiveRecords(records: Collection<ActiveRecord>)

    /**
     * 设置活跃度图表数据
     * @see queryChart
     */
    public fun mockSetChart(chart: ActiveChart)

    /**
     * 设置群荣耀历史数据
     * @see queryHonorHistory
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("mockSetHonorHistory")
    public fun mockSetHonorHistory(type: GroupHonorType, activeHonorList: ActiveHonorList?)

    /**
     * 设置活跃度排行榜
     * @see queryActiveRank
     */
    public fun mockSetRankRecords(list: List<ActiveRankRecord>)
}