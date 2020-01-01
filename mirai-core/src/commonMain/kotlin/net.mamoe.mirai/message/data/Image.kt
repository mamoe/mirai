@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.message.data

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.sendMessage
import net.mamoe.mirai.utils.ExternalImage


fun Image(id: String) = Image(ImageId(id))

/**
 * 图片消息. 在发送时将会区分群图片和好友图片发送.
 * 由接收消息时构建, 可直接发送
 *
 * @param id 这个图片的 [ImageId]
 */
inline class Image(inline val id: ImageId) : Message {
    override fun toString(): String = "[${id.value}]"

    companion object Key : Message.Key<Image>
}

inline val Image.idValue: String get() = id.value

inline class ImageId0x06(override inline val value: String) : ImageId {
    override fun toString(): String = "ImageId($value)"
}

/**
 * 一般是群的图片的 id.
 */
class ImageId0x03 constructor(override inline val value: String, inline val uniqueId: UInt, inline val height: Int, inline val width: Int) :
    ImageId {
    override fun toString(): String = "ImageId(value=$value, uniqueId=${uniqueId}, height=$height, width=$width)"

    val md5: ByteArray
        get() = this.value
            .substringAfter("{").substringBefore("}")
            .replace("-", "")
            .chunked(2)
            .map { (it[0] + it[1].toString()).toUByte(16).toByte() }
            .toByteArray().also { check(it.size == 16) }
}

@Suppress("FunctionName", "NOTHING_TO_INLINE")
inline fun ImageId(value: String): ImageId = ImageId0x06(value)

@Suppress("FunctionName", "NOTHING_TO_INLINE")
inline fun ImageId(value: String, uniqueId: UInt, height: Int, width: Int): ImageId =
    ImageId0x03(value, uniqueId, height, width)


/**
 * 图片的标识符. 由图片的数据产生.
 * 对于群, [value] 类似于 `{F61593B5-5B98-1798-3F47-2A91D32ED2FC}.jpg`, 由图片文件 MD5 直接产生.
 * 对于好友, [value] 类似于 `/01ee6426-5ff1-4cf0-8278-e8634d2909ef`, 由服务器返回.
 *
 * @see ExternalImage.groupImageId 群图片的 [ImageId] 获取
 * @see FriendImagePacket 好友图片的 [ImageId] 获取
 */
interface ImageId {
    val value: String
}

fun ImageId.checkLength() = check(value.length == 37 || value.length == 42) { "Illegal ImageId length" }
fun ImageId.requireLength() = require(value.length == 37 || value.length == 42) { "Illegal ImageId length" }

@Suppress("NOTHING_TO_INLINE")
inline fun ImageId.image(): Image =
    Image(this)

suspend inline fun ImageId.sendTo(contact: Contact) = contact.sendMessage(this.image())
