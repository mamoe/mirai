/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.image

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import net.mamoe.mirai.internal.message.contextualBugReportException
import net.mamoe.mirai.internal.network.protocol.data.proto.CustomFaceExtPb
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.network.protocol.data.proto.ImgExtPbResvAttrCommon
import net.mamoe.mirai.internal.network.protocol.data.proto.NotOnlineImageExtPb
import net.mamoe.mirai.internal.utils.io.serialization.loadAs
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.ImageType
import net.mamoe.mirai.utils.debug.structureToString
import net.mamoe.mirai.utils.generateImageId
import net.mamoe.mirai.utils.generateImageIdFromResourceId

internal sealed interface OnlineImage : Image, ConstOriginUrlAware {
    override val originUrl: String
}

/**
 * 接收消息时获取到的 [FriendImage]. 它可以直接获取下载链接 [originUrl]
 */
internal sealed class OnlineFriendImage : FriendImage(), OnlineImage


@Suppress("SERIALIZER_TYPE_INCOMPATIBLE")
@Serializable(with = OnlineFriendImageImpl.Serializer::class)
internal class OnlineFriendImageImpl(
    internal val delegate: ImMsgBody.NotOnlineImage,
) : @Suppress("DEPRECATION")
OnlineFriendImage() {
    object Serializer : Image.FallbackSerializer("OnlineFriendImage")

    override val size: Long get() = delegate.fileLen
    override val width: Int
        get() = delegate.picWidth
    override val height: Int
        get() = delegate.picHeight
    override val imageType: ImageType
        get() = OnlineImageIds.speculateImageType(delegate.filePath, delegate.imgType)
    override val imageId: String = kotlin.run {
        val imageType = imageType.formatName
        generateImageIdFromResourceId(delegate.resId, imageType)
            ?: kotlin.run {
                if (delegate.picMd5.size == 16) generateImageId(delegate.picMd5, imageType)
                else {
                    Image.logger.warning(
                        contextualBugReportException(
                            "Failed to compute friend imageId: resId=${delegate.resId}",
                            delegate.structureToString(),
                            additional = "并描述此时 Bot 是否正在从好友或群接受消息, 尽量附加该图片原文件"
                        )
                    )
                    delegate.resId
                }
            }
    }

    override val originUrl: String
        get() = if (delegate.origUrl.isNotBlank()) {
            "http://c2cpicdw.qpic.cn" + this.delegate.origUrl
        } else if (delegate.resId.isNotEmpty() && delegate.resId[0] == '{') {
            // https://github.com/mamoe/mirai/issues/1600
            gchatImageUrlByImageId(imageId)
        } else {
            "http://c2cpicdw.qpic.cn/offpic_new/0/" + delegate.resId + "/0?term=2"
        }

    override val isEmoji: Boolean by lazy {
        delegate.pbReserve.pbImageResv_checkIsEmoji(NotOnlineImageExtPb.ResvAttr.serializer())
    }
}


private fun gchatImageUrlByImageId(imageId: String) =
    "http://gchat.qpic.cn/gchatpic_new/0/0-0-${
        imageId.substring(1..36)
            .replace("-", "")
    }/0?term=2"


private fun <T : ImgExtPbResvAttrCommon> ByteArray.pbImageResv_checkIsEmoji(serializer: KSerializer<T>): Boolean {
    val data = this
    return kotlin.runCatching {
        data.takeIf { it.isNotEmpty() }?.loadAs(serializer)?.let { ext ->
            ext.imageBizType == 1 || ext.textSummary.decodeToString() == "[动画表情]"
        }
    }.getOrNull() ?: false
}

/**
 * 接收消息时获取到的 [GroupImage]. 它可以直接获取下载链接 [originUrl]
 */
@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
internal sealed class OnlineGroupImage : GroupImage(), OnlineImage


@Suppress("SERIALIZER_TYPE_INCOMPATIBLE")
@Serializable(with = OnlineGroupImageImpl.Serializer::class)
internal class OnlineGroupImageImpl(
    internal val delegate: ImMsgBody.CustomFace,
) : OnlineGroupImage() {
    object Serializer : Image.FallbackSerializer("OnlineGroupImage")

    override val size: Long get() = delegate.size.toLong()
    override val width: Int
        get() = delegate.width
    override val height: Int
        get() = delegate.height
    override val imageType: ImageType
        get() = OnlineImageIds.speculateImageType(delegate.filePath, delegate.imageType)

    override val imageId: String = generateImageId(
        delegate.picMd5,
        OnlineImageIds.speculateImageTypeNameFromFilePath(delegate.filePath)
    ).takeIf {
        Image.IMAGE_ID_REGEX.matches(it)
    } ?: generateImageId(delegate.picMd5)

    override val originUrl: String
        get() = if (delegate.origUrl.isBlank()) {
            gchatImageUrlByImageId(imageId)
        } else "http://gchat.qpic.cn" + delegate.origUrl

    override val isEmoji: Boolean by lazy {
        delegate.pbReserve.pbImageResv_checkIsEmoji(CustomFaceExtPb.ResvAttr.serializer())
    }
}

private object OnlineImageIds {

    fun speculateImageType(filePath: String, imageTypeInt: Int): ImageType {
        return getImageTypeById(imageTypeInt) ?: speculateImageTypeFromImageId(filePath) ?: ImageType.UNKNOWN
    }

    fun speculateImageTypeFromImageId(filePathOrImageId: String): ImageType? {
        return speculateImageTypeNameFromFilePath(filePathOrImageId)?.let { ImageType.matchOrNull(it) }
    }

    /**
     * @param filePath should ends with `.type`
     */
    fun speculateImageTypeNameFromFilePath(filePath: String): String? {
        return filePath.substringAfterLast('.').lowercase().let { ext ->
            if (ext == "null") {
                // official clients might send `null`
                null
            } else ext
        }
    }
}