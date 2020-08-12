@file:Suppress("DEPRECATION_ERROR", "UnusedImport")

package net.mamoe.mirai.qqandroid.message

import net.mamoe.mirai.message.data.FriendFlashImage
import net.mamoe.mirai.message.data.GroupFlashImage
import net.mamoe.mirai.message.data.md5
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.HummerCommelem
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.qqandroid.utils.io.serialization.toByteArray


internal fun GroupFlashImage.toJceData() = ImMsgBody.Elem(
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

internal fun FriendFlashImage.toJceData() = ImMsgBody.Elem(
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