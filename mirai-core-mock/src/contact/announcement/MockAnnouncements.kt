/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.contact.announcement

import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.contact.announcement.Announcement
import net.mamoe.mirai.contact.announcement.AnnouncementParameters
import net.mamoe.mirai.contact.announcement.Announcements
import net.mamoe.mirai.contact.announcement.OnlineAnnouncement
import net.mamoe.mirai.utils.MiraiInternalApi

public interface MockAnnouncements : Announcements {
    public fun publish0(announcement: Announcement, actor: NormalMember): OnlineAnnouncement
}

public data class MockOnlineAnnouncement @MiraiInternalApi public constructor(
    override val content: String,
    override val parameters: AnnouncementParameters,
    override val senderId: Long,
    override val fid: String = "",
    override val allConfirmed: Boolean,
    override val confirmedMembersCount: Int,
    override val publicationTime: Long
) : OnlineAnnouncement {

    override lateinit var group: Group
    override val sender: NormalMember? get() = group[senderId]
}
