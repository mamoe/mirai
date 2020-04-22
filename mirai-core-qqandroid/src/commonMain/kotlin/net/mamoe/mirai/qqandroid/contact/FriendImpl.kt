/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:OptIn(MiraiInternalAPI::class, LowLevelAPI::class)
@file:Suppress("EXPERIMENTAL_API_USAGE", "DEPRECATION_ERROR", "NOTHING_TO_INLINE")

package net.mamoe.mirai.qqandroid.contact

import kotlinx.atomicfu.AtomicInt
import kotlinx.atomicfu.atomic
import kotlinx.io.core.Closeable
import net.mamoe.mirai.LowLevelAPI
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.data.FriendInfo
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.BeforeImageUploadEvent
import net.mamoe.mirai.event.events.EventCancelledException
import net.mamoe.mirai.event.events.ImageUploadEvent
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.OfflineFriendImage
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.network.highway.postImage
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.Cmd0x352
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.image.LongConn
import net.mamoe.mirai.qqandroid.utils.MiraiPlatformUtils
import net.mamoe.mirai.qqandroid.utils.toUHexString
import net.mamoe.mirai.utils.ExternalImage
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.getValue
import net.mamoe.mirai.utils.unsafeWeakRef
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext
import kotlin.jvm.JvmSynthetic

internal inline class FriendInfoImpl(
    private val jceFriendInfo: net.mamoe.mirai.qqandroid.network.protocol.data.jce.FriendInfo
) : FriendInfo {
    override val nick: String get() = jceFriendInfo.nick
    override val uin: Long get() = jceFriendInfo.friendUin
}

@OptIn(ExperimentalContracts::class)
internal inline fun Friend.checkIsFriendImpl(): FriendImpl {
    contract {
        returns() implies (this@checkIsFriendImpl is FriendImpl)
    }
    check(this is FriendImpl) { "A Friend instance is not instance of FriendImpl. Don't interlace two protocol implementations together!" }
    return this
}

internal class FriendImpl(
    bot: QQAndroidBot,
    override val coroutineContext: CoroutineContext,
    override val id: Long,
    private val friendInfo: FriendInfo
) : Friend() {
    @Suppress("unused") // bug
    val lastMessageSequence: AtomicInt = atomic(-1)

    override val bot: QQAndroidBot by bot.unsafeWeakRef()
    override val nick: String
        get() = friendInfo.nick

    @JvmSynthetic
    @Suppress("DuplicatedCode")
    override suspend fun sendMessage(message: Message): MessageReceipt<Friend> {
        return sendMessageImpl(message).also {
            logMessageSent(message)
        }
    }

    @JvmSynthetic
    @OptIn(MiraiInternalAPI::class)
    override suspend fun uploadImage(image: ExternalImage): OfflineFriendImage = try {
        if (BeforeImageUploadEvent(this, image).broadcast().isCancelled) {
            throw EventCancelledException("cancelled by BeforeImageUploadEvent.ToGroup")
        }
        bot.network.run {
            val response = LongConn.OffPicUp(
                bot.client, Cmd0x352.TryUpImgReq(
                    srcUin = bot.id.toInt(),
                    dstUin = id.toInt(),
                    fileId = 0,
                    fileMd5 = image.md5,
                    fileSize = image.inputSize.toInt(),
                    fileName = image.md5.toUHexString("") + "." + image.format,
                    imgOriginal = 1,
                    imgWidth = image.width,
                    imgHeight = image.height,
                    imgType = image.imageType
                )
            ).sendAndExpect<LongConn.OffPicUp.Response>()

            @Suppress("UNCHECKED_CAST") // bug
            return when (response) {
                is LongConn.OffPicUp.Response.FileExists -> OfflineFriendImage(
                    filepath = response.resourceId,
                    md5 = response.imageInfo.fileMd5,
                    fileLength = response.imageInfo.fileSize.toInt(),
                    height = response.imageInfo.fileHeight,
                    width = response.imageInfo.fileWidth,
                    resourceId = response.resourceId
                ).also {
                    ImageUploadEvent.Succeed(this@FriendImpl, image, it).broadcast()
                }
                is LongConn.OffPicUp.Response.RequireUpload -> {
                    MiraiPlatformUtils.Http.postImage(
                        "0x6ff0070",
                        bot.id,
                        null,
                        imageInput = image.input,
                        inputSize = image.inputSize,
                        uKeyHex = response.uKey.toUHexString("")
                    )
                    //HighwayHelper.uploadImage(
                    //    client = bot.client,
                    //    serverIp = response.serverIp[0].toIpV4AddressString(),
                    //    serverPort = response.serverPort[0],
                    //    imageInput = image.input,
                    //    inputSize = image.inputSize.toInt(),
                    //    fileMd5 = image.md5,
                    //    uKey = response.uKey,
                    //    commandId = 1
                    //)
                    // 为什么不能 ??

                    return OfflineFriendImage(
                        filepath = response.resourceId,
                        md5 = image.md5,
                        fileLength = image.inputSize.toInt(),
                        height = image.height,
                        width = image.width,
                        resourceId = response.resourceId
                    ).also {
                        ImageUploadEvent.Succeed(this@FriendImpl, image, it).broadcast()
                    }
                }
                is LongConn.OffPicUp.Response.Failed -> {
                    ImageUploadEvent.Failed(this@FriendImpl, image, -1, response.message).broadcast()
                    error(response.message)
                }
            }
        }
    } finally {
        (image.input as? Closeable)?.close()
    }
}