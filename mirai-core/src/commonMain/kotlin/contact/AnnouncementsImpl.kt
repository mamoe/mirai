/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("DEPRECATION")

package net.mamoe.mirai.internal.contact

import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.mamoe.mirai.Bot
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.announcement.*
import net.mamoe.mirai.contact.checkBotPermission
import net.mamoe.mirai.data.GroupAnnouncement
import net.mamoe.mirai.data.GroupAnnouncementList
import net.mamoe.mirai.data.GroupAnnouncementMsg
import net.mamoe.mirai.data.GroupAnnouncementSettings
import net.mamoe.mirai.internal.asQQAndroidBot
import net.mamoe.mirai.utils.*
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import java.util.stream.Stream
import kotlin.io.use

internal class AnnouncementsImpl(
    private val group: GroupImpl,
) : Announcements {
    inline val bot get() = group.bot

    override suspend fun asFlow(): Flow<OnlineAnnouncement> {
        return flow {
            var i = 1
            while (true) {
                val result = Mirai.getRawGroupAnnouncements(bot, group.id, i++)
                checkResult(result, i)

                if (result.inst.isNullOrEmpty() && result.feeds.isNullOrEmpty()) break

                result.inst?.let { emitAll(it.asFlow()) }
                result.feeds?.let { emitAll(it.asFlow()) }
            }
        }.map { it.toAnnouncement(group) }
    }

    override suspend fun asStream(): Stream<OnlineAnnouncement> {
        return stream {
            var i = 1
            while (true) {
                val result = runBlocking { Mirai.getRawGroupAnnouncements(bot, group.id, i++) }
                checkResult(result, i)

                if (result.inst.isNullOrEmpty() && result.feeds.isNullOrEmpty()) break

                result.inst?.let { yieldAll(it) }
                result.feeds?.let { yieldAll(it) }
            }
        }.map { it.toAnnouncement(group) }
    }

    private fun checkResult(result: GroupAnnouncementList, i: Int) {
        if (result.ec != 0) {
            bot.logger.warning { "Failed to get announcements for group ${group.id}, at page $i. result=$result" }
        }
    }

    override suspend fun delete(fid: String): Boolean {
        group.checkBotPermission(MemberPermission.ADMINISTRATOR) { "Only administrator have permission to delete group announcement" }
        return Mirai.deleteGroupAnnouncement(bot, group.id, fid)
    }

    override suspend fun get(fid: String): OnlineAnnouncement? {
        return Mirai.getGroupAnnouncement(bot, group.id, fid)?.toAnnouncement(group)
    }

    override suspend fun publish(announcement: Announcement): OnlineAnnouncement = announcement.run {
        val bot = group.bot
        group.checkBotPermission(MemberPermission.ADMINISTRATOR) { "Only administrator have permission to send group announcement" }
        val image = parameters.image
        val fid = image?.toExternalResource()?.use {
            val imageUp = AnnouncementProtocol.uploadGroupAnnouncementImage(bot, group.id, it)
            AnnouncementProtocol.sendGroupAnnouncementWithImage(bot, group.id, imageUp, toGroupAnnouncement(bot.id))
        } ?: Mirai.sendGroupAnnouncement(bot, group.id, toGroupAnnouncement(bot.id))

        return OnlineAnnouncementImpl(
            group = group,
            senderId = bot.id,
            sender = group.botAsMember,
            title = title,
            body = body,
            parameters = parameters,
            fid = fid,
            isAllRead = false,
            readMemberNumber = 0,
            publishTime = currentTimeSeconds()
        )
    }

    override suspend fun uploadImage(resource: ExternalResource): AnnouncementImage {
        return AnnouncementProtocol.uploadGroupAnnouncementImage(bot, group.id, resource)
    }
}

@Suppress("DEPRECATION")
internal object AnnouncementProtocol {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun uploadGroupAnnouncementImage(
        bot: Bot,
        groupId: Long,
        resource: ExternalResource
    ): AnnouncementImage = bot.asQQAndroidBot().run {
        //https://youtrack.jetbrains.com/issue/KTOR-455
        val rep = Mirai.Http.post<String> {
            url("https://web.qun.qq.com/cgi-bin/announce/upload_img")
            body = MultiPartFormDataContent(formData {
                append("\"bkn\"", bkn)
                append("\"source\"", "troopNotice")
                append("m", "0")
                append(
                    "\"pic_up\"",
                    headers = Headers.build {
                        append(HttpHeaders.ContentType, ContentType.Image.PNG)
                        append(HttpHeaders.ContentDisposition, "filename=\"temp_uploadFile.png\"")
                    }
                ) {
                    writeFully(resource.inputStream().withUse { readBytes() })
                }
            })
            headers {
                append(
                    "cookie",
                    " p_uin=o${id};" +
                            " p_skey=${client.wLoginSigInfo.psKeyMap["qun.qq.com"]?.data?.encodeToString() ?: error("cookie parse p_skey error")}; "
                )
            }
        }
        val jsonObj = json.parseToJsonElement(rep)
        if (jsonObj.jsonObject["ec"]?.jsonPrimitive?.int != 0) {
            throw IllegalStateException("Upload group announcement image fail group:$groupId msg:${jsonObj.jsonObject["em"]}")
        }
        val id = jsonObj.jsonObject["id"]?.jsonPrimitive?.content
            ?: throw IllegalStateException("Upload group announcement image fail group:$groupId msg:${jsonObj.jsonObject["em"]}")
        return json.decodeFromString(AnnouncementImage.serializer(), id)
    }

    suspend fun sendGroupAnnouncementWithImage(
        bot: Bot,
        groupId: Long,
        image: AnnouncementImage,
        announcement: GroupAnnouncement
    ): String = bot.asQQAndroidBot().run {
        val rep = withContext(network.coroutineContext) {
            Mirai.Http.post<String> {
                url("https://web.qun.qq.com/cgi-bin/announce/add_qun_notice")
                body = MultiPartFormDataContent(formData {
                    append("qid", groupId)
                    append("bkn", bkn)
                    append("text", announcement.msg.text)
                    append("pinned", announcement.pinned)
                    append("pic", image.id)
                    append("imgWidth", image.width)
                    append("imgHeight", image.height)
                    append(
                        "settings",
                        json.encodeToString(
                            GroupAnnouncementSettings.serializer(),
                            announcement.settings ?: GroupAnnouncementSettings()
                        )
                    )
                    append("format", "json")
                })
                headers {
                    append(
                        "cookie",
                        " p_uin=o${id};" +
                                " p_skey=${
                                    client.wLoginSigInfo.psKeyMap["qun.qq.com"]?.data?.encodeToString() ?: error(
                                        "parse error"
                                    )
                                }; "
                    )
                }

            }
        }
        val jsonObj = json.parseToJsonElement(rep)
        return jsonObj.jsonObject["new_fid"]?.jsonPrimitive?.content
            ?: throw throw IllegalStateException("Send Announcement with image fail group:$groupId msg:${jsonObj.jsonObject["em"]} content:${announcement.msg.text}")
    }


}

@Suppress("DEPRECATION")
internal fun Announcement.toGroupAnnouncement(senderId: Long): GroupAnnouncement {
    return GroupAnnouncement(
        sender = senderId,
        msg = GroupAnnouncementMsg(
            title = title,
            text = body
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

@Suppress("DEPRECATION")
private fun GroupAnnouncement.toAnnouncement(group: Group): OnlineAnnouncementImpl {
    val fid = this.fid
    val settings = this.settings

    check(fid != null) { "GroupAnnouncement don't have id" }
    check(settings != null) { "GroupAnnouncement don't have setting" }

    return OnlineAnnouncementImpl(
        group = group,
        senderId = sender,
        sender = group[sender],
        title = msg.title ?: "",
        body = msg.text,
        parameters = buildAnnouncementParameters {
            isPinned = pinned == 1
            sendToNewMember = type == 20
            isTip = settings.tipWindowType == 0
            needConfirm = settings.confirmRequired == 1
            isShowEditCard = settings.isShowEditCard == 1
        },
        fid = fid,
        isAllRead = isAllConfirm != 0,
        readMemberNumber = readNum,
        publishTime = time
    )
}
