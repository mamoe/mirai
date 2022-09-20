/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.data

import net.mamoe.mirai.LowLevelApi

/**
 * 群资料.
 */
@LowLevelApi
public interface GroupInfo {
    /**
     * Uin
     */
    public val uin: Long

    /**
     * 群号码
     */ // 由 uin 计算得到
    public val groupCode: Long

    /**
     * 名称
     */
    public val name: String // 不一定能获取到

    /**
     * 群主
     */
    public val owner: Long // 不一定能获取到

    /**
     * 入群公告
     */
    public val memo: String // 不一定能获取到

    /**
     * 允许群员邀请其他人加入群
     */
    public val allowMemberInvite: Boolean

    /**
     * 允许匿名聊天
     */
    public val allowAnonymousChat: Boolean

    /**
     * 自动审批加群请求
     */
    public val autoApprove: Boolean

    /**
     * 坦白说开启状态
     */
    public val confessTalk: Boolean

    /**
     * 全员禁言
     */
    public val muteAll: Boolean

    /**
     * 机器人被禁言还剩时间, 秒.
     */
    public val botMuteTimestamp: Int

    /**
     * 荣誉是否显示
     * @since 2.13
     */
    public val isHonorVisible: Boolean

    /**
     * 头衔是否显示
     * @since 2.13
     */
    public val isTitleVisible: Boolean

    /**
     * 活跃度是否显示
     * @since 2.13
     */
    public val isTemperatureVisible: Boolean

    /**
     * 等级头衔
     * @since 2.13
     */
    public val rankTitles: Map<Int, String>

    /**
     * 活跃度头衔
     * @since 2.13
     */
    public val temperatureTitles: Map<Int, String>
}