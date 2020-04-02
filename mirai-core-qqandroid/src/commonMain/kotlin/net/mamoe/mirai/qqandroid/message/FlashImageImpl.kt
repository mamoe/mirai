package net.mamoe.mirai.qqandroid.message

import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.HummerCommelem
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.qqandroid.utils.io.serialization.toByteArray

fun FlashImage(image: Image) = when (image) {
    is GroupImage -> GroupFlashImageImpl(image)
    is FriendImage -> FriendFlashImageImpl(image)
    else -> throw IllegalArgumentException("不支持的图片类型(Please use GroupImage or FriendImage)")
}

fun Image.flash() = FlashImage(this)

internal class GroupFlashImageImpl(
    override val image: GroupImage
) : AbstractGroupFlashImage() {

    private var stringValue: String? = null
        get() {
            return field ?: kotlin.run {
                field = "[mirai:flash:${image.imageId}]"
                field
            }
        }

    override fun toString(): String = stringValue!!

    override val length: Int get() = stringValue!!.length

    override fun get(index: Int) = stringValue!![index]

    override fun subSequence(startIndex: Int, endIndex: Int) = stringValue!!.subSequence(startIndex, endIndex)

    override fun compareTo(other: String) = other.compareTo(stringValue!!)
}


internal class FriendFlashImageImpl(
    override val image: FriendImage
) : AbstractFriendFlashImage() {

    private val stringValue = "flash"

    override fun toString() = stringValue

    override val length = stringValue.length

    override fun get(index: Int) = stringValue.get(index)

    override fun subSequence(startIndex: Int, endIndex: Int) = stringValue.subSequence(startIndex, endIndex)

    override fun compareTo(other: String) = other.compareTo(stringValue)
}

internal fun GroupFlashImageImpl.toJceData() = ImMsgBody.Elem(
    commonElem = ImMsgBody.CommonElem(
        serviceType = 3,
        businessType = 0,
        pbElem = HummerCommelem.MsgElemInfoServtype3(
            flashTroopPic = ImMsgBody.CustomFace(
                filePath = image.filepath,
                md5 = image.md5,
                pbReserve = byteArrayOf(0x78, 0x06)
            )
        ).toByteArray(HummerCommelem.MsgElemInfoServtype3.serializer())
    )
)

internal fun FriendFlashImageImpl.toJceData() = ImMsgBody.Elem(
    commonElem = ImMsgBody.CommonElem(
        serviceType = 3,
        businessType = 0,
        pbElem = HummerCommelem.MsgElemInfoServtype3(
            flashC2cPic = ImMsgBody.NotOnlineImage(
                filePath = image.filepath,
                fileId = image.fileId,
                resId = image.resourceId,
                picMd5 = image.md5,
                oldPicMd5 = false,
                pbReserve = byteArrayOf(0x78, 0x06)
            )
        ).toByteArray(HummerCommelem.MsgElemInfoServtype3.serializer())
    )
)