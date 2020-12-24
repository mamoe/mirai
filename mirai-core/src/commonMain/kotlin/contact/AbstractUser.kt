/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.contact

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.BeforeImageUploadEvent
import net.mamoe.mirai.event.events.EventCancelledException
import net.mamoe.mirai.event.events.ImageUploadEvent
import net.mamoe.mirai.internal.network.highway.postImage
import net.mamoe.mirai.internal.network.highway.sizeToString
import net.mamoe.mirai.internal.network.protocol.data.proto.Cmd0x352
import net.mamoe.mirai.internal.network.protocol.packet.chat.image.LongConn
import net.mamoe.mirai.internal.utils.MiraiPlatformUtils
import net.mamoe.mirai.internal.utils.toUHexString
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.utils.ExternalImage
import net.mamoe.mirai.utils.verbose
import kotlin.coroutines.CoroutineContext
import kotlin.math.roundToInt
import kotlin.time.measureTime

internal abstract class AbstractUser(
    bot: Bot,
    coroutineContext: CoroutineContext,
    friendInfo: net.mamoe.mirai.data.FriendInfo,
) : User, AbstractContact(bot, coroutineContext) {
    final override val id: Long = friendInfo.uin
    final override val nick: String = friendInfo.nick
    final override val remark: String = friendInfo.remark

    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    override suspend fun uploadImage(image: ExternalImage): Image = try {
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

        when (response) {
            is LongConn.OffPicUp.Response.FileExists -> net.mamoe.mirai.internal.message.OfflineFriendImage(response.resourceId)
                .also {
                    ImageUploadEvent.Succeed(this, image, it).broadcast()
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

                net.mamoe.mirai.internal.message.OfflineFriendImage(response.resourceId).also {
                    ImageUploadEvent.Succeed(this, image, it).broadcast()
                }
            }
            is LongConn.OffPicUp.Response.Failed -> {
                ImageUploadEvent.Failed(this, image, -1, response.message).broadcast()
                error(response.message)
            }
        }
    } finally {
        image.input.release()
    }
}