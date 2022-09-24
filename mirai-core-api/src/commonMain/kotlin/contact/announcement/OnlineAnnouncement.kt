/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("INAPPLICABLE_JVM_NAME", "NOTHING_TO_INLINE")
@file:JvmBlockingBridge

package net.mamoe.mirai.contact.announcement

import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.contact.PermissionDeniedException
import net.mamoe.mirai.utils.NotStableForInheritance


/**
 * 表示从 [Announcements.get] 等途径在线获取的, 已经存在于服务器的公告.
 *
 * [OnlineAnnouncement] 拥有唯一识别属性 [fid] 代表其存在于服务器中的 ID. 可进行 [删除][delete]
 *
 * 可在 [Announcement] 获取更多信息.
 *
 * @since 2.7
 */
@NotStableForInheritance
public interface OnlineAnnouncement : Announcement {
    /**
     * 公告所属群
     */
    public val group: Group

    /**
     * 公告发送者 [NormalMember.id]
     */
    public val senderId: Long

    /**
     * 公告发送者. 当该成员已经离开群后为 `null`
     */
    public val sender: NormalMember?

    /**
     * 唯一识别属性
     */
    public val fid: String

    /**
     * 所有人都已阅读, 如果 [AnnouncementParameters.requireConfirmation] 为 `true` 则为所有人都已确认.
     */
    public val allConfirmed: Boolean

    /**
     * 已经阅读的成员数量，如果 [AnnouncementParameters.requireConfirmation] 为 `true` 则为已经确认的成员数量
     */
    public val confirmedMembersCount: Int

    /**
     * 公告发出的时间，为 EpochSecond (自 1970-01-01T00：00：00Z 的秒数)
     *
     * @see java.time.Instant.ofEpochSecond
     */
    public val publicationTime: Long

    /**
     * 删除这个公告. 需要管理员权限. 使用 [Announcements.delete] 与此方法效果相同.
     *
     * @return 成功返回 `true`, 群公告已被删除时返回 `false`
     * @throws PermissionDeniedException 当没有权限时抛出
     * @throws IllegalStateException 当协议异常时抛出
     * @see Announcements.delete
     */
    public suspend fun delete(): Boolean = group.announcements.delete(fid)

    /**
     * 获取 已确认/未确认 的群成员
     *
     * @param confirmed 是否确认
     * @return 群成员列表
     *
     * @throws PermissionDeniedException 当没有权限时抛出
     * @throws IllegalStateException 当协议异常时抛出
     *
     * @see Announcements.members
     */
    public suspend fun members(confirmed: Boolean): List<NormalMember> = group.announcements.members(fid, confirmed)

    /**
     * 提醒 未确认 的群成员
     *
     * @throws PermissionDeniedException 当没有权限时抛出
     * @throws IllegalStateException 当协议异常时抛出
     *
     * @see Announcements.remind
     */
    public suspend fun remind(): Unit = group.announcements.remind(fid)
}

/**
 * 公告所在群所属的 [Bot], 即 `group.bot`.
 * @since 2.7
 */
public inline val OnlineAnnouncement.bot: Bot get() = group.bot
