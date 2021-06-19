/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("DEPRECATION_ERROR")

package net.mamoe.mirai.internal.message

import kotlinx.serialization.Serializable
import net.mamoe.mirai.Bot
import net.mamoe.mirai.IMirai
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.contact.ContactOrBot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.internal.network.protocol.data.proto.HummerCommelem
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.utils._miraiContentToString
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.Image.Key.IMAGE_ID_REGEX
import net.mamoe.mirai.utils.*
import net.mamoe.mirai.utils.ExternalResource.Companion.DEFAULT_FORMAT_NAME

@Serializable(with = OnlineGroupImageImpl.Serializer::class)
internal class OnlineGroupImageImpl(
    internal val delegate: ImMsgBody.CustomFace
) : OnlineGroupImage() {
    object Serializer : Image.FallbackSerializer("OnlineGroupImage")


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
            "http://gchat.qpic.cn/gchatpic_new/0/0-0-${
                imageId.substring(1..36)
                    .replace("-", "")
            }/0?term=2"
        } else "http://gchat.qpic.cn" + delegate.origUrl

    override fun equals(other: Any?): Boolean {
        return other is OnlineGroupImageImpl && other.imageId == this.imageId
    }

    override fun hashCode(): Int {
        return imageId.hashCode() + 31 * md5.hashCode()
    }
}

@Serializable(with = OnlineFriendImageImpl.Serializer::class)
internal class OnlineFriendImageImpl(
    internal val delegate: ImMsgBody.NotOnlineImage
) : @Suppress("DEPRECATION")
OnlineFriendImage() {
    object Serializer : Image.FallbackSerializer("OnlineFriendImage")

    override val imageId: String = kotlin.run {
        val imageType = getImageType(delegate.imgType)
        generateImageIdFromResourceId(delegate.resId, imageType)
            ?: kotlin.run {
                if (delegate.picMd5.size == 16) generateImageId(delegate.picMd5, imageType)
                else {
                    MiraiLogger.TopLevel.warning(
                        contextualBugReportException(
                            "Failed to compute friend imageId: resId=${delegate.resId}",
                            delegate._miraiContentToString(),
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
        } else {
            "http://c2cpicdw.qpic.cn/offpic_new/0/" + delegate.resId + "/0?term=2"
        }
    // TODO: 2020/4/24 动态获取图片下载链接的 host

    override fun equals(other: Any?): Boolean {
        return other is OnlineFriendImageImpl && other.imageId == this.imageId
    }

    override fun hashCode(): Int {
        return imageId.hashCode() + 31 * md5.hashCode()
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

internal fun getImageType(id: Int): String {
    return when (id) {
        1000 -> "jpg"
        1001 -> "png"
        1002 -> "webp"
        1005 -> "bmp"
        2000 -> "gif"
        2001, 3 -> "png"
        else -> {
            if (UNKNOWN_IMAGE_TYPE_PROMPT_ENABLED) {
                MiraiLogger.TopLevel.debug(
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
        flag = ByteArray(4),
        bigUrl = bigUrl,
        origUrl = origUrl,
        //_400Height = 235,
        //_400Url = "/gchatpic_new/000000000/1041235568-2195821338-01E9451B70EDEAE3B37C101F1EEBF5B5/400?term=2",
        //_400Width = 351,
        oldData = this.oldVerSendFile
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
        picMd5 = picMd5,
        downloadPath = resId,
        original = 1,
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
        //_400Height = 235,
        //_400Url = "/gchatpic_new/000000000/1041235568-2195821338-01E9451B70EDEAE3B37C101F1EEBF5B5/400?term=2",
        //_400Width = 351,
        oldData = oldData,
//        pbReserve = "08 00 10 00 32 00 50 00 78 08".autoHexToBytes(),
//        useful = 1,
        //  pbReserve = CustomFaceExtPb.ResvAttr().toByteArray(CustomFaceExtPb.ResvAttr.serializer())
    )
}

private val oldData: ByteArray =
    "15 36 20 39 32 6B 41 31 00 38 37 32 66 30 36 36 30 33 61 65 31 30 33 62 37 20 20 20 20 20 20 35 30 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 7B 30 31 45 39 34 35 31 42 2D 37 30 45 44 2D 45 41 45 33 2D 42 33 37 43 2D 31 30 31 46 31 45 45 42 46 35 42 35 7D 2E 70 6E 67 41".hexToBytes()


@Suppress("DEPRECATION")
internal fun OfflineFriendImage.toJceData(): ImMsgBody.NotOnlineImage {
    val friendImageId = this.friendImageId
    return ImMsgBody.NotOnlineImage(
        filePath = friendImageId,
        resId = friendImageId,
        oldPicMd5 = false,
        picMd5 = this.md5,
        downloadPath = friendImageId,
        original = 1,
        pbReserve = byteArrayOf(0x78, 0x02)
    )
}


/**
 * 所有 [Image] 实现的基类.
 */
internal abstract class AbstractImage : Image { // make sealed in 1.3.0 ?
    private var _stringValue: String? = null
        get() = field ?: kotlin.run {
            field = "[mirai:image:$imageId]"
            field
        }

    final override fun toString(): String = _stringValue!!
    final override fun contentToString(): String = "[图片]"

    @MiraiExperimentalApi
    override fun appendMiraiCodeTo(builder: StringBuilder) {
        builder.append("[mirai:image:").append(imageId).append("]")
    }
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
@Serializable(with = OfflineGroupImage.Serializer::class)
internal data class OfflineGroupImage(
    override val imageId: String
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
@Serializable(with = OfflineFriendImage.Serializer::class)
internal data class OfflineFriendImage(
    override val imageId: String
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