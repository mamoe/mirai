/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message

import net.mamoe.mirai.internal.contact.AbstractContact
import net.mamoe.mirai.internal.contact.GroupImpl
import net.mamoe.mirai.internal.network.protocol.packet.chat.image.ImgStore
import net.mamoe.mirai.internal.network.protocol.packet.sendAndExpect
import net.mamoe.mirai.message.data.FriendImage
import net.mamoe.mirai.message.data.md5
import net.mamoe.mirai.utils.toLongUnsigned

internal class ImageIdFixer {
    private val groupImageIdCache: MutableMap<String, Long?> = mutableMapOf()
    private val friendImageToGroupImageCache: MutableMap<String, OfflineGroupImage> = mutableMapOf()

    suspend fun fix(image: OfflineGroupImage, contact: AbstractContact) {
        groupImageIdCache[image.imageId]?.let {
            image.fileId = it.toInt()
            return
        }

        image.fixImageFileId(contact).also {
            groupImageIdCache[image.imageId] = image.fileId?.toLongUnsigned()
        }
    }

    // kept for future use (e.g. https://github.com/mamoe/mirai/pull/1477)
    suspend fun convertFriendImageToGroupImage(image: FriendImage, contact: GroupImpl): OfflineGroupImage {
        friendImageToGroupImageCache[image.imageId]?.let { return it }

        return image.toOfflineGroupImageForSend(contact).also {
            friendImageToGroupImageCache[image.imageId] = it
        }
    }

    companion object {
        suspend fun OfflineGroupImage.fixImageFileId(contact: AbstractContact) {
            if (fileId == null) {
                val response: ImgStore.GroupPicUp.Response = ImgStore.GroupPicUp(
                    contact.bot.client,
                    uin = contact.bot.id,
                    groupCode = contact.id,
                    md5 = md5,
                    size = 1,
                ).sendAndExpect(contact.bot)

                fileId = when (response) {
                    is ImgStore.GroupPicUp.Response.Failed -> {
                        0 // Failed
                    }
                    is ImgStore.GroupPicUp.Response.FileExists -> {
                        response.fileId.toInt()
                    }
                    is ImgStore.GroupPicUp.Response.RequireUpload -> {
                        response.fileId.toInt()
                    }
                }
            }
        }

        /**
         * Ensures server holds the cache
         */
        suspend fun FriendImage.toOfflineGroupImageForSend(group: GroupImpl): OfflineGroupImage {
            group.bot.network.run {
                val response = ImgStore.GroupPicUp(
                    group.bot.client,
                    uin = group.bot.id,
                    groupCode = group.id,
                    md5 = md5,
                    size = size
                ).sendAndExpect()
                return OfflineGroupImage(
                    imageId = imageId,
                    width = width,
                    height = height,
                    size = if (response is ImgStore.GroupPicUp.Response.FileExists) {
                        response.fileInfo.fileSize
                    } else {
                        size
                    },
                    imageType = imageType
                ).also { img ->
                    when (response) {
                        is ImgStore.GroupPicUp.Response.FileExists -> {
                            img.fileId = response.fileId.toInt()
                        }
                        is ImgStore.GroupPicUp.Response.RequireUpload -> {
                            img.fileId = response.fileId.toInt()
                        }
                        is ImgStore.GroupPicUp.Response.Failed -> {
                            img.fileId = 0
                        }
                    }
                }
            }
        }
    }
}