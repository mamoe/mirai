/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.contact

import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.contact.announcement.AnnouncementParameters
import net.mamoe.mirai.contact.announcement.OnlineAnnouncement

/**
 * Note: Online is not designed to be serializable
 *
 * @since 2.7
 */
//@SerialName(OnlineAnnouncementImpl.SERIAL_NAME)
//@Serializable(OnlineAnnouncementImpl.Serializer::class)
internal data class OnlineAnnouncementImpl(
    override val group: Group,
    override val senderId: Long,
    override val sender: NormalMember?,
    override val content: String,
    override val parameters: AnnouncementParameters,
    override val fid: String,
    override val allConfirmed: Boolean,
    override val confirmedMembersCount: Int,
    override val publicationTime: Long,
) : OnlineAnnouncement {
//
//    @Serializable
//    private data class SerialData(
//        val botId: Long,
//        val groupId: Long,
//        val memberId: Long,
//        val title: String,
//        val body: String,
//        val parameters: AnnouncementParameters,
//        val fid: String,
//        val isAllRead: Boolean,
//        val readMemberNumber: Int,
//        val publishTime: Long,
//    )
//
//    internal object Serializer : KSerializer<OnlineAnnouncementImpl> by SerialData.serializer().map(
//        SerialData.serializer().descriptor.copy(SERIAL_NAME),
//        {
//            OnlineAnnouncementImpl(
//                sender = Bot.getInstance(botId).getGroupOrFail(groupId).getMemberOrFail(memberId),
//                title = title,
//                body = body,
//                parameters = parameters,
//                fid = Fid(fid),
//                isAllRead = isAllRead,
//                readMemberNumber = readMemberNumber,
//                publishTime = publishTime
//            )
//        },
//        {
//            SerialData(
//                botId = sender.bot.id,
//                groupId = sender.group.id,
//                memberId = sender.id,
//                title = title,
//                body = body,
//                parameters = parameters,
//                fid = fid.toString(),
//                isAllRead = isAllRead,
//                readMemberNumber = readMemberNumber,
//                publishTime = publishTime
//            )
//        }
//    )

//    companion object {
//        const val SERIAL_NAME: String = "ReceiveAnnouncement"
//    }
}