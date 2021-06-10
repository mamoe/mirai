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
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.data.UserInfo
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.internal.message.OfflineFriendImage
import net.mamoe.mirai.internal.message.contextualBugReportException
import net.mamoe.mirai.internal.message.getImageType
import net.mamoe.mirai.internal.network.context.BdhSession
import net.mamoe.mirai.internal.network.highway.ChannelKind
import net.mamoe.mirai.internal.network.highway.Highway
import net.mamoe.mirai.internal.network.highway.ResourceKind.PRIVATE_IMAGE
import net.mamoe.mirai.internal.network.highway.postImage
import net.mamoe.mirai.internal.network.highway.tryServersUpload
import net.mamoe.mirai.internal.network.protocol.data.proto.Cmd0x352
import net.mamoe.mirai.internal.network.protocol.packet.chat.image.ImgStore
import net.mamoe.mirai.internal.network.protocol.packet.chat.image.LongConn
import net.mamoe.mirai.internal.network.protocol.packet.sendAndExpect
import net.mamoe.mirai.internal.utils._miraiContentToString
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.isContentEmpty
import net.mamoe.mirai.utils.*
import kotlin.coroutines.CoroutineContext

internal val User.info: UserInfo? get() = this.castOrNull<AbstractUser>()?.info

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
                    buType = 1,
                    srcUin = bot.id,
                    dstUin = this@AbstractUser.id,
                    fileMd5 = resource.md5,
                    fileSize = resource.size,
                    fileName = resource.md5.toUHexString("") + "." + resource.formatName,
                    imgOriginal = 1,
                )
            ).sendAndExpect<LongConn.OffPicUp.Response>()
        }

        return when (resp) {
            is LongConn.OffPicUp.Response.FileExists -> {
                val imageType = getImageType(resp.imageInfo.fileType)
                    .takeIf { it != ExternalResource.DEFAULT_FORMAT_NAME }
                    ?: resource.formatName

                OfflineFriendImage(
                    imageId = generateImageIdFromResourceId(
                        resourceId = resp.resourceId,
                        format = imageType
                    ) ?: kotlin.run {
                        if (resp.imageInfo.fileMd5.size == 16) {
                            generateImageId(resp.imageInfo.fileMd5, imageType)
                        } else {
                            throw contextualBugReportException(
                                "Failed to compute friend image image from resourceId: ${resp.resourceId}",
                                resp._miraiContentToString(),
                                additional = "并附加此时正在上传的文件"
                            )
                        }
                    }
                ).also {
                    ImageUploadEvent.Succeed(this, resource, it).broadcast()
                }
            }

            is LongConn.OffPicUp.Response.RequireUpload -> {

                kotlin.runCatching {
                    // try once upload by private bdh
                    Highway.uploadResourceBdh(
                        bot = bot,
                        resource = resource,
                        kind = PRIVATE_IMAGE,
                        commandId = 1,
                        initialTicket = resp.uKey,
                        tryOnce = true
                    )
                }.recoverCatchingSuppressed {
                    // try upload as group image
                    val response: ImgStore.GroupPicUp.Response = ImgStore.GroupPicUp(
                        bot.client,
                        uin = bot.id,
                        groupCode = id,
                        md5 = resource.md5,
                        size = resource.size
                    ).sendAndExpect(bot)

                    when (response) {
                        is ImgStore.GroupPicUp.Response.Failed -> {
                            error("upload private image as group image failed with reason ${response.message}")
                        }
                        is ImgStore.GroupPicUp.Response.FileExists -> {
                            // success
                        }
                        is ImgStore.GroupPicUp.Response.RequireUpload -> {
                            // val servers = response.uploadIpList.zip(response.uploadPortList)
                            Highway.uploadResourceBdh(
                                bot = bot,
                                resource = resource,
                                kind = PRIVATE_IMAGE,
                                commandId = 2,
                                initialTicket = response.uKey,
                                fallbackSession = {
                                    BdhSession(
                                        EMPTY_BYTE_ARRAY, EMPTY_BYTE_ARRAY,
                                        ssoAddresses = response.uploadIpList.zip(response.uploadPortList)
                                            .toMutableSet(),
                                    )
                                }
                            )
                        }
                    }
                }.recoverCatchingSuppressed {
                    // try upload by private bdh on other servers
                    Highway.uploadResourceBdh(
                        bot = bot,
                        resource = resource,
                        kind = PRIVATE_IMAGE,
                        commandId = 1,
                        initialTicket = resp.uKey,
                    )
                }.recoverCatchingSuppressed {
                    // try upload by http on provided servers
                    tryServersUpload(
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
                    // try upload by http on fallback server
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

@Suppress("DuplicatedCode")
internal suspend fun <C : User> SendMessageHandler<out C>.sendMessageImpl(
    message: Message,
    preSendEventConstructor: (C, Message) -> MessagePreSendEvent,
    postSendEventConstructor: (C, MessageChain, Throwable?, MessageReceipt<C>?) -> MessagePostSendEvent<C>,
): MessageReceipt<C> {
    require(!message.isContentEmpty()) { "message is empty" }

    val chain = contact.broadcastMessagePreSendEvent(message, preSendEventConstructor)

    val result = this
        .runCatching { sendMessage(message, chain, SendMessageStep.FIRST) }

    // logMessageSent(result.getOrNull()?.source?.plus(chain) ?: chain) // log with source
    contact.logMessageSent(chain)

    postSendEventConstructor(contact, chain, result.exceptionOrNull(), result.getOrNull()).broadcast()

    return result.getOrThrow()
}