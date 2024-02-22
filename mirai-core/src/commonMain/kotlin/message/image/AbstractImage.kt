/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.image

import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.utils.MiraiLogger

/**
 * 所有 [Image] 实现的基类.
 */
// moved from mirai-core-api since 2.11
internal sealed class AbstractImage : Image {
    private val _stringValue: String? by lazy(LazyThreadSafetyMode.NONE) { "[mirai:image:$imageId, width=$width, height=$height, size=$size, type=$imageType, isEmoji=$isEmoji]" }

    override val size: Long
        get() = 0L
    override val width: Int
        get() = 0
    override val height: Int
        get() = 0

    final override fun toString(): String = _stringValue!!
    final override fun contentToString(): String = if (isEmoji) {
        "[动画表情]"
    } else {
        "[图片]"
    }

    override fun appendMiraiCodeTo(builder: StringBuilder) {
        builder.append("[mirai:image:").append(imageId).append("]")
    }

    final override fun hashCode(): Int = imageId.hashCode()
    final override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is Image) return false
        return this.imageId == other.imageId &&
                this.width == other.width &&
                this.height == other.height &&
                this.isEmoji == other.isEmoji &&
                this.imageType == other.imageType &&
                this.size == other.size
    }
}

/**
 * 好友图片
 *
 * [imageId] 形如 `/f8f1ab55-bf8e-4236-b55e-955848d7069f` (37 长度)  或 `/000000000-3814297509-BFB7027B9354B8F899A062061D74E206` (54 长度)
 */
// NotOnlineImage
// moved from mirai-core-api since 2.11
internal sealed class FriendImage : AbstractImage()

/**
 * 群图片.
 *
 * @property imageId 形如 `{01E9451B-70ED-EAE3-B37C-101F1EEBF5B5}.ext` (ext系扩展名)
 * @see Image 查看更多说明
 */
// CustomFace
// moved from mirai-core-api since 2.11
internal sealed class GroupImage : AbstractImage()

// NT Image
internal sealed class NewTechImage : AbstractImage()

private val imageLogger: MiraiLogger by lazy { MiraiLogger.Factory.create(Image::class, "Image") }
internal val Image.Key.logger get() = imageLogger