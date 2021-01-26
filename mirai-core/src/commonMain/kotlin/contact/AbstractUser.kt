/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.contact

import net.mamoe.mirai.Bot
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.Stranger
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.data.UserInfo
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.BeforeImageUploadEvent
import net.mamoe.mirai.event.events.EventCancelledException
import net.mamoe.mirai.event.events.ImageUploadEvent
import net.mamoe.mirai.internal.message.OfflineFriendImage
import net.mamoe.mirai.internal.message.getImageType
import net.mamoe.mirai.internal.network.highway.ChannelKind
import net.mamoe.mirai.internal.network.highway.Highway
import net.mamoe.mirai.internal.network.highway.ResourceKind.PRIVATE_IMAGE
import net.mamoe.mirai.internal.network.highway.postImage
import net.mamoe.mirai.internal.network.highway.tryServers
import net.mamoe.mirai.internal.network.protocol.data.proto.Cmd0x352
import net.mamoe.mirai.internal.network.protocol.packet.chat.image.LongConn
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.utils.*
import kotlin.coroutines.CoroutineContext

internal val User.info: UserInfo? get() = this.castOrNull<AbstractUser>()?.info

internal open class UserInfoImpl(override val uin: Long, override val nick: String, override val remark: String = "") :
    UserInfo

internal abstract class AbstractUser(
    bot: Bot,
    coroutineContext: CoroutineContext,
    userInfo: UserInfo,
) : User, AbstractContact(bot, coroutineContext) {
    final override val id: Long = userInfo.uin
    final override var nick: String = userInfo.nick
    final override val remark: String = userInfo.remark

    open val info: UserInfo = userInfo

    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    override suspend fun uploadImage(resource: ExternalResource): Image {
        if (BeforeImageUploadEvent(this, resource).broadcast().isCancelled) {
            throw EventCancelledException("cancelled by BeforeImageUploadEvent.ToGroup")
        }
        val resp = bot.network.run {
            LongConn.OffPicUp(
                bot.client, Cmd0x352.TryUpImgReq(
                    srcUin = bot.id.toInt(),
                    dstUin = id.toInt(),
                    fileId = 0,
                    fileMd5 = resource.md5,
                    fileSize = resource.size.toInt(),
                    fileName = resource.md5.toUHexString("") + "." + resource.formatName,
                    imgOriginal = 1
                )
            ).sendAndExpect<LongConn.OffPicUp.Response>()
        }

        val kind = when (this) {
            is Stranger -> "stranger"
            is Friend -> "friend"
            is Member -> "temp"
            else -> "unknown"
        }
        return when (resp) {
            is LongConn.OffPicUp.Response.FileExists -> OfflineFriendImage(
                imageId = generateImageIdFromResourceId(
                    resourceId = resp.resourceId,
                    format = getImageType(resp.imageInfo.fileType).takeIf { it != ExternalResource.DEFAULT_FORMAT_NAME }
                        ?: resource.formatName
                ) ?: resp.resourceId
            ).also {
                ImageUploadEvent.Succeed(this, resource, it).broadcast()
            }

            is LongConn.OffPicUp.Response.RequireUpload -> {

                kotlin.runCatching {
                    Highway.uploadResourceBdh(
                        bot = bot,
                        resource = resource,
                        kind = PRIVATE_IMAGE,
                        commandId = 1,
                        initialTicket = resp.uKey
                    )
                }.recoverCatchingSuppressed {
                    tryServers(
                        bot = bot,
                        servers = resp.serverIp.zip(resp.serverPort),
                        resourceSize = resource.size,
                        resourceKind = PRIVATE_IMAGE,
                        channelKind = ChannelKind.HTTP
                    ) { ip, port ->
                        Mirai.Http.postImage(
                            serverIp = ip, serverPort = port,
                            htcmd = "0x6ff0070",
                            uin = bot.id,
                            groupcode = null,
                            imageInput = resource,
                            uKeyHex = resp.uKey.toUHexString("")
                        )
                    }
                }.recoverCatchingSuppressed {
                    Mirai.Http.postImage(
                        serverIp = "htdata2.qq.com",
                        htcmd = "0x6ff0070",
                        uin = bot.id,
                        groupcode = null,
                        imageInput = resource,
                        uKeyHex = resp.uKey.toUHexString("")
                    )
                }.getOrThrow()

                OfflineFriendImage(
                    generateImageIdFromResourceId(resp.resourceId, resource.formatName) ?: resp.resourceId
                ).also {
                    ImageUploadEvent.Succeed(this, resource, it).broadcast()
                }
            }
            is LongConn.OffPicUp.Response.Failed -> {
                ImageUploadEvent.Failed(this, resource, -1, resp.message).broadcast()
                error(resp.message)
            }
        }
    }
}