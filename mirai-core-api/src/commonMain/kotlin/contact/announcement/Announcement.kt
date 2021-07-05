/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("INAPPLICABLE_JVM_NAME", "NOTHING_TO_INLINE")
@file:JvmBlockingBridge

package net.mamoe.mirai.contact.announcement

import net.mamoe.kjbb.JvmBlockingBridge
import net.mamoe.mirai.contact.Group


/**
 * 表示一个群公告.
 *
 * @see Announcement
 *
 * @since 2.7
 */
public sealed interface Announcement {
    /**
     * 内容
     */
    public val content: String

    /**
     * 附加参数. 可以通过 [AnnouncementParametersBuilder] 构建获得.
     * @see AnnouncementParameters
     * @see AnnouncementParametersBuilder
     */
    public val parameters: AnnouncementParameters

    /**
     * 在该群发布群公告并获得 [OnlineAnnouncement].
     */
    public suspend fun publishTo(group: Group): OnlineAnnouncement = group.announcements.publish(this)
}

/**
 * 创建 [OfflineAnnouncement]. 若 [this] 类型为 [OfflineAnnouncement] 则直接返回 [this].
 * @since 2.7
 */
public inline fun Announcement.toOffline(): OfflineAnnouncement = OfflineAnnouncement.from(this)


