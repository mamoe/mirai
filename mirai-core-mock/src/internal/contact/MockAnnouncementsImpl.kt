/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.internal.contact

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.contact.announcement.Announcement
import net.mamoe.mirai.contact.announcement.AnnouncementImage
import net.mamoe.mirai.contact.announcement.OnlineAnnouncement
import net.mamoe.mirai.event.events.GroupEntranceAnnouncementChangeEvent
import net.mamoe.mirai.mock.contact.announcement.MockAnnouncements
import net.mamoe.mirai.mock.contact.announcement.MockOnlineAnnouncement
import net.mamoe.mirai.mock.utils.broadcastBlocking
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.currentTimeSeconds
import net.mamoe.mirai.utils.generateImageId
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Stream

internal class MockAnnouncementsImpl(
    val group: Group,
) : MockAnnouncements {
    val announcements = ConcurrentHashMap<String, OnlineAnnouncement>()

    override suspend fun asFlow(): Flow<OnlineAnnouncement> = announcements.values.asFlow()

    override fun asStream(): Stream<OnlineAnnouncement> = announcements.values.toList().stream()

    override suspend fun delete(fid: String): Boolean = announcements.remove(fid) != null

    override suspend fun get(fid: String): OnlineAnnouncement? = announcements[fid]

    internal fun putDirect(announcement: MockOnlineAnnouncement) {
        val annoc = if (announcement.fid.isEmpty()) {
            announcement.copy(fid = UUID.randomUUID().toString())
        } else announcement
        if (annoc.parameters.sendToNewMember) {
            announcements.entries.removeIf { (_, v) -> v.parameters.sendToNewMember }
        }
        announcements[annoc.fid] = annoc
        annoc.group = group
    }

    override fun publish0(announcement: Announcement, actor: NormalMember): OnlineAnnouncement {
        val old = if (announcement.parameters.sendToNewMember)
            announcements.elements().toList().firstOrNull { oa -> oa.parameters.sendToNewMember }
        else null
        val onac = MockOnlineAnnouncement(
            content = announcement.content,
            parameters = announcement.parameters,
            senderId = actor.id,
            fid = UUID.randomUUID().toString(),
            allConfirmed = false,
            confirmedMembersCount = 0,
            publicationTime = currentTimeSeconds()
        )
        putDirect(onac)
        GroupEntranceAnnouncementChangeEvent(
            old?.content ?: "",
            announcement.content,
            group,
            actor
        ).broadcastBlocking()
        return onac
    }

    override suspend fun publish(announcement: Announcement): OnlineAnnouncement {
        // TODO: CheckPermission
        return publish0(announcement, this.group.botAsMember)
    }

    override suspend fun uploadImage(resource: ExternalResource): AnnouncementImage = resource.inResource {
        AnnouncementImage.create(generateImageId(resource.md5), 500, 500)
    }
}
