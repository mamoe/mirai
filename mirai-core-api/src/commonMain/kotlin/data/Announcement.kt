/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused")
@file:JvmBlockingBridge

package net.mamoe.mirai.data

import net.mamoe.kjbb.JvmBlockingBridge
import net.mamoe.mirai.Bot
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.checkBotPermission
import net.mamoe.mirai.containsGroup
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.MiraiExperimentalApi
import java.time.Instant

/**
 * 表示一个群公告. [ReceiveAnnouncement] 表示
 *
 * 可通过 [Announcement.create] 构造.
 *
 * @see Announcement
 */
public open class Announcement internal constructor(
    /**
     * bot的Id
     */
    public val botId: Long,

    /**
     * 公告的标题
     */
    public val title: String,

    /**
     * 公告的内容
     */
    public val msg: String,

    /**
     * 公告的可变参数. 可以通过 [AnnouncementParametersBuilder] 构建获得.
     * @see AnnouncementParameters
     * @see AnnouncementParametersBuilder
     */
    public val parameters: AnnouncementParameters
) {
    /**
     * 发送该公告到群
     */
    public suspend fun publish(group: Group) {
        val bot = Bot.getInstance(botId)
        require(bot.containsGroup(group.id)) { "Bot don't have such group" }
        group.checkBotPermission(MemberPermission.ADMINISTRATOR) { "Only administrator have permission to send group announcement" }
        if (parameters.image == null)
            Mirai.sendGroupAnnouncement(bot, group.id, covertToGroupAnnouncement())
        else {
            val image =
                Mirai.uploadGroupAnnouncementImage(bot, group.id, parameters.image.toExternalResource())
            Mirai.sendGroupAnnouncementWithImage(bot, group.id, image, covertToGroupAnnouncement())
        }
    }

    public companion object {
        /**
         * 构造一个 [Announcement].
         * @see Announcement
         */
        @JvmStatic
        public fun create(botId: Long, title: String, msg: String, parameters: AnnouncementParameters): Announcement {
            return Announcement(botId, title, msg, parameters)
        }
    }
}

/**
 * 群公告的扩展参数.
 *
 * 可通过 [AnnouncementParametersBuilder] 构建. [AnnouncementParameters] 用于 [创建公告][Announcement.create].
 */
public class AnnouncementParameters internal constructor(
    /**
     * 群公告的图片，目前仅支持发送图片，不支持获得图片
     */
    public val image: ByteArray? = null,

    /**
     * 是否发送给新成员
     */
    public val sendToNewMember: Boolean = false,

    /**
     * 是否置顶，可以有多个置顶公告
     */
    public val isPinned: Boolean = false,

    /**
     * 是否显示能够引导群成员修改昵称的窗口
     */
    public val isShowEditCard: Boolean = false,

    /**
     * 是否使用弹窗
     */
    public val isTip: Boolean = false,

    /**
     * 是否需要群成员确认
     */
    public val needConfirm: Boolean = false,
) {
    /**
     * 以该对象的参数创建一个 [AnnouncementParametersBuilder].
     */
    public fun builder(): AnnouncementParametersBuilder = AnnouncementParametersBuilder().apply {
        val outer = this@AnnouncementParameters
        this.image = outer.image
        this.sendToNewMember = outer.sendToNewMember
        this.isPinned = outer.isPinned
        this.isShowEditCard = outer.isShowEditCard
        this.isTip = outer.isTip
        this.needConfirm = outer.needConfirm
    }
}

/**
 * 表示一个收到的群公告. 只能由 mirai 构造.
 */
public class ReceiveAnnouncement internal constructor(
    /**
     * bot的 Id
     */
    botId: Long,
    /**
     * 公告的标题
     */
    title: String,
    /**
     * 公告的内容
     */
    msg: String,
    /**
     * 公告的可变参数
     */
    parameters: AnnouncementParameters,
    /**
     * 公告发送者的 QQ 号
     */
    public val senderId: Long,

    /**
     * 公告的 `fid，每个公告仅有一条 `fid`，类似于主键
     */
    public val fid: String,

    /**
     * 所有人都已阅读, 如果 [AnnouncementParameters.needConfirm] 为 `true` 则为所有人都已确认,
     */
    public val isAllRead: Boolean,

    /**
     * 已经阅读的成员数量，如果 [AnnouncementParameters.needConfirm] 为 `true` 则为已经确认的成员数量
     */
    public val readMemberNumber: Int,

    /**
     * 公告发出的时间，为 EpochSecond (自 1970-01-01T00：00：00Z 的秒数)
     *
     * @see Instant.ofEpochSecond
     */
    public val publishTime: Long,
) : Announcement(botId, title, msg, parameters)

/**
 * [AnnouncementParameters] 的构建器. 可以构建一个 [AnnouncementParameters] 实例.
 *
 * ## 获得实例
 *
 * 直接构造实例: `new AnnouncementParametersBuilder()` 或者从已有的公告中获取 [AnnouncementParameters.builder].
 *
 * ## 使用
 *
 * ### 在 Kotlin 使用
 *
 * ```
 * val parameters = buildAnnouncementParameters {
 *     sendToNewMember = true
 *     // ...
 * }
 * ```
 *
 * ### 在 Java 使用
 *
 * ```java
 * AnnouncementParameters parameters = new AnnouncementParametersBuilder()
 *         .sendToNewMember(true)
 *         .pinned(true)
 *         .build();
 * ```
 *
 * @see buildAnnouncementParameters
 */
public class AnnouncementParametersBuilder @JvmOverloads constructor(
    prototype: AnnouncementParameters = AnnouncementParameters()
) {
    public var image: ByteArray? = prototype.image
    public var sendToNewMember: Boolean = prototype.sendToNewMember
    public var isPinned: Boolean = prototype.isPinned
    public var isShowEditCard: Boolean = prototype.isShowEditCard
    public var isTip: Boolean = prototype.isTip
    public var needConfirm: Boolean = prototype.needConfirm

    public fun image(image: ByteArray?): AnnouncementParametersBuilder {
        this.image = image
        return this
    }

    public fun sendToNewMember(sendToNewMember: Boolean): AnnouncementParametersBuilder {
        this.sendToNewMember = sendToNewMember
        return this
    }

    public fun pinned(isPinned: Boolean): AnnouncementParametersBuilder {
        this.isPinned = isPinned
        return this
    }

    public fun showEditCard(isShowEditCard: Boolean): AnnouncementParametersBuilder {
        this.isShowEditCard = isShowEditCard
        return this
    }

    public fun tip(isTip: Boolean): AnnouncementParametersBuilder {
        this.isTip = isTip
        return this
    }

    public fun needConfirm(needConfirm: Boolean): AnnouncementParametersBuilder {
        this.needConfirm = needConfirm
        return this
    }

    /**
     * 使用当前参数构造 [AnnouncementParameters].
     */
    public fun build(): AnnouncementParameters =
        AnnouncementParameters(image, sendToNewMember, isPinned, isShowEditCard, isTip, needConfirm)
}

/**
 * 使用 [AnnouncementParametersBuilder] 构建 [AnnouncementParameters].
 * @see AnnouncementParametersBuilder
 */
public inline fun buildAnnouncementParameters(
    builderAction: AnnouncementParametersBuilder.() -> Unit
): AnnouncementParameters = AnnouncementParametersBuilder().apply(builderAction).build()

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
        Announcement(bot.id, title, msg, announcementParameters).covertToGroupAnnouncement()
    )
}

internal fun GroupAnnouncement.covertToAnnouncement(botId: Long): ReceiveAnnouncement {
    check(this.fid != null) { "GroupAnnouncement don't have id" }
    check(this.settings != null) { "GroupAnnouncement don't have setting" }

    return ReceiveAnnouncement(
        botId = botId,
        fid = fid,
        senderId = sender,
        publishTime = time,
        title = msg.title ?: "",
        msg = msg.text,
        readMemberNumber = readNum,
        isAllRead = isAllConfirm != 0,
        parameters = buildAnnouncementParameters {
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
        type = if (parameters.sendToNewMember) 20 else 6,
        settings = GroupAnnouncementSettings(
            isShowEditCard = if (parameters.isShowEditCard) 1 else 0,
            tipWindowType = if (parameters.isTip) 0 else 1,
            confirmRequired = if (parameters.needConfirm) 1 else 0,
        ),
        pinned = if (parameters.isPinned) 1 else 0,
    )
}