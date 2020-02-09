/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.utils

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.Input
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.sendTo
import net.mamoe.mirai.utils.io.toUHexString

/**
 * 外部图片. 图片数据还没有读取到内存.
 *
 * 在 JVM, 请查看 'ExternalImageJvm.kt'
 *
 * @see ExternalImage.sendTo 上传图片并以纯图片消息发送给联系人
 * @See ExternalImage.upload 上传图片并得到 [Image] 消息
 */
class ExternalImage(
    val width: Int,
    val height: Int,
    val md5: ByteArray,
    imageFormat: String,
    val input: Input,
    val inputSize: Long, // dont be greater than Int.MAX
    val filename: String
) {
    init {
        check(inputSize in 0L..Int.MAX_VALUE.toLong()) { "file is too big" }
    }

    companion object {
        operator fun invoke(
            width: Int,
            height: Int,
            md5: ByteArray,
            format: String,
            data: ByteReadPacket,
            filename: String
        ): ExternalImage = ExternalImage(width, height, md5, format, data, data.remaining, filename)

        fun generateUUID(md5: ByteArray): String{
            return "${md5[0..3]}-${md5[4..5]}-${md5[6..7]}-${md5[8..9]}-${md5[10..15]}"
        }
    }

    val format: String =
        when (val it = imageFormat.toLowerCase()) {
            "jpeg" -> "jpg" //必须转换
            else -> it
        }

    /**
     * ImgType:
     *  JPG:    1000
     *  PNG:    1001
     *  WEBP:   1002
     *  BMP:    1005
     *  GIG:    2000
     *  APNG:   2001
     *  SHARPP: 1004
     */
    val imageType: Int
        get() = when (format) {
            "jpg" -> 1000
            "png" -> 1001
            "webp" -> 1002
            "bmp" -> 1005
            "gig" -> 2000
            "apng" -> 2001
            "sharpp" -> 1004
            else -> 1000 // unsupported, just make it jpg
        }

    override fun toString(): String = "[ExternalImage(${width}x$height $format)]"

    fun calculateImageResourceId(): String {
        return "{${generateUUID(md5)}}.$format"
    }
}

/**
 * 将图片发送给指定联系人
 */
suspend fun ExternalImage.sendTo(contact: Contact) = when (contact) {
    is Group -> contact.uploadImage(this).sendTo(contact)
    is QQ -> contact.uploadImage(this).sendTo(contact)
    else -> error("unreachable")
}

/**
 * 上传图片并通过图片 ID 构造 [Image]
 * 这个函数可能需消耗一段时间
 *
 * @see contact 图片上传对象. 由于好友图片与群图片不通用, 上传时必须提供目标联系人
 */
suspend fun ExternalImage.upload(contact: Contact): Image = when (contact) {
    is Group -> contact.uploadImage(this)
    is QQ -> contact.uploadImage(this)
    else -> error("unreachable")
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