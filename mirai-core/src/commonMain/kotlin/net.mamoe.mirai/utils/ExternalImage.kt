@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.utils

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.Input
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.message.Image
import net.mamoe.mirai.message.ImageId
import net.mamoe.mirai.message.image
import net.mamoe.mirai.message.sendTo
import net.mamoe.mirai.network.protocol.tim.packet.action.uploadImage
import net.mamoe.mirai.utils.io.toUHexString

@Suppress("FunctionName")
fun ExternalImage(
    width: Int,
    height: Int,
    md5: ByteArray,
    format: String,
    data: ByteReadPacket
) = ExternalImage(width, height, md5, format, data, data.remaining)

/**
 * 外部图片. 图片数据还没有读取到内存.
 * @see ExternalImage.sendTo 上传图片并以纯图片消息发送给联系人
 * @See ExternalImage.upload 上传图片并得到 [Image] 消息
 */
class ExternalImage(
    val width: Int,
    val height: Int,
    val md5: ByteArray,
    imageFormat: String,
    val input: Input,
    val inputSize: Long
) {
    private val format: String

    init {
        if (imageFormat == "JPEG" || imageFormat == "jpeg") {//必须转换
            this.format = "jpg"
        } else {
            this.format = imageFormat
        }
    }

    /**
     * 用于发送消息的 [ImageId]
     */
    val groupImageId: ImageId by lazy { ImageId("{${md5[0..3]}-${md5[4..5]}-${md5[6..7]}-${md5[8..9]}-${md5[10..15]}}.$format") }

    override fun toString(): String = "[ExternalImage(${width}x$height $format)]"
}

/**
 * 将图片发送给指定联系人
 */
suspend fun ExternalImage.sendTo(contact: Contact) = when (contact) {
    is Group -> contact.uploadImage(this).sendTo(contact)
    is QQ -> contact.uploadImage(this).sendTo(contact)
}

/**
 * 上传图片并通过图片 ID 构造 [Image]
 * 这个函数可能需消耗一段时间
 *
 * @see contact 图片上传对象. 由于好友图片与群图片不通用, 上传时必须提供目标联系人
 */
suspend fun ExternalImage.upload(contact: Contact): Image = when (contact) {
    is Group -> contact.uploadImage(this).image()
    is QQ -> contact.uploadImage(this).image()
}

/**
 * 将图片发送给 [this]
 */
suspend inline fun Contact.sendImage(image: ExternalImage) = image.sendTo(this)

private operator fun ByteArray.get(range: IntRange): String = buildString {
    range.forEach {
        append(this@get[it].toUHexString())
    }
}