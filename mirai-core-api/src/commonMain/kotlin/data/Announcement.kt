/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.data

import net.mamoe.mirai.Bot
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.checkBotPermission
import net.mamoe.mirai.containsGroup
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.MiraiExperimentalApi
import java.time.Instant
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * 群公告.
 */
public interface Announcement {

    /**
     * bot的Id
     */
    public val botId: Long

    /**
     * 公告的标题
     */
    public val title: String

    /**
     * 公告的内容
     */
    public val msg: String

    /**
     * 公告的可变参数
     */
    public val announcementParameters: AnnouncementParameters

    /**
     * 发送一个公告
     *
     * @param group 群
     */
    public suspend fun publish(group: Group)
}

/**
 * 群公告的可变参数，不使用则为默认值
 */
public class AnnouncementParameters {

    /**
     * 群公告的图片，目前仅支持发送图片，不支持获得图片
     */

    public var image: ByteArray? = null

    /**
     * 是否发送给新成员
     */
    public var sendToNewMember: Boolean = false

    /**
     * 是否置顶，可以有多个置顶公告
     */
    public var isPinned: Boolean = false

    /**
     * 是否显示能够引导群成员修改昵称的窗口
     */
    public var isShowEditCard: Boolean = false

    /**
     * 是否使用弹窗
     */
    public var isTip: Boolean = false

    /**
     * 是否需要群成员确认
     */
    public var needConfirm: Boolean = false
}

/**
 * 收到的群公告.
 */
public interface ReceiveAnnouncement : Announcement {

    /**
     * 公告发送者的QQ号
     */
    public val senderId: Long

    /**
     * 公告的fid，每个公告仅有一条fid，类似于主键
     */
    public val fid: String

    /**
     * 所有人都已阅读, 如果 [AnnouncementParameters.needConfirm] 为true则为所有人都已确认,
     */
    public val isAllRead: Boolean

    /**
     * 已经阅读的成员数量，如果 [AnnouncementParameters.needConfirm] 为true则为已经确认的成员数量
     */
    public val readMemberNumber: Int

    /**
     * 公告发出的时间，为 EpochSecond (自 1970-01-01T00：00：00Z 的秒数)
     *
     * @see Instant.ofEpochSecond
     */
    public val publishTime: Long
}

/**
 * Announcement的构造函数
 *
 * @param botId bot的id
 * @param title 公告的标题
 * @param msg 公告的信息
 * @param announcementParameters [AnnouncementParameters] 的设置，不构造则为默认值
 * @return [Announcement] 返回构造的Announcement
 */
@Suppress("unused")
public fun buildAnnouncement(
    botId: Long,
    title: String,
    msg: String,
    announcementParameters: AnnouncementParameters = AnnouncementParameters()
): Announcement = AnnouncementImpl(
    botId,
    title,
    msg,
    announcementParameters
)

/**
 * Announcement的构造函数
 *
 * @param botId bot的id
 * @param title 公告的标题
 * @param msg 公告的信息
 * @param announcementParameters [AnnouncementParameters] 的DSL构造函数
 * @return [Announcement] 返回构造的Announcement
 */
@Suppress("unused")
public inline fun buildAnnouncement(
    botId: Long,
    title: String,
    msg: String,
    announcementParameters: AnnouncementParameters.() -> Unit
): Announcement = buildAnnouncement(
    botId,
    title,
    msg,
    AnnouncementParameters().apply(announcementParameters)
)

/**
 * [AnnouncementParameters] 的DSL构造函数
 */
@MiraiExperimentalApi
public inline fun AnnouncementParameters(block: AnnouncementParameters.() -> Unit): AnnouncementParameters {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return AnnouncementParameters().apply(block)
}

/**
 * 发送一个 [Announcement]
 *
 * @param title 公告标题
 * @param msg 公告内容
 * @param announcementParameters 公告设置
 */
@MiraiExperimentalApi
public suspend fun Group.sendAnnouncement(
    title: String,
    msg: String,
    announcementParameters: AnnouncementParameters = AnnouncementParameters()
) {
    checkBotPermission(MemberPermission.ADMINISTRATOR) { "Only administrator have permission to send group announcement" }
    Mirai.sendGroupAnnouncement(
        bot,
        id,
        AnnouncementImpl(bot.id, title, msg, announcementParameters).covertToGroupAnnouncement()
    )
}

///////
// IMPLEMENTATION
internal open class AnnouncementImpl(
    override val botId: Long,
    override val title: String,
    override val msg: String,
    override val announcementParameters: AnnouncementParameters,
) : Announcement {
    override suspend fun publish(group: Group) {
        val bot = Bot.getInstance(botId)
        require(bot.containsGroup(group.id)) { "Bot don't have such group" }
        group.checkBotPermission(MemberPermission.ADMINISTRATOR) { "Only administrator have permission to send group announcement" }
        if (announcementParameters.image == null)
            Mirai.sendGroupAnnouncement(bot, group.id, covertToGroupAnnouncement())
        else {
            val image =
                Mirai.uploadGroupAnnouncementImage(bot, group.id, announcementParameters.image!!.toExternalResource())
            Mirai.sendGroupAnnouncementWithImage(bot, group.id, image, covertToGroupAnnouncement())
        }
    }
}

internal class ReceiveAnnouncementImpl(
    override val botId: Long,
    override val fid: String,
    override val senderId: Long,
    override val title: String,
    override val msg: String,
    override val publishTime: Long,
    override val isAllRead: Boolean,
    override val readMemberNumber: Int,
    override val announcementParameters: AnnouncementParameters
) : AnnouncementImpl(botId, title, msg, announcementParameters),
    ReceiveAnnouncement


internal fun GroupAnnouncement.covertToAnnouncement(botId: Long): ReceiveAnnouncement {
    check(this.fid != null) { "GroupAnnouncement don't have id" }
    check(this.settings != null) { "GroupAnnouncement don't have setting" }

    return ReceiveAnnouncementImpl(
        botId = botId,
        fid = fid,
        senderId = sender,
        publishTime = time,
        title = msg.title ?: "",
        msg = msg.text,
        readMemberNumber = readNum,
        isAllRead = isAllConfirm != 0,
        announcementParameters = AnnouncementParameters {
            isPinned = pinned == 1
            sendToNewMember = type == 20
            isTip = settings.tipWindowType == 0
            needConfirm = settings.confirmRequired == 1
            isShowEditCard = settings.isShowEditCard == 1
        }
    )
}

internal fun Announcement.covertToGroupAnnouncement(): GroupAnnouncement {
    return GroupAnnouncement(
        sender = botId,
        msg = GroupAnnouncementMsg(
            title = title,
            text = msg
        ),
        type = if (announcementParameters.sendToNewMember) 20 else 6,
        settings = GroupAnnouncementSettings(
            isShowEditCard = if (announcementParameters.isShowEditCard) 1 else 0,
            tipWindowType = if (announcementParameters.isTip) 0 else 1,
            confirmRequired = if (announcementParameters.needConfirm) 1 else 0,
        ),
        pinned = if (announcementParameters.isPinned) 1 else 0,
    )
}