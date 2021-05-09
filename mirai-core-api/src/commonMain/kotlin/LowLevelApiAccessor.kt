/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmBlockingBridge

package net.mamoe.mirai

import kotlinx.coroutines.Job
import net.mamoe.kjbb.JvmBlockingBridge
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.data.*
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.WeakRef
import kotlin.annotation.AnnotationTarget.*

/**
 * 标示这个 API 是低级的 API.
 *
 * 低级的 API 可能在任意时刻被改动.
 * 使用低级的 API 无法带来任何安全和便捷保障.
 * 仅在某些使用结构化 API 可能影响性能的情况下使用这些低级 API.
 */
@MiraiExperimentalApi
@RequiresOptIn
@Retention(AnnotationRetention.BINARY)
@Target(CLASS, TYPE, FUNCTION, PROPERTY, CONSTRUCTOR)
public annotation class LowLevelApi

/**
 * [IMirai] 协议层低级 API.
 *
 * **警告**: 所有的低级 API 都可能在任意时刻不经过任何警告和迭代就被修改. 因此非常不建议在任何情况下使用这些 API.
 */
@LowLevelApi
public interface LowLevelApiAccessor {
    /**
     * 主动刷新 keys, 如 SKey, PSKey 等.
     *
     * 通常 mirai 会自动刷新, 不需要手动刷新.
     *
     * @since 2.2
     */
    @MiraiExperimentalApi
    public suspend fun refreshKeys(bot: Bot)

    /**
     * 构造一个 [Friend] 对象. 它持有对 [Bot] 的弱引用([WeakRef]).
     *
     * [Bot] 无法管理这个对象, 但这个对象会以 [Bot] 的 [Job] 作为父 Job.
     * 因此, 当 [Bot] 被关闭后, 这个对象也会被关闭.
     */
    @LowLevelApi
    public fun newFriend(bot: Bot, friendInfo: FriendInfo): Friend

    /**
     * 构造一个 [Stranger] 对象. 它持有对 [Bot] 的弱引用([WeakRef]).
     *
     * [Bot] 无法管理这个对象, 但这个对象会以 [Bot] 的 [Job] 作为父 Job.
     * 因此, 当 [Bot] 被关闭后, 这个对象也会被关闭.
     */
    @LowLevelApi
    public fun newStranger(bot: Bot, strangerInfo: StrangerInfo): Stranger

    /**
     * 撤回这条消息. 不会广播事件, 不会有安全性检查.
     * @see recallMessage
     */
    @LowLevelApi
    public suspend fun recallGroupMessageRaw(
        bot: Bot,
        groupCode: Long,
        messageIds: IntArray,
        messageInternalIds: IntArray,
    ): Boolean

    /**
     * 撤回这条消息. 不会广播事件, 不会有安全性检查.
     * @see recallMessage
     */
    @LowLevelApi
    public suspend fun recallFriendMessageRaw(
        bot: Bot,
        targetId: Long,
        messageIds: IntArray,
        messageInternalIds: IntArray,
        time: Int,
    ): Boolean

    /**
     * 撤回这条消息. 不会广播事件, 不会有安全性检查.
     * @see recallMessage
     */
    @LowLevelApi
    public suspend fun recallGroupTempMessageRaw(
        bot: Bot,
        groupUin: Long,
        targetId: Long,
        messageIds: IntArray,
        messageInternalIds: IntArray,
        time: Int,
    ): Boolean

    /**
     * 向服务器查询群列表. 返回值高 32 bits 为 uin, 低 32 bits 为 groupCode
     */
    @LowLevelApi
    public suspend fun getRawGroupList(bot: Bot): Sequence<Long>

    /**
     * 向服务器查询群成员列表.
     * 请优先使用 [Bot.getGroup], [Group.members] 查看群成员.
     *
     * 这个函数很慢. 请不要频繁使用.
     *
     * @see IMirai.calculateGroupUinByGroupCode 使用 groupCode 计算 groupUin
     */
    @LowLevelApi
    public suspend fun getRawGroupMemberList(
        bot: Bot,
        groupUin: Long,
        groupCode: Long,
        ownerId: Long
    ): Sequence<MemberInfo>

    /**
     * 获取群公告列表
     * @param page 页码
     */
    @LowLevelApi
    @MiraiExperimentalApi
    public suspend fun getRawGroupAnnouncements(
        bot: Bot,
        groupId: Long,
        page: Int = 1,
        amount: Int = 10
    ): GroupAnnouncementList

    /**
     * 发送群公告
     *
     * @return 公告的fid
     */
    @LowLevelApi
    @MiraiExperimentalApi
    public suspend fun sendGroupAnnouncement(
        bot: Bot,
        groupId: Long,
        announcement: GroupAnnouncement
    ): String


    /**
     * 删除群公告
     * @param fid [GroupAnnouncement.fid]
     */
    @LowLevelApi
    @MiraiExperimentalApi
    public suspend fun deleteGroupAnnouncement(
        bot: Bot,
        groupId: Long,
        fid: String
    )

    /**
     * 获取一条群公告
     * @param fid [GroupAnnouncement.fid]
     */
    @LowLevelApi
    @MiraiExperimentalApi
    public suspend fun getGroupAnnouncement(
        bot: Bot,
        groupId: Long,
        fid: String
    ): GroupAnnouncement


    /**
     * 获取群活跃信息
     * 不传page可得到趋势图
     * page从0开始传入可以得到发言列表
     */
    @LowLevelApi
    @MiraiExperimentalApi
    public suspend fun getRawGroupActiveData(bot: Bot, groupId: Long, page: Int = -1): GroupActiveData


    /**
     * 获取群荣誉信息
     */
    @LowLevelApi
    @MiraiExperimentalApi
    public suspend fun getRawGroupHonorListData(
        bot: Bot,
        groupId: Long,
        type: GroupHonorType
    ): GroupHonorListData?


    /**
     * 处理一个账号请求添加机器人为好友的事件
     */
    @LowLevelApi
    public suspend fun solveNewFriendRequestEvent(
        bot: Bot,
        eventId: Long,
        fromId: Long,
        fromNick: String,
        accept: Boolean,
        blackList: Boolean
    )

    /**
     * 处理被邀请加入一个群请求事件
     */
    @LowLevelApi
    public suspend fun solveBotInvitedJoinGroupRequestEvent(
        bot: Bot,
        eventId: Long,
        invitorId: Long,
        groupId: Long,
        accept: Boolean
    )

    /**
     * 处理账号请求加入群事件
     */
    @LowLevelApi
    public suspend fun solveMemberJoinRequestEvent(
        bot: Bot,
        eventId: Long,
        fromId: Long,
        fromNick: String,
        groupId: Long,
        accept: Boolean?,
        blackList: Boolean,
        message: String = ""
    )

    /**
     * 查询语音的下载连接
     */
    @LowLevelApi
    public suspend fun getGroupVoiceDownloadUrl(
        bot: Bot,
        md5: ByteArray,
        groupId: Long,
        dstUin: Long
    ): String

    /**
     * 禁言一个匿名用户
     *
     * @param anonymousId [AnonymousMember.anonymousId]
     */
    @LowLevelApi
    public suspend fun muteAnonymousMember(
        bot: Bot,
        anonymousId: String,
        anonymousNick: String,
        groupId: Long,
        seconds: Int,
    )

}
