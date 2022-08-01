/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("DEPRECATION")

package net.mamoe.mirai.internal.contact.announcement

import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.announcement.*
import net.mamoe.mirai.contact.checkBotPermission
import net.mamoe.mirai.internal.AbstractBot
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.contact.GroupImpl
import net.mamoe.mirai.internal.contact.OnlineAnnouncementImpl
import net.mamoe.mirai.internal.contact.announcement.AnnouncementProtocol.deleteGroupAnnouncement
import net.mamoe.mirai.internal.contact.announcement.AnnouncementProtocol.getGroupAnnouncement
import net.mamoe.mirai.internal.contact.announcement.AnnouncementProtocol.getRawGroupAnnouncements
import net.mamoe.mirai.internal.contact.announcement.AnnouncementProtocol.sendGroupAnnouncement
import net.mamoe.mirai.internal.contact.announcement.AnnouncementProtocol.toAnnouncement
import net.mamoe.mirai.internal.contact.announcement.AnnouncementProtocol.toGroupAnnouncement
import net.mamoe.mirai.internal.network.client
import net.mamoe.mirai.internal.network.components.HttpClientProvider
import net.mamoe.mirai.internal.network.highway.ChannelKind
import net.mamoe.mirai.internal.network.highway.ResourceKind
import net.mamoe.mirai.internal.network.highway.tryServersUpload
import net.mamoe.mirai.internal.network.psKey
import net.mamoe.mirai.internal.network.sKey
import net.mamoe.mirai.internal.utils.io.writeResource
import net.mamoe.mirai.utils.*
import net.mamoe.mirai.utils.Either.Companion.onLeft
import net.mamoe.mirai.utils.Either.Companion.rightOrNull

internal expect class AnnouncementsImpl(
    group: GroupImpl,
    logger: MiraiLogger,
) : CommonAnnouncementsImpl

internal abstract class CommonAnnouncementsImpl(
    protected val group: GroupImpl,
    protected val logger: MiraiLogger,
) : Announcements {
    inline val bot get() = group.bot

    protected suspend fun getGroupAnnouncementList(i: Int): GroupAnnouncementList? {
        return bot.getRawGroupAnnouncements(group.id, i).onLeft {
            if (logger.isEnabled) { // createException
                logger.warning(
                    { "Failed to load announcement for group ${group.id}" },
                    it.createException()
                )
            }
        }.rightOrNull
    }

    override suspend fun asFlow(): Flow<OnlineAnnouncement> {
        return flow {
            var i = 1
            while (true) {
                val result = getGroupAnnouncementList(i++) ?: break

                if (result.inst.isNullOrEmpty() && result.feeds.isNullOrEmpty()) break

                result.inst?.let { emitAll(it.asFlow()) }
                result.feeds?.let { emitAll(it.asFlow()) }
            }
        }.map { it.toAnnouncement(group) }
    }


    override suspend fun delete(fid: String): Boolean {
        group.checkBotPermission(MemberPermission.ADMINISTRATOR) { "Only administrator have permission to delete group announcement" }
        return bot.deleteGroupAnnouncement(group.id, fid)
    }

    override suspend fun get(fid: String): OnlineAnnouncement {
        return bot.getGroupAnnouncement(group.id, fid).toAnnouncement(group)
    }


    private fun Announcement.describe(): String =
        "'${content.truncated(10)}' ${parameters.describe()}"

    private fun AnnouncementParameters.describe(): String {
        return mutableListOf<String>().apply {
            if (image != null) add("with image")
            if (sendToNewMember) add("sendToNewMember")
            if (isPinned) add("pinned")
            if (showEditCard) add("showEditCard")
            if (showPopup) add("popup")
            if (requireConfirmation) add("needConfirm")
        }.joinToString()
    }

    override suspend fun publish(announcement: Announcement): OnlineAnnouncement = announcement.run {
        val id = announcement.hashCode()
        logger.verbose { "Publishing announcement #$id: ${announcement.describe()}" }
        val bot = group.bot
        group.checkBotPermission(MemberPermission.ADMINISTRATOR) { "Only administrator have permission to send group announcement" }
        val image = parameters.image
        val fid = bot.sendGroupAnnouncement(group.id, toGroupAnnouncement(bot.id), image)

        return OnlineAnnouncementImpl(
            group = group,
            senderId = bot.id,
            sender = group.botAsMember,
            content = content,
            parameters = parameters,
            fid = fid,
            allConfirmed = false,
            confirmedMembersCount = 0,
            publicationTime = currentTimeSeconds()
        ).also {
            logger.verbose { "Publishing announcement #$id: success." }
        }
    }

    override suspend fun uploadImage(resource: ExternalResource): AnnouncementImage {
        return tryServersUpload(
            bot,
            serversStub,
            resource.size,
            ResourceKind.ANNOUNCEMENT_IMAGE,
            ChannelKind.HTTP
        ) { _, _ ->
            // use common logging

            AnnouncementProtocol.uploadGroupAnnouncementImage(bot, resource)
        }
    }
}

private val serversStub = listOf("web.qun.qq.com" to 80)

internal object AnnouncementProtocol {
    @Serializable
    data class UploadImageResp(
        @SerialName("ec") override val errorCode: Int = 0,
        @SerialName("em") override val errorMessage: String? = null,
        @SerialName("id") val id: String,
    ) : CheckableResponseA(), JsonStruct

    suspend fun uploadGroupAnnouncementImage(
        bot: AbstractBot,
        resource: ExternalResource
    ): AnnouncementImage = bot.run {
        val resp = bot.components[HttpClientProvider].getHttpClient().post {
            url("https://web.qun.qq.com/cgi-bin/announce/upload_img")
            setBody(
                MultiPartFormDataContent(formData {
                    append("\"bkn\"", client.wLoginSigInfo.bkn)
                    append("\"source\"", "troopNotice")
                    append("m", "0")
                    append(
                        "\"pic_up\"",
                        headers = Headers.build {
                            append(HttpHeaders.ContentType, ContentType.Image.PNG)
                            append(HttpHeaders.ContentDisposition, "filename=\"temp_uploadFile.png\"")
                        }
                    ) {
                        writeResource(resource)
                    }
                })
            )
            cookie("uin", "o$id")
            cookie("p_uin", "o$id")
            cookie("skey", sKey)
            cookie("p_skey", psKey("qun.qq.com"))
        }.bodyAsText().loadSafelyAs(UploadImageResp.serializer()).check()
        return resp.id.replace("&quot;", "\"").loadSafelyAs(GroupAnnouncementImage.serializer()).check().toPublic()
    }

    @Serializable
    data class SendGroupAnnouncementResp(
        @SerialName("ec") override val errorCode: Int = 0,
        @SerialName("em") override val errorMessage: String? = null,
        @SerialName("new_fid") val fid: String,
    ) : CheckableResponseA(), JsonStruct

    suspend fun QQAndroidBot.sendGroupAnnouncement(
        groupId: Long,
        announcement: GroupAnnouncement,
        image: AnnouncementImage?,
    ): String {
        return bot.components[HttpClientProvider].getHttpClient().post {
            url(
                "https://web.qun.qq.com/cgi-bin/announce/add_qun_" + if (announcement.type == 20) {
                    "instruction"
                } else {
                    "notice"
                }
            )
            setBody(
                MultiPartFormDataContent(formData {
                    append("qid", groupId)
                    append("bkn", client.wLoginSigInfo.bkn)
                    append("text", announcement.msg.text)
                    append("pinned", announcement.pinned)
                    image?.let {
                        append("pic", image.id)
                        append("imgWidth", image.width)
                        append("imgHeight", image.height)
                    }
                    append(
                        "settings",
                        announcement.settings.toJsonString(GroupAnnouncementSettings.serializer()),
                    )
                    append("format", "json")
                    // append("type", announcement.type.toString())
                })
            )
            cookie("uin", "o$id")
            cookie("p_uin", "o$id")
            cookie("skey", sKey)
            cookie("p_skey", psKey("qun.qq.com"))
        }.bodyAsText().loadSafelyAs(SendGroupAnnouncementResp.serializer()).check().fid
    }

    suspend fun QQAndroidBot.getRawGroupAnnouncements(
        groupId: Long,
        page: Int,
        amount: Int = 10
    ): Either<DeserializationFailure, GroupAnnouncementList> {
        return bot.components[HttpClientProvider].getHttpClient().post {
            url("https://web.qun.qq.com/cgi-bin/announce/list_announce")
            setBody(
                MultiPartFormDataContent(formData {
                    append("qid", groupId)
                    append("bkn", client.wLoginSigInfo.bkn)
                    append("ft", 23)  //好像是一个用来识别应用的参数
                    append("s", if (page == 1) 0 else -(page * amount + 1))  // 第一页这里的参数应该是-1
                    append("n", amount)
                    append("ni", if (page == 1) 1 else 0)
                    append("format", "json")
                })
            )
            cookie("uin", "o$id")
            cookie("skey", sKey)
        }.bodyAsText().loadSafelyAs(GroupAnnouncementList.serializer())
    }

    @Serializable
    data class DeleteResp(
        @SerialName("ec") override val errorCode: Int = 0,
        @SerialName("em") override val errorMessage: String? = null,
    ) : CheckableResponseA(), JsonStruct

    suspend fun QQAndroidBot.deleteGroupAnnouncement(groupId: Long, fid: String): Boolean {
        components[HttpClientProvider].getHttpClient().post {
            url("https://web.qun.qq.com/cgi-bin/announce/del_feed")
            setBody(feedBody(groupId, fid))
            cookie("uin", "o$id")
            cookie("p_uin", "o$id")
            cookie("skey", sKey)
            cookie("p_skey", psKey("qun.qq.com"))
        }.bodyAsText().loadSafelyAs(DeleteResp.serializer()).check()
        return true
    }

    suspend fun QQAndroidBot.getGroupAnnouncement(groupId: Long, fid: String): GroupAnnouncement {
        return bot.components[HttpClientProvider].getHttpClient().post {
            url("https://web.qun.qq.com/cgi-bin/announce/get_feed")
            setBody(feedBody(groupId, fid))
            cookie("uin", "o$id")
            cookie("p_uin", "o$id")
            cookie("skey", sKey)
        }.bodyAsText().loadAs(GroupAnnouncement.serializer())
    }

    private fun QQAndroidBot.feedBody(
        groupId: Long,
        fid: String
    ) = MultiPartFormDataContent(formData {
        append("qid", groupId)
        append("bkn", client.wLoginSigInfo.bkn)
        append("fid", fid)
        append("format", "json")
    })

    fun Announcement.toGroupAnnouncement(senderId: Long): GroupAnnouncement {
        return GroupAnnouncement(
            sender = senderId,
            msg = GroupAnnouncementMsg(text = content), // 实际测试中发布一个新公告的时候不需要进行 html 转码
            type = if (parameters.sendToNewMember) 20 else 6,
            settings = GroupAnnouncementSettings(
                isShowEditCard = if (parameters.showEditCard) 1 else 0,
                tipWindowType = if (parameters.showPopup) 0 else 1,
                confirmRequired = if (parameters.requireConfirmation) 1 else 0,
            ),
            pinned = if (parameters.isPinned) 1 else 0,
        )
    }

    fun GroupAnnouncement.toAnnouncement(group: Group): OnlineAnnouncementImpl {
        val fid = this.fid ?: ""

        return OnlineAnnouncementImpl(
            group = group,
            senderId = sender,
            sender = group[sender],
            content = msg.text.decodeHtmlEscape(),
            parameters = buildAnnouncementParameters {
                isPinned = this@toAnnouncement.pinned == 1
                sendToNewMember = type == 20
                showPopup = settings.tipWindowType == 0
                requireConfirmation = settings.confirmRequired == 1
                showEditCard = settings.isShowEditCard == 1
            },
            fid = fid,
            allConfirmed = isAllConfirm != 0,
            confirmedMembersCount = readNum,
            publicationTime = time
        )
    }
}
