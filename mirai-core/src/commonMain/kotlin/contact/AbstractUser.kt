/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.contact

import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.Stranger
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.data.UserInfo
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.message.contextualBugReportException
import net.mamoe.mirai.internal.message.flags.SkipEventBroadcast
import net.mamoe.mirai.internal.message.image.*
import net.mamoe.mirai.internal.message.protocol.MessageProtocolFacade
import net.mamoe.mirai.internal.message.protocol.outgoing.HighwayUploader
import net.mamoe.mirai.internal.message.protocol.outgoing.MessageProtocolStrategy
import net.mamoe.mirai.internal.network.component.buildComponentStorage
import net.mamoe.mirai.internal.network.components.BdhSession
import net.mamoe.mirai.internal.network.components.ClockHolder
import net.mamoe.mirai.internal.network.components.HttpClientProvider
import net.mamoe.mirai.internal.network.highway.ChannelKind
import net.mamoe.mirai.internal.network.highway.Highway
import net.mamoe.mirai.internal.network.highway.ResourceKind.PRIVATE_IMAGE
import net.mamoe.mirai.internal.network.highway.postImage
import net.mamoe.mirai.internal.network.highway.tryServersUpload
import net.mamoe.mirai.internal.network.protocol.data.proto.Cmd0x352
import net.mamoe.mirai.internal.network.protocol.packet.chat.image.ImgStore
import net.mamoe.mirai.internal.network.protocol.packet.chat.image.LongConn
import net.mamoe.mirai.internal.utils.AtomicIntSeq
import net.mamoe.mirai.internal.utils.C2CPkgMsgParsingCache
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.*
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext

internal val User.info: UserInfo? get() = this.castOrNull<AbstractUser>()?.info

@Suppress("NOTHING_TO_INLINE")
internal inline fun User.impl(): AbstractUser {
    contract { returns() implies (this@impl is AbstractUser) }
    check(this is AbstractUser)
    return this
}

internal val User.correspondingMessageSourceKind
    get() = when (this) {
        is Friend -> MessageSourceKind.FRIEND
        is Member -> MessageSourceKind.TEMP
        is Stranger -> MessageSourceKind.STRANGER
        else -> error("Unknown user: ${this::class.qualifiedName}")
    }

internal sealed class AbstractUser(
    bot: QQAndroidBot,
    parentCoroutineContext: CoroutineContext,
    userInfo: UserInfo,
) : User, AbstractContact(bot, parentCoroutineContext) {

    final override val id: Long = userInfo.uin
    abstract override val nick: String
    abstract override val remark: String

    val messageSeq = AtomicIntSeq.forMessageSeq()
    val fragmentedMessageMerger = C2CPkgMsgParsingCache()

    open val info: UserInfo = userInfo

    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    override suspend fun uploadImage(resource: ExternalResource): Image = resource.withAutoClose {
        if (BeforeImageUploadEvent(this, resource).broadcast().isCancelled) {
            throw EventCancelledException("cancelled by BeforeImageUploadEvent.ToGroup")
        }
        val imageInfo = runBIO { resource.calculateImageInfo() }
        val resp = bot.network.sendAndExpect(
            LongConn.OffPicUp(
                bot.client,
                Cmd0x352.TryUpImgReq(
                    buType = 1,
                    srcUin = bot.id,
                    dstUin = this@AbstractUser.id,
                    fileMd5 = resource.md5,
                    fileSize = resource.size,
                    imgWidth = imageInfo.width,
                    imgHeight = imageInfo.height,
                    imgType = getIdByImageType(imageInfo.imageType),
                    fileName = "${resource.md5.toUHexString("")}.${resource.formatName}",
                    //For gif, using not original
                    imgOriginal = (imageInfo.imageType != ImageType.GIF),
                    buildVer = bot.client.buildVer,
                ),
            ), 5000, 2
        )

        return when (resp) {
            is LongConn.OffPicUp.Response.FileExists -> {
                val imageType =
                    getImageType(resp.imageInfo.fileType).takeIf { it != ExternalResource.DEFAULT_FORMAT_NAME }
                        ?: resource.formatName

                resp.imageInfo.run {
                    OfflineFriendImage(
                        imageId = generateImageIdFromResourceId(
                            resourceId = resp.resourceId, format = imageType
                        ) ?: kotlin.run {
                            if (resp.imageInfo.fileMd5.size == 16) {
                                generateImageId(resp.imageInfo.fileMd5, imageType)
                            } else {
                                throw contextualBugReportException(
                                    "Failed to compute friend image image from resourceId: ${resp.resourceId}",
                                    resp.structureToString(),
                                    additional = "并附加此时正在上传的文件"
                                )
                            }
                        },
                        width = fileWidth,
                        height = fileHeight,
                        imageType = getImageTypeById(fileType) ?: ImageType.UNKNOWN,
                        size = resource.size
                    )
                }.also {
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
                    val response: ImgStore.GroupPicUp.Response = bot.network.sendAndExpect(
                        ImgStore.GroupPicUp(
                            bot.client,
                            uin = bot.id,
                            groupCode = id,
                            md5 = resource.md5,
                            size = resource.size,
                            picWidth = imageInfo.width,
                            picHeight = imageInfo.height,
                            picType = getIdByImageType(imageInfo.imageType),
                            buType = 2, // not group
                        )
                    )

                    when (response) {
                        is ImgStore.GroupPicUp.Response.Failed -> {
                            error("upload private image as group image failed with reason ${response.message}")
                        }
                        is ImgStore.GroupPicUp.Response.FileExists -> {
                            // success
                        }
                        is ImgStore.GroupPicUp.Response.RequireUpload -> {
                            // val servers = response.uploadIpList.zip(response.uploadPortList)
                            Highway.uploadResourceBdh(bot = bot,
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
                                })
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
                        bot.components[HttpClientProvider].getHttpClient().postImage(
                            serverIp = ip,
                            serverPort = port,
                            htcmd = "0x6ff0070",
                            uin = bot.id,
                            groupcode = null,
                            imageInput = resource,
                            uKeyHex = resp.uKey.toUHexString("")
                        )
                    }
                }.recoverCatchingSuppressed {
                    // try upload by http on fallback server
                    bot.components[HttpClientProvider].getHttpClient().postImage(
                        serverIp = "htdata2.qq.com",
                        htcmd = "0x6ff0070",
                        uin = bot.id,
                        groupcode = null,
                        imageInput = resource,
                        uKeyHex = resp.uKey.toUHexString("")
                    )
                }.getOrThrow()

                imageInfo.run {
                    OfflineFriendImage(
                        imageId = generateImageIdFromResourceId(resp.resourceId, resource.formatName)
                            ?: resp.resourceId,
                        width = width,
                        height = height,
                        imageType = imageType,
                        size = resource.size
                    )
                }.also {
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


internal suspend fun <C : AbstractContact> C.sendMessageImpl(
    message: Message,
    messageProtocolStrategy: MessageProtocolStrategy<C>,
    preSendEventConstructor: (C, Message) -> MessagePreSendEvent,
    postSendEventConstructor: (C, MessageChain, Throwable?, MessageReceipt<C>?) -> MessagePostSendEvent<C>,
): MessageReceipt<C> {
    val skipEvent = if (message is MessageChain) {
        message.anyIsInstance<SkipEventBroadcast>()
    } else false

    require(!message.isContentEmpty()) { "message is empty" }

    val chain = broadcastMessagePreSendEvent(message, skipEvent, preSendEventConstructor)

    val result = kotlin.runCatching {
        MessageProtocolFacade.preprocessAndSendOutgoing(this, chain, buildComponentStorage {
            set(MessageProtocolStrategy, messageProtocolStrategy)
            set(HighwayUploader, HighwayUploader.Default)
            set(ClockHolder, bot.components[ClockHolder])
        })
    }

    if (result.isSuccess) {
        // logMessageSent(result.getOrNull()?.source?.plus(chain) ?: chain) // log with source
        bot.logger.verbose("$this <- $chain".replaceMagicCodes())
    }

    if (!skipEvent) {
        postSendEventConstructor(this, chain, result.exceptionOrNull(), result.getOrNull()).broadcast()
    }

    return result.getOrThrow()
}