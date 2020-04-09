package net.mamoe.mirai.data

import net.mamoe.mirai.Bot
import net.mamoe.mirai.LowLevelAPI

/**
 * 群资料.
 *
 * 通过 [Bot._lowLevelQueryGroupInfo] 得到
 */
@LowLevelAPI
interface GroupInfo {
    /**
     * Uin
     */
    val uin: Long

    /**
     * 群号码
     */ // 由 uin 计算得到
    val groupCode: Long

    /**
     * 名称
     */
    val name: String // 不一定能获取到

    /**
     * 群主
     */
    val owner: Long // 不一定能获取到

    /**
     * 入群公告
     */
    val memo: String // 不一定能获取到

    /**
     * 允许群员邀请其他人加入群
     */
    val allowMemberInvite: Boolean

    /**
     * 允许匿名聊天
     */
    val allowAnonymousChat: Boolean

    /**
     * 自动审批加群请求
     */
    val autoApprove: Boolean

    /**
     * 坦白说开启状态
     */
    val confessTalk: Boolean

    /**
     * 全员禁言
     */
    val muteAll: Boolean

    /**
     * 机器人被禁言还剩时间, 秒.
     */
    val botMuteTimestamp: Int

    /*
    /**
     * 机器人的头衔
     */
    val botSpecialTitle: String

    /**
     * 机器人的昵称
     */
    val botNameCard: String*/
}