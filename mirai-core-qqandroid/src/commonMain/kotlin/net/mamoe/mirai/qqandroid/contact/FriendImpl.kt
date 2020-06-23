/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:OptIn(LowLevelAPI::class)
@file:Suppress(
    "EXPERIMENTAL_API_USAGE",
    "DEPRECATION_ERROR",
    "NOTHING_TO_INLINE",
    "INVISIBLE_MEMBER",
    "INVISIBLE_REFERENCE"
)

package net.mamoe.mirai.qqandroid.contact

import kotlinx.atomicfu.AtomicInt
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.io.core.Closeable
import net.mamoe.mirai.LowLevelAPI
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.data.FriendInfo
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.BeforeImageUploadEvent
import net.mamoe.mirai.event.events.EventCancelledException
import net.mamoe.mirai.event.events.ImageUploadEvent
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.OfflineFriendImage
import net.mamoe.mirai.message.data.isContentNotEmpty
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.network.highway.postImage
import net.mamoe.mirai.qqandroid.network.highway.sizeToString
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.Cmd0x352
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.image.LongConn
import net.mamoe.mirai.qqandroid.utils.MiraiPlatformUtils
import net.mamoe.mirai.qqandroid.utils.toUHexString
import net.mamoe.mirai.utils.ExternalImage
import net.mamoe.mirai.utils.getValue
import net.mamoe.mirai.utils.unsafeWeakRef
import net.mamoe.mirai.utils.verbose
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext
import kotlin.jvm.JvmSynthetic
import kotlin.math.roundToInt
import kotlin.time.measureTime

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
    coroutineContext: CoroutineContext,
    override val id: Long,
    private val friendInfo: FriendInfo
) : Friend() {
    override val coroutineContext: CoroutineContext = coroutineContext + SupervisorJob(coroutineContext[Job])

    @Suppress("unused") // bug
    val lastMessageSequence: AtomicInt = atomic(-1)

    override val bot: QQAndroidBot by bot.unsafeWeakRef()
    override val nick: String
        get() = friendInfo.nick

    @JvmSynthetic
    @Suppress("DuplicatedCode")
    override suspend fun sendMessage(message: Message): MessageReceipt<Friend> {
        require(message.isContentNotEmpty()) { "message is empty" }
        return sendMessageImpl(
            message,
            friendReceiptConstructor = { MessageReceipt(it, this, null) },
            tReceiptConstructor = { MessageReceipt(it, this, null) }
        ).also {
            logMessageSent(message)
        }
    }

    @JvmSynthetic
    override suspend fun uploadImage(image: ExternalImage): Image = try {
        @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
        if (image.input is net.mamoe.mirai.utils.internal.DeferredReusableInput) {
            image.input.init(bot.configuration.fileCacheStrategy)
        }
        if (BeforeImageUploadEvent(this, image).broadcast().isCancelled) {
            throw EventCancelledException("cancelled by BeforeImageUploadEvent.ToGroup")
        }
        val response = bot.network.run {
            LongConn.OffPicUp(
                bot.client, Cmd0x352.TryUpImgReq(
                    srcUin = bot.id.toInt(),
                    dstUin = id.toInt(),
                    fileId = 0,
                    fileMd5 = image.md5,
                    fileSize = image.input.size.toInt(),
                    fileName = image.md5.toUHexString("") + "." + image.formatName,
                    imgOriginal = 1
                )
            ).sendAndExpect<LongConn.OffPicUp.Response>()
        }

        @Suppress("UNCHECKED_CAST", "DEPRECATION")
        when (response) {
            is LongConn.OffPicUp.Response.FileExists -> OfflineFriendImage(response.resourceId)
                .also {
                    ImageUploadEvent.Succeed(this@FriendImpl, image, it).broadcast()
                }
            is LongConn.OffPicUp.Response.RequireUpload -> {
                bot.network.logger.verbose {
                    "[Http] Uploading friend image, size=${image.input.size.sizeToString()}"
                }

                val time = measureTime {
                    MiraiPlatformUtils.Http.postImage(
                        "0x6ff0070",
                        bot.id,
                        null,
                        imageInput = image.input,
                        uKeyHex = response.uKey.toUHexString("")
                    )
                }

                bot.network.logger.verbose {
                    "[Http] Uploading friend image: succeed at ${(image.input.size.toDouble() / 1024 / time.inSeconds).roundToInt()} KiB/s"
                }

                /*
                HighwayHelper.uploadImageToServers(
                    bot,
                    response.serverIp.zip(response.serverPort),
                    response.uKey,
                    image,
                    kind = "friend",
                    commandId = 1
                )*/
                // 为什么不能 ??

                OfflineFriendImage(response.resourceId).also {
                    ImageUploadEvent.Succeed(this@FriendImpl, image, it).broadcast()
                }
            }
            is LongConn.OffPicUp.Response.Failed -> {
                ImageUploadEvent.Failed(this@FriendImpl, image, -1, response.message).broadcast()
                error(response.message)
            }
        }
    } finally {
        @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
        (image.input as? Closeable)?.close()
    }
}