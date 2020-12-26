/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
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
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.message.data.FriendImage
import net.mamoe.mirai.message.data.GroupImage
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Image.Key.FRIEND_IMAGE_ID_REGEX_1
import net.mamoe.mirai.message.data.Image.Key.FRIEND_IMAGE_ID_REGEX_2
import net.mamoe.mirai.message.data.Image.Key.GROUP_IMAGE_ID_REGEX
import net.mamoe.mirai.message.data.md5
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.generateImageId
import net.mamoe.mirai.utils.hexToBytes

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

internal class OnlineGroupImageImpl(
    internal val delegate: ImMsgBody.CustomFace
) : @Suppress("DEPRECATION")
OnlineGroupImage() {
    override val imageId: String = generateImageId(
        delegate.md5,
        delegate.filePath.substringAfterLast('.')
    ).takeIf {
        GROUP_IMAGE_ID_REGEX.matches(it)
    } ?: generateImageId(delegate.md5)

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

internal class OnlineFriendImageImpl(
    internal val delegate: ImMsgBody.NotOnlineImage
) : @Suppress("DEPRECATION")
OnlineFriendImage() {
    override val imageId: String get() = delegate.resId
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

@Suppress("DEPRECATION")
internal fun OfflineGroupImage.toJceData(): ImMsgBody.CustomFace {
    return ImMsgBody.CustomFace(
        filePath = this.imageId,
        md5 = this.md5,
        flag = ByteArray(4),
        //_400Height = 235,
        //_400Url = "/gchatpic_new/1040400290/1041235568-2195821338-01E9451B70EDEAE3B37C101F1EEBF5B5/400?term=2",
        //_400Width = 351,
        oldData = oldData
    )
}

private val oldData: ByteArray =
    "15 36 20 39 32 6B 41 31 00 38 37 32 66 30 36 36 30 33 61 65 31 30 33 62 37 20 20 20 20 20 20 35 30 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 7B 30 31 45 39 34 35 31 42 2D 37 30 45 44 2D 45 41 45 33 2D 42 33 37 43 2D 31 30 31 46 31 45 45 42 46 35 42 35 7D 2E 70 6E 67 41".hexToBytes()


@Suppress("DEPRECATION")
internal fun OfflineFriendImage.toJceData(): ImMsgBody.NotOnlineImage {
    return ImMsgBody.NotOnlineImage(
        filePath = this.imageId,
        resId = this.imageId,
        oldPicMd5 = false,
        picMd5 = this.md5,
        downloadPath = this.imageId,
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

    override fun appendMiraiCode(builder: StringBuilder) {
        builder.append("[mirai:image:").append(imageId).append("]")
    }
}

internal interface ConstOriginUrlAware : Image {
    val originUrl: String
}

internal interface DeferredOriginUrlAware : Image {
    fun getUrl(bot: Bot): String
}

internal interface SuspendDeferredOriginUrlAware : Image {
    suspend fun getUrl(bot: Bot): String
}

/**
 * 由 [ExternalImage] 委托的 [Image] 类型.
 */
@MiraiExperimentalApi("Will be renamed to OfflineImage on 1.2.0")
@Suppress("DEPRECATION_ERROR")
internal class ExperimentalDeferredImage internal constructor(
    @Suppress("CanBeParameter") private val externalImage: ExternalResource // for future use
) : AbstractImage(), SuspendDeferredOriginUrlAware {
    override suspend fun getUrl(bot: Bot): String {
        TODO()
    }

    override val imageId: String = externalImage.calculateResourceId()
}

@Suppress("EXPOSED_SUPER_INTERFACE")
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
 * 通过 [Group.uploadImage] 上传得到的 [GroupImage]. 它的链接需要查询 [IMirai.queryImageUrl]
 *
 * @param imageId 参考 [Image.imageId]
 */
@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@Serializable
internal data class OfflineGroupImage(
    override val imageId: String
) : GroupImage(), OfflineImage, DeferredOriginUrlAware {
    override fun getUrl(bot: Bot): String {
        return "http://gchat.qpic.cn/gchatpic_new/${bot.id}/0-0-${
            imageId.substring(1..36)
                .replace("-", "")
        }/0?term=2"
    }

    init {
        @Suppress("DEPRECATION")
        require(imageId matches GROUP_IMAGE_ID_REGEX) {
            "Illegal imageId. It must matches GROUP_IMAGE_ID_REGEX"
        }
    }
}

/**
 * 接收消息时获取到的 [GroupImage]. 它可以直接获取下载链接 [originUrl]
 */
@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
internal abstract class OnlineGroupImage : GroupImage(), OnlineImage

/**
 * 通过 [Group.uploadImage] 上传得到的 [GroupImage]. 它的链接需要查询 [IMirai.queryImageUrl]
 *
 * @param imageId 参考 [Image.imageId]
 */
@Serializable
internal data class OfflineFriendImage(
    override val imageId: String
) : FriendImage(), OfflineImage, DeferredOriginUrlAware {
    override fun getUrl(bot: Bot): String {
        return "http://c2cpicdw.qpic.cn/offpic_new/${bot.id}/${this.imageId}/0?term=2"
    }

    init {
        require(imageId matches FRIEND_IMAGE_ID_REGEX_1 || imageId matches FRIEND_IMAGE_ID_REGEX_2) {
            "Illegal imageId. It must matches either FRIEND_IMAGE_ID_REGEX_1 or FRIEND_IMAGE_ID_REGEX_2"
        }
    }
}

/**
 * 接收消息时获取到的 [FriendImage]. 它可以直接获取下载链接 [originUrl]
 */
internal abstract class OnlineFriendImage : FriendImage(), OnlineImage

// endregion
