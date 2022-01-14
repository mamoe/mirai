/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.image

import net.mamoe.mirai.contact.ContactOrBot
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.internal.network.protocol.data.proto.HummerCommelem
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray
import net.mamoe.mirai.message.data.FlashImage
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.ImageType
import net.mamoe.mirai.utils.generateImageId
import net.mamoe.mirai.utils.toUHexString


internal val Image.friendImageId: String
    get() {
        //    /1234567890-3666252994-EFF4427CE3D27DB6B1D9A8AB72E7A29C
        return "/000000000-000000000-${md5.toUHexString("")}"
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