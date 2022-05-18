/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.contact.announcement

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.announcement.OnlineAnnouncement
import net.mamoe.mirai.internal.contact.GroupImpl
import net.mamoe.mirai.internal.contact.announcement.AnnouncementProtocol.toAnnouncement
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.stream
import java.util.stream.Stream

internal actual class AnnouncementsImpl actual constructor(
    group: GroupImpl,
    logger: MiraiLogger,
) : CommonAnnouncementsImpl(group, logger) {
    override fun asStream(): Stream<OnlineAnnouncement> {
        return stream {
            var i = 1
            while (true) {
                val result = runBlocking { getGroupAnnouncementList(i++) } ?: break

                if (result.inst.isNullOrEmpty() && result.feeds.isNullOrEmpty()) break

                result.inst?.let { yieldAll(it) }
                result.feeds?.let { yieldAll(it) }
            }
        }.map { it.toAnnouncement(group) }
    }
}
