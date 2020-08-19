/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai

import kotlinx.coroutines.Job
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.data.*
import net.mamoe.mirai.utils.MiraiExperimentalAPI
import net.mamoe.mirai.utils.SinceMirai
import net.mamoe.mirai.utils.WeakRef

/**
 * 标示这个 API 是低级的 API.
 *
 * 低级的 API 可能在任意时刻被改动.
 * 使用低级的 API 无法带来任何安全和便捷保障.
 * 仅在某些使用结构化 API 可能影响性能的情况下使用这些低级 API.
 */
@RequiresOptIn
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
public annotation class LowLevelAPI

/**
 * [Bot] 相关协议层低级 API.
 *
 * **注意**: 不应该把这个类作为一个类型, 只应使用其中的方法
 *
 * **警告**: 所有的低级 API 都可能在任意时刻不经过任何警告和迭代就被修改. 因此非常不建议在任何情况下使用这些 API.
 */
@MiraiExperimentalAPI
@Suppress("FunctionName", "unused")
@LowLevelAPI
public interface LowLevelBotAPIAccessor {
    /**
     * 构造一个 [Friend] 对象. 它持有对 [Bot] 的弱引用([WeakRef]).
     *
     * [Bot] 无法管理这个对象, 但这个对象会以 [Bot] 的 [Job] 作为父 Job.
     * 因此, 当 [Bot] 被关闭后, 这个对象也会被关闭.
     */
    @LowLevelAPI
    public fun _lowLevelNewFriend(friendInfo: FriendInfo): Friend

    /**
     * 向服务器查询群列表. 返回值高 32 bits 为 uin, 低 32 bits 为 groupCode
     */
    @LowLevelAPI
    public suspend fun _lowLevelQueryGroupList(): Sequence<Long>

    /**
     * 向服务器查询群资料. 获得的仅为当前时刻的资料.
     * 请优先使用 [Bot.getGroup] 然后查看群资料.
     */
    @LowLevelAPI
    public suspend fun _lowLevelQueryGroupInfo(groupCode: Long): GroupInfo

    /**
     * 向服务器查询群成员列表.
     * 请优先使用 [Bot.getGroup], [Group.members] 查看群成员.
     *
     * 这个函数很慢. 请不要频繁使用.
     *
     * @see Group.calculateGroupUinByGroupCode 使用 groupCode 计算 groupUin
     */
    @LowLevelAPI
    public suspend fun _lowLevelQueryGroupMemberList(
        groupUin: Long,
        groupCode: Long,
        ownerId: Long
    ): Sequence<MemberInfo>

    /**
     * 获取群公告列表
     * @param page 页码
     */
    @LowLevelAPI
    @MiraiExperimentalAPI
    public suspend fun _lowLevelGetAnnouncements(groupId: Long, page: Int = 1, amount: Int = 10): GroupAnnouncementList

    /**
     * 发送群公告
     *
     * @return 公告的fid
     */
    @LowLevelAPI
    @MiraiExperimentalAPI
    public suspend fun _lowLevelSendAnnouncement(groupId: Long, announcement: GroupAnnouncement): String


    /**
     * 删除群公告
     * @param fid [GroupAnnouncement.fid]
     */
    @LowLevelAPI
    @MiraiExperimentalAPI
    public suspend fun _lowLevelDeleteAnnouncement(groupId: Long, fid: String)

    /**
     * 获取一条群公告
     * @param fid [GroupAnnouncement.fid]
     */
    @LowLevelAPI
    @MiraiExperimentalAPI
    public suspend fun _lowLevelGetAnnouncement(groupId: Long, fid: String): GroupAnnouncement


    /**
     * 获取群活跃信息
     * 不传page可得到趋势图
     * page从0开始传入可以得到发言列表
     */
    @LowLevelAPI
    @MiraiExperimentalAPI
    public suspend fun _lowLevelGetGroupActiveData(groupId: Long, page: Int = -1): GroupActiveData


    /**
     * 处理一个账号请求添加机器人为好友的事件
     */
    @LowLevelAPI
    public suspend fun _lowLevelSolveNewFriendRequestEvent(
        eventId: Long,
        fromId: Long,
        fromNick: String,
        accept: Boolean,
        blackList: Boolean
    )

    /**
     * 处理被邀请加入一个群请求事件
     */
    @LowLevelAPI
    public suspend fun _lowLevelSolveBotInvitedJoinGroupRequestEvent(
        eventId: Long,
        invitorId: Long,
        groupId: Long,
        accept: Boolean
    )

    /**
     * 处理账号请求加入群事件
     */
    @LowLevelAPI
    @SinceMirai("1.2.0")
    public suspend fun _lowLevelSolveMemberJoinRequestEvent(
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
    @LowLevelAPI
    @SinceMirai("1.2.0")
    public suspend fun _lowLevelQueryGroupVoiceDownloadUrl(
        md5: ByteArray,
        groupId: Long,
        dstUin: Long
    ): String

    /**
     * 查询语音的上传连接
     */
    @LowLevelAPI
    @SinceMirai("1.2.0")
    public suspend fun _lowLevelUploadVoice(
        md5: ByteArray,
        groupId: Long,
    )
}
