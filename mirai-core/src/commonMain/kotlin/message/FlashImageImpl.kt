/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("DEPRECATION_ERROR", "UnusedImport")

package net.mamoe.mirai.internal.message

import net.mamoe.mirai.internal.network.protocol.data.proto.HummerCommelem
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray
import net.mamoe.mirai.message.data.FlashImage
import net.mamoe.mirai.message.data.isFriendImage
import net.mamoe.mirai.message.data.isGroupImage
import net.mamoe.mirai.message.data.md5


internal fun FlashImage.toJceData(): ImMsgBody.Elem {
    return when {
        image.isFriendImage -> ImMsgBody.Elem(
            commonElem = ImMsgBody.CommonElem(
                serviceType = 3,
                businessType = 0,
                pbElem = HummerCommelem.MsgElemInfoServtype3(
                    flashC2cPic = ImMsgBody.NotOnlineImage(
                        filePath = image.imageId,
                        resId = image.imageId,
                        picMd5 = image.md5,
                        oldPicMd5 = false,
                        pbReserve = byteArrayOf(0x78, 0x06)
                    )
                ).toByteArray(HummerCommelem.MsgElemInfoServtype3.serializer())
            )
        )

        image.isGroupImage -> ImMsgBody.Elem(
            commonElem = ImMsgBody.CommonElem(
                serviceType = 3,
                businessType = 0,
                pbElem = HummerCommelem.MsgElemInfoServtype3(
                    flashTroopPic = ImMsgBody.CustomFace(
                        filePath = image.imageId,
                        md5 = image.md5,
                        pbReserve = byteArrayOf(0x78, 0x06)
                    )
                ).toByteArray(HummerCommelem.MsgElemInfoServtype3.serializer())
            )
        )

        else -> error("Internal error: an image is neither group image nor friend image.")
    }
}