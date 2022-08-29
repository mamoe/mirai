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

import kotlinx.coroutines.flow.Flow
import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.data.GroupHonorType
import net.mamoe.mirai.utils.JavaFriendlyAPI
import net.mamoe.mirai.utils.NotStableForInheritance
import java.util.stream.Stream

/**
 * 表示一个群活跃度管理.
 *
 * ## 获取 [GroupActive] 实例
 *
 * 只可以通过 [Group.active] 获取一个群的活跃度管理, 即 [GroupActive] 实例.
 *
 * ### 头衔设置
 *
 * * 通过 [honorShow] 可以获取和设置一个群的荣誉是否显示,
 * * 通过 [titleShow] 可以获取和设置一个群的头衔是否显示,
 * * 通过 [temperatureShow] 可以获取和设置一个群的活跃度是否显示,
 * * 通过 [rankTitles] 可以获取和设置一个群的等级头衔列表 (PC 端显示),
 * * 通过 [temperatureTitles] 可以获取和设置一个群的活跃度头衔列表 (手机端显示)
 *
 * 设置时，修改将异步发送到服务器
 *
 * ### 刷新群成员活跃数据
 *
 * 通过 [flush] 可以刷新 [Member.active] 中的属性
 *
 * ### 活跃度记录
 *
 * 通过 [asFlow] 或 [asStream] 可以获取群活跃度记录*惰性*流,
 * 在从流中收集数据时才会请求服务器获取数据. 通常建议在 Kotlin 使用协程的 [asFlow], 在 Java 使用 [asStream].
 *
 * 若要获取全部活跃度记录, 可使用 [toList].
 *
 * ### 活跃度图表
 *
 * 通过 [getChart] 可以获取活跃度图表，
 * 包括
 * * 每日总人数 [ActiveChart.members]
 * * 每日活跃人数 [ActiveChart.actives]
 * * 每日申请人数 [ActiveChart.sentences]
 * * 每日入群人数 [ActiveChart.join]
 * * 每日退群人数 [ActiveChart.exit]
 * @since 2.13.0
 */
@NotStableForInheritance
public actual interface GroupActive {

    /**
     * 是否在群聊中显示荣誉
     *
     * set 时传入的荣誉头衔显示设置 将会异步发送给api。
     *
     * @see Member.rankTitle
     */
    @MiraiExperimentalApi
    public actual var honorShow: Boolean

    /**
     * 是否在群聊中显示头衔
     *
     * set 时传入的等级头衔显示设置 将会异步发送给api，并刷新等级头衔信息。
     *
     * @see Member.rankTitle
     */
    @MiraiExperimentalApi
    public actual var titleShow: Boolean

    /**
     * 是否在群聊中显示活跃度
     *
     * set 时传入的等级头衔显示设置 将会异步发送给api，并刷新等级头衔信息。
     *
     * @see Member.active
     */
    @MiraiExperimentalApi
    public actual var temperatureShow: Boolean

    /**
     * 等级头衔列表，键是等级，值是头衔
     *
     * set 时传入的等级头衔 将会异步发送给api，并刷新等级头衔信息。
     *
     * @see Member.rankTitle
     */
    @MiraiExperimentalApi
    public actual var rankTitles: Map<Int, String>

    /**
     * 活跃度头衔列表，键是等级，值是头衔
     *
     * set 时传入的活跃度头衔 将会异步发送给api，并刷新活跃度头衔信息。
     *
     * @see Member.rankTitle
     */
    @MiraiExperimentalApi
    public actual var temperatureTitles: Map<Int, String>

    /**
     * 刷新 [Member.active] 中的属性
     * @see Member.active
     */
    public actual suspend fun flush()

    /**
     * 创建一个能获取该群内所有群活跃度记录的 [Flow]. 在 [Flow] 被使用时才会分页下载 [ActiveRecord].
     *
     * 异常不会抛出, 只会记录到网络日志. 当获取发生异常时将会终止获取, 不影响已经成功获取的 [ActiveRecord] 和 [Flow] 的[收集][Flow.collect].
     */
    public actual fun asFlow(): Flow<ActiveRecord>

    /**
     * 创建一个能获取该群内所有群活跃度记录的 [Stream]. 在 [Stream] 被使用时才会分页下载 [ActiveRecord].
     *
     * 异常不会抛出, 只会记录到网络日志. 当获取发生异常时将会终止获取, 不影响已经成功获取的 [ActiveRecord] 和 [Stream] 的[收集][Stream.collect].
     *
     * 实现细节: 为了适合 Java 调用, 实现类似为阻塞式的 [asFlow], 因此不建议在 Kotlin 使用. 在 Kotlin 请使用 [asFlow].
     */
    @JavaFriendlyAPI
    public fun asStream(): Stream<ActiveRecord>

    /**
     * 获取活跃度图表数据，查询失败时返回 null
     */
    public actual suspend fun getChart(): ActiveChart?

    /**
     * 群荣耀历史数据，查询失败时返回 null
     */
    public actual suspend fun getHonorList(type: GroupHonorType): ActiveHonorList?
}