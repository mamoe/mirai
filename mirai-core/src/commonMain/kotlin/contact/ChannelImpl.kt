/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.contact

import net.mamoe.mirai.contact.Channel
import net.mamoe.mirai.contact.file.RemoteFiles
import net.mamoe.mirai.data.ChannelInfo
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.message.image.OfflineGuildImage
import net.mamoe.mirai.internal.message.image.calculateImageInfo
import net.mamoe.mirai.internal.message.image.getIdByImageType
import net.mamoe.mirai.internal.message.image.getImageTypeById
import net.mamoe.mirai.internal.message.protocol.outgoing.ChannelMessageProtocolStrategy
import net.mamoe.mirai.internal.message.protocol.outgoing.MessageProtocolStrategy
import net.mamoe.mirai.internal.network.components.BdhSession
import net.mamoe.mirai.internal.network.highway.Highway
import net.mamoe.mirai.internal.network.highway.ResourceKind
import net.mamoe.mirai.internal.network.protocol.data.proto.Cmd0x388
import net.mamoe.mirai.internal.network.protocol.packet.chat.image.ImgStore
import net.mamoe.mirai.internal.utils.ImagePatcher
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.ImageType
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.OfflineAudio
import net.mamoe.mirai.utils.*
import kotlin.coroutines.CoroutineContext


internal expect class ChannelImpl constructor(
    bot: QQAndroidBot,
    parentCoroutineContext: CoroutineContext,
    id: Long,
    guildId: Long,
    channelInfo: ChannelInfo,
) : Channel, CommonChannelImpl {
    companion object
}


@Suppress("PropertyName")
internal abstract class CommonChannelImpl constructor(
    bot: QQAndroidBot,
    parentCoroutineContext: CoroutineContext,
    override val id: Long,
    channelInfo: ChannelInfo,
) : Channel, AbstractContact(bot, parentCoroutineContext) {

    override val tinyId: Long
        get() = bot.tinyId

    private val messageProtocolStrategy: MessageProtocolStrategy<ChannelImpl> =
        ChannelMessageProtocolStrategy(this.cast())

    override suspend fun uploadAudio(resource: ExternalResource): OfflineAudio {
        throw EventCancelledException("The channel does not support sending messages")
    }

    override val name: String = channelInfo.name

    override suspend fun sendMessage(message: Message): MessageReceipt<Channel> {
        return sendMessageImpl(
            message,
            messageProtocolStrategy.cast(),
            ::ChannelMessagePreSendEvent,
            ::ChannelMessagePostSendEvent.cast()
        )
    }

    override suspend fun uploadImage(resource: ExternalResource): Image = resource.withAutoClose {
        if (BeforeImageUploadEvent(this, resource).broadcast().isCancelled) {
            throw EventCancelledException("cancelled by BeforeImageUploadEvent.ToChannel")
        }

        fun OfflineGuildImage.putIntoCache() {
            // We can't understand wny Image(channel.uploadImage().imageId)
            bot.components[ImagePatcher].putCache(this)
        }

        val imageInfo = runBIO { resource.calculateImageInfo() }
        val response: ImgStore.QQMeetPicUp.Response = bot.network.sendAndExpect(
            ImgStore.QQMeetPicUp(
                bot.client,
                uin = bot.id,
                guildId = guildId,
                channelId = id,
                md5 = resource.md5,
                size = resource.size,
                filename = "${resource.md5.toUHexString("")}.${resource.formatName}",
                picWidth = imageInfo.width,
                picHeight = imageInfo.height,
                picType = getIdByImageType(imageInfo.imageType),
            ), 5000, 2
        )

        when (response) {
            is ImgStore.QQMeetPicUp.Response.Failed -> {
                ImageUploadEvent.Failed(this@CommonChannelImpl, resource, response.resultCode, response.message)
                    .broadcast()
                if (response.message == "over file size max") throw OverFileSizeMaxException()
                error("upload guild image failed with reason ${response.message}")
            }

            is ImgStore.QQMeetPicUp.Response.FileExists -> {
                val resourceId = resource.calculateResourceId()
                return response.fileInfo.run {
                    OfflineGuildImage(
                        serverPort = response.serverPort,
                        serverIp = response.serverIp,
                        imageId = resourceId,
                        height = fileHeight,
                        width = fileWidth,
                        imageType = getImageTypeById(fileType) ?: ImageType.UNKNOWN,
                        size = resource.size,
                        downloadIndex = response.downloadIndex
                    )
                }
                    .also {
                        it.fileId = response.fileId.toInt()
                    }
                    .also { it.putIntoCache() }
                    .also { ImageUploadEvent.Succeed(this@CommonChannelImpl, resource, it).broadcast() }
            }

            is ImgStore.QQMeetPicUp.Response.RequireUpload -> {
                Highway.uploadResourceBdh(
                    bot = bot,
                    resource = resource,
                    kind = ResourceKind.GUILD_IMAGE,
                    commandId = 83,
                    initialTicket = response.uKey,
                    noBdhAwait = true,
                    fallbackSession = {
                        BdhSession(
                            EMPTY_BYTE_ARRAY, EMPTY_BYTE_ARRAY,
                            ssoAddresses = response.uploadIpList.zip(response.uploadPortList).toMutableSet(),
                        )
                    },
                    extendInfo = Cmd0x388.uploadGuildChannel(
                        guildId = guildId,
                        channelId = id,
                    ).toByteArray(Cmd0x388.uploadGuildChannel.serializer())
                )

                return imageInfo.run {
                    OfflineGuildImage(
                        serverPort = response.uploadPortList.firstOrNull() ?: 0,
                        serverIp = response.uploadIpList.firstOrNull() ?: 0,
                        imageId = resource.calculateResourceId(),
                        width = width,
                        height = height,
                        imageType = imageType,
                        size = resource.size,
                        downloadIndex = response.downloadIndex
                    )
                }.also { it.fileId = response.fileId.toInt() }
                    .also { it.putIntoCache() }
                    .also { ImageUploadEvent.Succeed(this@CommonChannelImpl, resource, it).broadcast() }
            }
        }
    }

    override val files: RemoteFiles
        get() = TODO("Not yet implemented")

    override fun toString(): String = "Guild($guildId) Channel($id)"
}