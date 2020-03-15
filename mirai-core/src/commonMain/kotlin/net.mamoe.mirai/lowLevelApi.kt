/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai

import kotlinx.coroutines.Job
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.data.*
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.utils.MiraiExperimentalAPI
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.WeakRef

/**
 * 标示这个 API 是低级的 API.
 *
 * 使用低级的 API 无法带来任何安全和便捷保障.
 * 仅在某些使用结构化 API 可能影响性能的情况下使用这些低级 API.
 */
@RequiresOptIn
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
annotation class LowLevelAPI

/**
 * [Bot] 相关协议层低级 API.
 */
@MiraiExperimentalAPI
@Suppress("FunctionName", "unused")
@LowLevelAPI
interface LowLevelBotAPIAccessor {
    /**
     * 账号信息
     */
    @Deprecated("将来会做修改", level = DeprecationLevel.ERROR)
    @MiraiExperimentalAPI
    @LowLevelAPI
    @MiraiInternalAPI
    abstract val account: BotAccount

    /**
     * 构造一个 [_lowLevelNewQQ] 对象. 它持有对 [Bot] 的弱引用([WeakRef]).
     *
     * [Bot] 无法管理这个对象, 但这个对象会以 [Bot] 的 [Job] 作为父 Job.
     * 因此, 当 [Bot] 被关闭后, 这个对象也会被关闭.
     */
    @LowLevelAPI
    fun _lowLevelNewQQ(friendInfo: FriendInfo): QQ

    /**
     * 向服务器查询群列表. 返回值高 32 bits 为 uin, 低 32 bits 为 groupCode
     */
    @LowLevelAPI
    suspend fun _lowLevelQueryGroupList(): Sequence<Long>

    /**
     * 向服务器查询群资料. 获得的仅为当前时刻的资料.
     * 请优先使用 [Bot.getGroup] 然后查看群资料.
     */
    @LowLevelAPI
    suspend fun _lowLevelQueryGroupInfo(groupCode: Long): GroupInfo

    /**
     * 向服务器查询群成员列表.
     * 请优先使用 [Bot.getGroup], [Group.members] 查看群成员.
     *
     * 这个函数很慢. 请不要频繁使用.
     *
     * @see Group.calculateGroupUinByGroupCode 使用 groupCode 计算 groupUin
     */
    @LowLevelAPI
    suspend fun _lowLevelQueryGroupMemberList(groupUin: Long, groupCode: Long, ownerId: Long): Sequence<MemberInfo>

    /**
     * 撤回一条由机器人发送给好友的消息
     * @param messageId [MessageSource.id]
     */
    @MiraiExperimentalAPI("还未实现")
    @LowLevelAPI
    suspend fun _lowLevelRecallFriendMessage(friendId: Long, messageId: Long, time: Long)

    /**
     * 撤回一条群里的消息. 可以是机器人发送也可以是其他群员发送.
     * @param messageId [MessageSource.id]
     */
    @LowLevelAPI
    suspend fun _lowLevelRecallGroupMessage(groupId: Long, messageId: Long)

    /**
     * 获取群公告列表
     * @param page 页码
     * */
    @LowLevelAPI
    @MiraiExperimentalAPI
    suspend fun _lowLevelGetAnnouncements(groupId: Long, page: Int = 1, amount: Int = 10): GroupAnnouncementList

    /**
     * 发送群公告
     *
     * @return 公告的fid
     * */
    @LowLevelAPI
    @MiraiExperimentalAPI
    suspend fun _lowLevelSendAnnouncement(groupId: Long, announcement: GroupAnnouncement): String


    /**
     * 删除群公告
     * @param fid [GroupAnnouncement.fid]
     * */
    @LowLevelAPI
    @MiraiExperimentalAPI
    suspend fun _lowLevelDeleteAnnouncement(groupId: Long, fid: String)

    /**
     * 获取一条群公告
     * @param fid [GroupAnnouncement.fid]
     * */
    @LowLevelAPI
    @MiraiExperimentalAPI
    suspend fun _lowLevelGetAnnouncement(groupId: Long, fid: String): GroupAnnouncement


    /**
     * 获取群活跃信息
     *
     * */
    @LowLevelAPI
    @MiraiExperimentalAPI
    suspend fun _lowLevelGetGroupActiveData(groupId: Long): GroupActiveData
}

/**
 * 撤回一条群里的消息. 可以是机器人发送也可以是其他群员发送.
 */
@Suppress("FunctionName")
@MiraiExperimentalAPI
@LowLevelAPI
suspend fun LowLevelBotAPIAccessor._lowLevelRecallGroupMessage(
    groupId: Long,
    messageSequenceId: Int,
    messageRandom: Int
) {
    this._lowLevelRecallGroupMessage(
        groupId,
        messageSequenceId.toLong().shl(32) or messageRandom.toLong().and(0xFFFFFFFFL)
    )
}