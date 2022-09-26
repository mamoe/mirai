/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmBlockingBridge

package net.mamoe.mirai.contact.active

import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.data.GroupHonorType
import net.mamoe.mirai.utils.NotStableForInheritance
import net.mamoe.mirai.utils.Streamable
import kotlin.jvm.JvmName

/**
 * 表示一个群活跃度管理.
 *
 * ## 获取 [GroupActive] 实例
 *
 * 只可以通过 [Group.active] 获取一个群的活跃度管理, 即 [GroupActive] 实例.
 *
 * ### 头衔设置
 *
 * * 通过 [isHonorVisible] 可以获取和设置一个群的荣誉是否显示,
 * * 通过 [isTitleVisible] 可以获取和设置一个群的头衔是否显示,
 * * 通过 [isTemperatureVisible] 可以获取和设置一个群的活跃度是否显示,
 * * 通过 [rankTitles] 可以获取和设置一个群的等级头衔列表 (PC 端显示),
 * * 通过 [temperatureTitles] 可以获取和设置一个群的活跃度头衔列表 (手机端显示)
 *
 * ### 刷新群成员活跃数据
 *
 * 通过 [refresh] 可以刷新 [Member.active] 中的属性 (不包括 honors 和 temperature)
 *
 * ### 活跃度记录
 *
 * 通过 [asFlow] 可以获取群活跃度记录*惰性*流.
 *
 * 若要获取全部活跃度记录, 可使用 [toList].
 *
 * ### 活跃度图表
 *
 * 通过 [queryChart] 可以获取活跃度图表，
 * 包括
 * * 每日总人数 [ActiveChart.members]
 * * 每日活跃人数 [ActiveChart.actives]
 * * 每日申请人数 [ActiveChart.sentences]
 * * 每日入群人数 [ActiveChart.join]
 * * 每日退群人数 [ActiveChart.exit]
 *
 * 通过 [queryHonorHistory] 可以获取群荣耀历史数据，
 * 包括
 * * 当前荣耀持有者 (龙王，壕礼皇冠, 善财福禄寿) [ActiveHonorList.current]
 * * 群荣耀历史记录 [ActiveHonorList.records]
 *
 * @since 2.13
 */
@NotStableForInheritance
public interface GroupActive : Streamable<ActiveRecord> {

    /**
     * 是否在群聊中显示荣誉
     * @see MemberActive.honors
     */
    public val isHonorVisible: Boolean

    /**
     * 设置是否在群聊中显示荣誉
     * @see MemberActive.honors
     */
    public suspend fun setHonorVisible(newValue: Boolean)

    /**
     * 是否在群聊中显示头衔
     * @see Member.rankTitle
     * @see Member.temperatureTitle
     */
    public val isTitleVisible: Boolean

    /**
     * 设置是否在群聊中显示头衔。操作成功时会同时刷新等级头衔信息。
     * @see Member.rankTitle
     * @see Member.temperatureTitle
     */
    public suspend fun setTitleVisible(newValue: Boolean)

    /**
     * 是否在群聊中显示活跃度
     * @see MemberActive.temperature
     */
    public val isTemperatureVisible: Boolean

    /**
     * 设置是否在群聊中显示活跃度。操作成功时会同时刷新等级头衔信息。
     * @see MemberActive.temperature
     */
    public suspend fun setTemperatureVisible(newValue: Boolean)

    /**
     * 等级头衔列表，键是等级，值是头衔
     *
     * @see Member.rankTitle
     */
    public val rankTitles: Map<Int, String>

    /**
     * 设置等级头衔列表，键是等级，值是头衔。操作成功时会同时刷新等级头衔信息。
     * @see Member.rankTitle
     */
    public suspend fun setRankTitles(newValue: Map<Int, String>)

    /**
     * 活跃度头衔列表，键是等级，值是头衔。操作成功时会同时刷新活跃度头衔信息。
     * @see Member.temperatureTitle
     */
    public val temperatureTitles: Map<Int, String>

    /**
     * 设置活跃度头衔列表，键是等级，值是头衔。操作成功时会同时刷新活跃度头衔信息。
     * @see Member.temperatureTitle
     */
    public suspend fun setTemperatureTitles(newValue: Map<Int, String>)

    /**
     * 刷新 [Member.active] 中的属性 (不包括 [honors][MemberActive.honors] 和 [temperature][MemberActive.temperature])
     * @see Member.active
     */
    public suspend fun refresh()

    /**
     * 获取活跃度图表数据
     */
    public suspend fun queryChart(): ActiveChart

    /**
     * 获取群荣耀历史数据, 刷新 [Member.active] 中的 [MemberActive.honors]
     * @see Member.active
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("queryHonorHistory")
    public suspend fun queryHonorHistory(type: GroupHonorType): ActiveHonorList

    /**
     * 获取活跃度排行榜，通常是前五十名
     */
    public suspend fun queryActiveRank(): List<ActiveRankRecord>
}