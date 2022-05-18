/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.image

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.mamoe.mirai.Bot
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.ImageType

/**
 * 离线的图片, 即为客户端主动上传到服务器而获得的 [Image] 实例.
 * 不能直接获取它在服务器上的链接. 需要通过 [IMirai.queryImageUrl] 查询
 *
 * 一般由 [Contact.uploadImage] 得到
 */
internal sealed interface OfflineImage : Image

/**
 * 通过 [Group.uploadImage] 上传得到的 [GroupImage]. 它的链接需要查询 [IMirai.queryImageUrl]
 *
 * @param imageId 参考 [Image.imageId]
 */
@Suppress("SERIALIZER_TYPE_INCOMPATIBLE")
@Serializable(with = OfflineFriendImage.Serializer::class)
internal data class OfflineFriendImage(
    override val imageId: String,
    override val width: Int = 0,
    override val height: Int = 0,
    override val size: Long = 0L,
    override val imageType: ImageType = ImageType.UNKNOWN,
    override val isEmoji: Boolean = false,
) : FriendImage(), OfflineImage, DeferredOriginUrlAware {
    object Serializer : Image.FallbackSerializer("OfflineFriendImage")

    override fun getUrl(bot: Bot): String {
        return "http://c2cpicdw.qpic.cn/offpic_new/${bot.id}${this.friendImageId}/0?term=2"
    }
}

/**
 * @param imageId 参考 [Image.imageId]
 */
@Suppress("SERIALIZER_TYPE_INCOMPATIBLE")
@Serializable(with = OfflineGroupImage.Serializer::class)
internal data class OfflineGroupImage(
    override val imageId: String,
    override val width: Int = 0,
    override val height: Int = 0,
    override val size: Long = 0L,
    override val imageType: ImageType = ImageType.UNKNOWN,
    override val isEmoji: Boolean = false,
) : GroupImage(), OfflineImage, DeferredOriginUrlAware {
    @Transient
    internal var fileId: Int? = null

    object Serializer : Image.FallbackSerializer("OfflineGroupImage")

    override fun getUrl(bot: Bot): String {
        return "http://gchat.qpic.cn/gchatpic_new/${bot.id}/0-0-${
            imageId.substring(1..36)
                .replace("-", "")
        }/0?term=2"
    }

    init {
        @Suppress("DEPRECATION")
        require(imageId matches Image.IMAGE_ID_REGEX) {
            "Illegal imageId. It must matches GROUP_IMAGE_ID_REGEX"
        }
    }
}
