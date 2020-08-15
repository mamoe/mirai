/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("DEPRECATION_ERROR")

package net.mamoe.mirai.qqandroid.message

import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.qqandroid.utils.hexToBytes
import net.mamoe.mirai.utils.ExternalImage

internal class OnlineGroupImageImpl(
    internal val delegate: ImMsgBody.CustomFace
) : @Suppress("DEPRECATION")
OnlineGroupImage() {
    override val imageId: String = ExternalImage.generateImageId(delegate.md5)
    override val originUrl: String
        get() = if (delegate.origUrl.isBlank()) {
            "http://gchat.qpic.cn/gchatpic_new/0/0-0-${imageId.substring(1..36)
                .replace("-", "")}/0?term=2"
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