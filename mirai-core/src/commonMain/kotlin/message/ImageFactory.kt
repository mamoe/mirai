/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message

import net.mamoe.mirai.Bot
import net.mamoe.mirai.internal.asQQAndroidBot
import net.mamoe.mirai.internal.utils.ImagePatcher
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.ImageType

internal class ImageFactoryImpl : Image.Factory {
    override fun create(
        imageId: String,
        size: Long,
        type: ImageType,
        width: Int,
        height: Int,
        isEmoji: Boolean
    ): Image {
        return when {
            imageId matches Image.IMAGE_ID_REGEX -> {
                Bot.instancesSequence.forEach { existsBot ->
                    runCatching {
                        val patcher = existsBot.asQQAndroidBot().components[ImagePatcher]

                        patcher.findCacheByImageId(imageId)?.let { cache ->
                            val rsp = cache.cacheOGI.value0
                            cache.accessLock.release()
                            if (rsp != null) return rsp
                        }
                    }
                }
                OfflineGroupImage(imageId)
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
}