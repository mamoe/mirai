/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.framework.components

import net.mamoe.mirai.internal.contact.GroupImpl
import net.mamoe.mirai.internal.message.image.FriendImage
import net.mamoe.mirai.internal.message.image.OfflineGroupImage
import net.mamoe.mirai.internal.utils.ImageCache
import net.mamoe.mirai.internal.utils.ImagePatcher
import net.mamoe.mirai.internal.utils.ImagePatcherImpl

internal class TestImagePatcher : ImagePatcher {
    val patchedOfflineGroupImages: MutableList<OfflineGroupImage> = mutableListOf()
    val patchedFriendImages: MutableList<FriendImage> = mutableListOf()

    private val patcher = ImagePatcherImpl()

    override fun findCacheByImageId(id: String): ImageCache? {
        return patcher.findCacheByImageId(id)
    }

    override fun putCache(image: OfflineGroupImage) {
        patcher.putCache(image)
    }

    override suspend fun patchOfflineGroupImage(group: GroupImpl, image: OfflineGroupImage) {
        patchedOfflineGroupImages.add(image)
    }

    override suspend fun patchFriendImageToGroupImage(group: GroupImpl, image: FriendImage): OfflineGroupImage {
        patchedFriendImages.add(image)
        return OfflineGroupImage(image.imageId, image.width, image.height, image.size, image.imageType, image.isEmoji)
    }
}