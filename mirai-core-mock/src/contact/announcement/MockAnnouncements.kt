/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.contact.announcement

import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.contact.announcement.*
import net.mamoe.mirai.mock.MockBotDSL
import net.mamoe.mirai.utils.MiraiInternalApi

public interface MockAnnouncements : Announcements {
    /**
     * 直接以 [actor] 的身份推送一则公告
     *
     * @param events 当为 `true` 时会广播相关事件
     * @param announcement 见 [OfflineAnnouncement], [OfflineAnnouncement.create]
     */
    @MockBotDSL
    public fun mockPublish(
        announcement: Announcement,
        actor: NormalMember,
        events: Boolean
    ): OnlineAnnouncement

    @MockBotDSL
    public fun mockPublish(
        announcement: Announcement,
        actor: NormalMember,
    ): OnlineAnnouncement = mockPublish(announcement, actor, false)
}

public class MockOnlineAnnouncement @MiraiInternalApi public constructor(
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

internal fun MockOnlineAnnouncement.copy(
    content: String = this.content,
    parameters: AnnouncementParameters = this.parameters,
    senderId: Long = this.senderId,
    fid: String = this.fid,
    allConfirmed: Boolean = this.allConfirmed,
    confirmedMembersCount: Int = this.confirmedMembersCount,
    publicationTime: Long = this.publicationTime,
): MockOnlineAnnouncement = MockOnlineAnnouncement(
    content = content,
    parameters = parameters,
    senderId = senderId,
    fid = fid,
    allConfirmed = allConfirmed,
    confirmedMembersCount = confirmedMembersCount,
    publicationTime = publicationTime,
)
