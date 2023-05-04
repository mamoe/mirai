/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.image

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.asQQAndroidBot
import net.mamoe.mirai.internal.network.protocol.data.proto.Cmd0x352
import net.mamoe.mirai.internal.network.protocol.packet.chat.image.ImgStore
import net.mamoe.mirai.internal.network.protocol.packet.chat.image.LongConn
import net.mamoe.mirai.internal.utils.ImagePatcher
import net.mamoe.mirai.internal.utils.withCache
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.ImageType
import net.mamoe.mirai.message.data.InternalImageProtocol
import net.mamoe.mirai.utils.cast
import net.mamoe.mirai.utils.toUHexString

internal class InternalImageProtocolImpl : InternalImageProtocol {

    /**
     * Test Notes:
     *
     * - 查图片只需要 md5 和 size
     * - 上传给群的图片可以通过 GroupPicUp(groupCode=user.id) 或 OffPicUp(dstUin=user.id) 查询
     * - 上传给好友的图片可以通过 GroupPicUp(groupCode=group.id) 或 OffPicUp(dstUin=group.id) 查询
     */
    interface ImageUploadedChecker<C : Contact?> {
        suspend fun isUploaded(
            bot: QQAndroidBot,
            context: C,
            md5: ByteArray,
            type: ImageType,
            size: Long,
            width: Int,
            height: Int
        ): Boolean

        companion object {
            val checkers = mapOf(
                Group::class to ImageUploadedCheckerGroup(),
                User::class to ImageUploadedCheckerUser(),
                null to ImageUploadedCheckerFallback()
            )
        }
    }

    class ImageUploadedCheckerGroup : ImageUploadedChecker<Group> {
        override suspend fun isUploaded(
            bot: QQAndroidBot,
            context: Group,
            md5: ByteArray,
            type: ImageType,
            size: Long,
            width: Int,
            height: Int
        ): Boolean {
            val response: ImgStore.GroupPicUp.Response = bot.network.sendAndExpect(
                ImgStore.GroupPicUp(
                    bot.client,
                    uin = bot.id,
                    groupCode = context.id,
                    md5 = md5,
                    size = size,
                    filename = "${md5.toUHexString("")}.${type.formatName}",
                    picWidth = width,
                    picHeight = height,
                    picType = getIdByImageType(type),
                )
            )

            return response is ImgStore.GroupPicUp.Response.FileExists
        }
    }

    class ImageUploadedCheckerUser : ImageUploadedChecker<User> {
        override suspend fun isUploaded(
            bot: QQAndroidBot,
            context: User,
            md5: ByteArray,
            type: ImageType,
            size: Long,
            width: Int,
            height: Int
        ): Boolean {
            val resp = bot.network.sendAndExpect(
                LongConn.OffPicUp(
                    bot.client,
                    Cmd0x352.TryUpImgReq(
                        buType = 1,
                        srcUin = bot.id,
                        dstUin = context.id,
                        fileMd5 = md5,
                        fileSize = size,
                        imgWidth = width,
                        imgHeight = height,
                        imgType = getIdByImageType(type),
                        fileName = "${md5.toUHexString("")}.${type.formatName}",
                        //For gif, using not original
                        imgOriginal = (type != ImageType.GIF),
                        buildVer = bot.client.buildVer,
                    ),
                )
            )

            return resp is LongConn.OffPicUp.Response.FileExists
        }
    }

    class ImageUploadedCheckerFallback : ImageUploadedChecker<Nothing?> {
        override suspend fun isUploaded(
            bot: QQAndroidBot,
            context: Nothing?,
            md5: ByteArray,
            type: ImageType,
            size: Long,
            width: Int,
            height: Int
        ): Boolean {
            val response: ImgStore.GroupPicUp.Response = bot.network.sendAndExpect(
                ImgStore.GroupPicUp(
                    bot.client,
                    uin = bot.id,
                    groupCode = 1,
                    md5 = md5,
                    size = size,
                    filename = "${md5.toUHexString("")}.${type.formatName}",
                    picWidth = width,
                    picHeight = height,
                    picType = getIdByImageType(type),
                )
            )

            return response is ImgStore.GroupPicUp.Response.FileExists
        }
    }

    fun findExistImageByCache(imageId: String): Image? {
        Bot.instancesSequence.forEach { existsBot ->
            runCatching {
                val patcher = existsBot.asQQAndroidBot().components[ImagePatcher]

                patcher.findCacheByImageId(imageId)?.withCache { cache ->
                    val rsp = cache.cacheOGI.value0
                    if (rsp != null) return rsp
                }
            }
        }
        return null
    }

    override fun createImage(
        imageId: String,
        size: Long,
        type: ImageType,
        width: Int,
        height: Int,
        isEmoji: Boolean
    ): Image {
        return when {
            imageId matches Image.IMAGE_ID_REGEX -> {
                if (size == 0L && width == 0 && height == 0) {
                    findExistImageByCache(imageId)?.let { return it }
                }
                OfflineGroupImage(imageId, width, height, size, type, isEmoji)
            }
            imageId matches Image.IMAGE_RESOURCE_ID_REGEX_1 -> {
                OfflineFriendImage(imageId, width, height, size, type, isEmoji)
            }
            imageId matches Image.IMAGE_RESOURCE_ID_REGEX_2 -> {
                OfflineFriendImage(imageId, width, height, size, type, isEmoji)
            }
            else ->
                @Suppress("INVISIBLE_MEMBER")
                throw IllegalArgumentException("Illegal imageId: $imageId. ${net.mamoe.mirai.message.data.ILLEGAL_IMAGE_ID_EXCEPTION_MESSAGE}")
        }
    }

    override suspend fun isUploaded(
        bot: Bot,
        md5: ByteArray,
        size: Long,
        context: Contact?,
        type: ImageType,
        width: Int,
        height: Int
    ): Boolean {
        val checker = findChecker(context) ?: return false
        checker.cast<ImageUploadedChecker<Contact?>>()
        return checker.isUploaded(bot.asQQAndroidBot(), context.cast<Contact?>(), md5, type, size, width, height)
    }

    fun findChecker(context: Contact?) = ImageUploadedChecker.checkers.asSequence()
        .find { bothNull(it.key, context) || it.key?.isInstance(context) == true }?.value

    private fun bothNull(a: Any?, b: Any?) = a == null && b == null
}