/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("DEPRECATION_ERROR")

package net.mamoe.mirai.internal.message

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import net.mamoe.mirai.Bot
import net.mamoe.mirai.IMirai
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.contact.ContactOrBot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.internal.network.protocol.data.proto.*
import net.mamoe.mirai.internal.utils.io.serialization.loadAs
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray
import net.mamoe.mirai.internal.utils.structureToString
import net.mamoe.mirai.message.data.FlashImage
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Image.Key.IMAGE_ID_REGEX
import net.mamoe.mirai.message.data.ImageType
import net.mamoe.mirai.utils.*
import net.mamoe.mirai.utils.ExternalResource.Companion.DEFAULT_FORMAT_NAME


/**
 * 所有 [Image] 实现的基类.
 */
// moved from mirai-core-api since 2.11
internal sealed class AbstractImage : Image {
    private val _stringValue: String? by lazy(LazyThreadSafetyMode.NONE) { "[mirai:image:$imageId]" }

    override val size: Long
        get() = 0L
    override val width: Int
        get() = 0
    override val height: Int
        get() = 0
    override val imageType: ImageType
        get() = ImageType.UNKNOWN

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
        return this.imageId == other.imageId
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
        get() = getImageTypeById(delegate.imageType)

    override val imageId: String = generateImageId(
        delegate.picMd5,
        delegate.filePath.substringAfterLast('.').lowercase().let { ext ->
            if (ext == "null") {
                // official clients might send `null`
                getImageType(delegate.imageType)
            } else ext
        }
    ).takeIf {
        IMAGE_ID_REGEX.matches(it)
    } ?: generateImageId(delegate.picMd5)

    override val originUrl: String
        get() = if (delegate.origUrl.isBlank()) {
            gchatImageUrlByImageId(imageId)
        } else "http://gchat.qpic.cn" + delegate.origUrl

    override val isEmoji: Boolean by lazy {
        delegate.pbReserve.pbImageResv_checkIsEmoji(CustomFaceExtPb.ResvAttr.serializer())
    }
}

private fun <T : ImgExtPbResvAttrCommon> ByteArray.pbImageResv_checkIsEmoji(serializer: KSerializer<T>): Boolean {
    val data = this
    return kotlin.runCatching {
        data.takeIf { it.isNotEmpty() }?.loadAs(serializer)?.let { ext ->
            ext.imageBizType == 1 || ext.textSummary.decodeToString() == "[动画表情]"
        }
    }.getOrNull() ?: false
}

private fun gchatImageUrlByImageId(imageId: String) =
    "http://gchat.qpic.cn/gchatpic_new/0/0-0-${
        imageId.substring(1..36)
            .replace("-", "")
    }/0?term=2"


private val imageLogger: MiraiLogger by lazy { MiraiLogger.Factory.create(Image::class) }
internal val Image.Key.logger get() = imageLogger

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
        get() = getImageTypeById(delegate.imgType)
    override val imageId: String = kotlin.run {
        val imageType = getImageType(delegate.imgType)
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

/*
 * ImgType:
 *  JPG:    1000
 *  PNG:    1001
 *  WEBP:   1002
 *  BMP:    1005
 *  GIG:    2000 // gig? gif?
 *  APNG:   2001
 *  SHARPP: 1004
 */

internal val UNKNOWN_IMAGE_TYPE_PROMPT_ENABLED = systemProp("mirai.unknown.image.type.logging", false)

internal fun getImageTypeById(id: Int): ImageType {
    return if (id == 2001) {
        ImageType.APNG
    } else {
        ImageType.match(getImageType(id))
    }
}

internal fun getIdByImageType(imageType: ImageType): Int {
    return when (imageType) {
        ImageType.JPG -> 1000
        ImageType.PNG -> 1001
        //ImageType.WEBP -> 1002 //Unsupported by pc client
        ImageType.BMP -> 1005
        ImageType.GIF -> 2000
        ImageType.APNG -> 2001
        //default to jpg
        else -> 1000
    }
}

internal data class ImageInfo(val width: Int = 0, val height: Int = 0, val imageType: ImageType = ImageType.UNKNOWN)

internal fun getImageType(id: Int): String {
    return when (id) {
        1000 -> "jpg"
        1001 -> "png"
        //1002 -> "webp" //Unsupported by pc client
        1005 -> "bmp"
        2000, 3, 4 -> "gif"
        //apng
        2001 -> "png"
        else -> {
            if (UNKNOWN_IMAGE_TYPE_PROMPT_ENABLED) {
                Image.logger.debug(
                    "Unknown image id: $id. Stacktrace:",
                    Exception()
                )
            }
            DEFAULT_FORMAT_NAME
        }
    }
}

internal fun ImMsgBody.NotOnlineImage.toCustomFace(): ImMsgBody.CustomFace {

    return ImMsgBody.CustomFace(
        filePath = generateImageId(picMd5, getImageType(imgType)),
        picMd5 = picMd5,
        bizType = 5,
        fileType = 66,
        useful = 1,
        flag = ByteArray(4),
        bigUrl = bigUrl,
        origUrl = origUrl,
        width = picWidth.coerceAtLeast(1),
        height = picHeight.coerceAtLeast(1),
        imageType = imgType,
        //_400Height = 235,
        //_400Url = "/gchatpic_new/000000000/1041235568-2195821338-01E9451B70EDEAE3B37C101F1EEBF5B5/400?term=2",
        //_400Width = 351,
        origin = original,
        size = fileLen.toInt()
    )
}

// aka friend image id
internal fun ImMsgBody.NotOnlineImageOrCustomFace.calculateResId(): String {
    val url = origUrl.takeIf { it.isNotBlank() }
        ?: thumbUrl.takeIf { it.isNotBlank() }
        ?: _400Url.takeIf { it.isNotBlank() }
        ?: ""

    // gchatpic_new
    // offpic_new
    val picSenderId = url.substringAfter("pic_new/").substringBefore("/")
        .takeIf { it.isNotBlank() } ?: "000000000"
    val unknownInt = url.substringAfter("-").substringBefore("-")
        .takeIf { it.isNotBlank() } ?: "000000000"

    return "/$picSenderId-$unknownInt-${picMd5.toUHexString("")}"
}

internal fun ImMsgBody.CustomFace.toNotOnlineImage(): ImMsgBody.NotOnlineImage {
    val resId = calculateResId()

    return ImMsgBody.NotOnlineImage(
        filePath = filePath,
        resId = resId,
        oldPicMd5 = false,
        picWidth = width,
        picHeight = height,
        imgType = imageType,
        picMd5 = picMd5,
        fileLen = size.toLong(),
        oldVerSendFile = oldData,
        downloadPath = resId,
        original = origin,
        bizType = bizType,
        pbReserve = byteArrayOf(0x78, 0x02),
    )
}

@Suppress("DEPRECATION")
internal fun OfflineGroupImage.toJceData(): ImMsgBody.CustomFace {
    return ImMsgBody.CustomFace(
        fileId = this.fileId ?: 0,
        filePath = this.imageId,
        picMd5 = this.md5,
        flag = ByteArray(4),
        size = size.toInt(),
        width = width.coerceAtLeast(1),
        height = height.coerceAtLeast(1),
        imageType = getIdByImageType(imageType),
        origin = if (imageType == ImageType.GIF) {
            0
        } else {
            1
        },
        //_400Height = 235,
        //_400Url = "/gchatpic_new/000000000/1041235568-2195821338-01E9451B70EDEAE3B37C101F1EEBF5B5/400?term=2",
        //_400Width = 351,
        //        pbReserve = "08 00 10 00 32 00 50 00 78 08".autoHexToBytes(),
        bizType = 5,
        fileType = 66,
        useful = 1,
        //  pbReserve = CustomFaceExtPb.ResvAttr().toByteArray(CustomFaceExtPb.ResvAttr.serializer())
    )
}


@Suppress("DEPRECATION")
internal fun OfflineFriendImage.toJceData(): ImMsgBody.NotOnlineImage {
    val friendImageId = this.friendImageId
    return ImMsgBody.NotOnlineImage(
        filePath = friendImageId,
        resId = friendImageId,
        oldPicMd5 = false,
        picMd5 = this.md5,
        fileLen = size,
        downloadPath = friendImageId,
        original = if (imageType == ImageType.GIF) {
            0
        } else {
            1
        },
        picWidth = width,
        picHeight = height,
        imgType = getIdByImageType(imageType),
        pbReserve = byteArrayOf(0x78, 0x02)
    )
}


internal interface ConstOriginUrlAware {
    val originUrl: String
}

internal interface DeferredOriginUrlAware {
    fun getUrl(bot: Bot): String
}

internal interface SuspendDeferredOriginUrlAware {
    suspend fun getUrl(bot: Bot): String
}

internal interface OnlineImage : Image, ConstOriginUrlAware {
    override val originUrl: String
}

/**
 * 离线的图片, 即为客户端主动上传到服务器而获得的 [Image] 实例.
 * 不能直接获取它在服务器上的链接. 需要通过 [IMirai.queryImageUrl] 查询
 *
 * 一般由 [Contact.uploadImage] 得到
 */
internal interface OfflineImage : Image

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
        require(imageId matches IMAGE_ID_REGEX) {
            "Illegal imageId. It must matches GROUP_IMAGE_ID_REGEX"
        }
    }
}

/**
 * 接收消息时获取到的 [GroupImage]. 它可以直接获取下载链接 [originUrl]
 */
@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
internal abstract class OnlineGroupImage : GroupImage(), OnlineImage

internal val Image.friendImageId: String
    get() {
        //    /1234567890-3666252994-EFF4427CE3D27DB6B1D9A8AB72E7A29C
        return "/000000000-000000000-${md5.toUHexString("")}"
    }

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
 * 接收消息时获取到的 [FriendImage]. 它可以直接获取下载链接 [originUrl]
 */
internal abstract class OnlineFriendImage : FriendImage(), OnlineImage

// endregion


internal fun FlashImage.toJceData(messageTarget: ContactOrBot?): ImMsgBody.Elem {
    return if (messageTarget is User) {
        ImMsgBody.Elem(
            commonElem = ImMsgBody.CommonElem(
                serviceType = 3,
                businessType = 0,
                pbElem = HummerCommelem.MsgElemInfoServtype3(
                    flashC2cPic = ImMsgBody.NotOnlineImage(
                        filePath = image.friendImageId,
                        resId = image.friendImageId,
                        picMd5 = image.md5,
                        oldPicMd5 = false,
                        pbReserve = byteArrayOf(0x78, 0x06)
                    )
                ).toByteArray(HummerCommelem.MsgElemInfoServtype3.serializer())
            )
        )
    } else {
        ImMsgBody.Elem(
            commonElem = ImMsgBody.CommonElem(
                serviceType = 3,
                businessType = 0,
                pbElem = HummerCommelem.MsgElemInfoServtype3(
                    flashTroopPic = ImMsgBody.CustomFace(
                        filePath = image.imageId,
                        picMd5 = image.md5,
                        pbReserve = byteArrayOf(0x78, 0x06)
                    )
                ).toByteArray(HummerCommelem.MsgElemInfoServtype3.serializer())
            )
        )
    }
}