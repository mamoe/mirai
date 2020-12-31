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
import net.mamoe.mirai.internal.network.highway.postImage
import net.mamoe.mirai.internal.network.highway.sizeToString
import net.mamoe.mirai.internal.network.protocol.data.proto.Cmd0x352
import net.mamoe.mirai.internal.network.protocol.packet.chat.image.LongConn
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.MiraiPlatformUtils
import net.mamoe.mirai.utils.toUHexString
import net.mamoe.mirai.utils.verbose
import kotlin.coroutines.CoroutineContext
import kotlin.math.roundToInt
import kotlin.time.measureTime

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

    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    override suspend fun uploadImage(resource: ExternalResource): Image {
        if (BeforeImageUploadEvent(this, resource).broadcast().isCancelled) {
            throw EventCancelledException("cancelled by BeforeImageUploadEvent.ToGroup")
        }
        val response = bot.network.run {
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
        return when (response) {
            is LongConn.OffPicUp.Response.FileExists -> OfflineFriendImage(response.resourceId)
                .also {
                    ImageUploadEvent.Succeed(this, resource, it).broadcast()
                }
            is LongConn.OffPicUp.Response.RequireUpload -> {
                bot.network.logger.verbose {
                    "[Http] Uploading $kind image, size=${resource.size.sizeToString()}"
                }

                val time = measureTime {
                    MiraiPlatformUtils.Http.postImage(
                        "0x6ff0070",
                        bot.id,
                        null,
                        imageInput = resource,
                        uKeyHex = response.uKey.toUHexString("")
                    )
                }

                bot.network.logger.verbose {
                    "[Http] Uploading $kind image: succeed at ${(resource.size.toDouble() / 1024 / time.inSeconds).roundToInt()} KiB/s"
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
                    ImageUploadEvent.Succeed(this, resource, it).broadcast()
                }
            }
            is LongConn.OffPicUp.Response.Failed -> {
                ImageUploadEvent.Failed(this, resource, -1, response.message).broadcast()
                error(response.message)
            }
        }
    }
}