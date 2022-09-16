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
import net.mamoe.mirai.data.GroupHonorType
import net.mamoe.mirai.utils.NotStableForInheritance

/**
 * 群活跃度相关属性
 * @since 2.13
 */
@NotStableForInheritance
public interface MemberActive {

    /**
     * 群活跃等级. 取值为 1~6 (包含)
     *
     * 这个等级是在 PC 端成员管理功能中显示的等级
     *
     * @see point
     */
    public val rank: Int

    /**
     * 群活跃积分.
     *
     * 这个积分是在 PC 端成员管理功能中显示的积分，和手机端显示的 群荣誉活跃积分 不同
     *
     * @see rank
     */
    public val point: Int

    /**
     * 群荣誉标识.
     */
    public val honors: Set<GroupHonorType>

    /**
     * 群荣誉等级. 取值为 1~100 (包含)
     *
     * 这个等级是在手机端群荣誉功能中显示的等级
     */
    public val temperature: Int

    /**
     * 查询头衔佩戴情况
     */
    public suspend fun queryMedal(): MemberMedalInfo
}