/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.contact

import net.mamoe.mirai.data.Announcement
import net.mamoe.mirai.data.ReadAnnouncement
import net.mamoe.mirai.utils.MiraiExperimentalApi

@MiraiExperimentalApi
internal data class ReadAnnouncementImpl(
    override val fid: String,
    override val senderId: Long,
    override val title: String,
    override val msg: String,
    override val sendToNewMember: Boolean,
    override val publishTime: Long,
    override val isPinned: Boolean,
    override val isShowEditCard: Boolean,
    override val isTip: Boolean,
    override val needConfirm: Boolean,
    override val isAllRead: Boolean,
    override val readMemberNumber: Int
) : ReadAnnouncement

@MiraiExperimentalApi
internal open class AnnouncementImpl(
    override val senderId: Long,
    override val title: String,
    override val msg: String,
    override val sendToNewMember: Boolean = false,
    override val isPinned: Boolean = false,
    override val isShowEditCard: Boolean = false,
    override val isTip: Boolean = false,
    override val needConfirm: Boolean = false
) : Announcement