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

import kotlinx.coroutines.io.ByteReadChannel
import kotlinx.io.InputStream
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.Input
import kotlinx.serialization.InternalSerializationApi
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.OfflineImage
import net.mamoe.mirai.message.data.sendTo
import kotlin.jvm.JvmSynthetic

/**
 * 外部图片. 图片数据还没有读取到内存.
 *
 * 在 JVM, 请查看 'ExternalImageJvm.kt'
 *
 * @see ExternalImage.sendTo 上传图片并以纯图片消息发送给联系人
 * @See ExternalImage.upload 上传图片并得到 [Image] 消息
 */
class ExternalImage private constructor(
    val width: Int,
    val height: Int,
    val md5: ByteArray,
    imageFormat: String,
    val input: Any, // Input from kotlinx.io, InputStream from kotlinx.io MPP, ByteReadChannel from ktor
    val inputSize: Long, // dont be greater than Int.MAX
    val filename: String
) {
    constructor(
        width: Int,
        height: Int,
        md5: ByteArray,
        imageFormat: String,
        input: ByteReadChannel,
        inputSize: Long, // dont be greater than Int.MAX
        filename: String
    ) : this(width, height, md5, imageFormat, input as Any, inputSize, filename)

    constructor(
        width: Int,
        height: Int,
        md5: ByteArray,
        imageFormat: String,
        input: Input,
        inputSize: Long, // dont be greater than Int.MAX
        filename: String
    ) : this(width, height, md5, imageFormat, input as Any, inputSize, filename)

    constructor(
        width: Int,
        height: Int,
        md5: ByteArray,
        imageFormat: String,
        input: ByteReadPacket,
        filename: String
    ) : this(width, height, md5, imageFormat, input as Any, input.remaining, filename)

    @OptIn(InternalSerializationApi::class)
    constructor(
        width: Int,
        height: Int,
        md5: ByteArray,
        imageFormat: String,
        input: InputStream,
        filename: String
    ) : this(width, height, md5, imageFormat, input as Any, input.available().toLong(), filename)

    init {
        require(inputSize < 30L * 1024 * 1024) { "file is too big. Maximum is about 20MB" }
    }

    companion object {
        fun generateUUID(md5: ByteArray): String {
            return "${md5[0..3]}-${md5[4..5]}-${md5[6..7]}-${md5[8..9]}-${md5[10..15]}"
        }

        fun generateImageId(md5: ByteArray, imageType: Int): String {
            return """{${generateUUID(md5)}}.${determineFormat(imageType)}"""
        }

        fun determineImageType(format: String): Int {
            return when (format) {
                "jpg" -> 1000
                "png" -> 1001
                "webp" -> 1002
                "bmp" -> 1005
                "gig" -> 2000
                "apng" -> 2001
                "sharpp" -> 1004
                else -> 1000 // unsupported, just make it jpg
            }
        }

        fun determineFormat(imageType: Int): String {
            return when (imageType) {
                1000 -> "jpg"
                1001 -> "png"
                1002 -> "webp"
                1005 -> "bmp"
                2000 -> "gig"
                2001 -> "apng"
                1004 -> "sharpp"
                else -> "jpg" // unsupported, just make it jpg
            }
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
        get() = determineImageType(format)

    override fun toString(): String = "[ExternalImage(${width}x$height $format)]"

    fun calculateImageResourceId(): String {
        return "{${generateUUID(md5)}}.$format"
    }
}

/**
 * 将图片发送给指定联系人
 */
@JvmSynthetic
suspend fun <C : Contact> ExternalImage.sendTo(contact: C): MessageReceipt<C> = when (contact) {
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
@JvmSynthetic
suspend fun ExternalImage.upload(contact: Contact): OfflineImage = when (contact) {
    is Group -> contact.uploadImage(this)
    is QQ -> contact.uploadImage(this)
    else -> error("unreachable")
}

/**
 * 将图片发送给 [this]
 */
@JvmSynthetic
suspend inline fun <C : Contact> C.sendImage(image: ExternalImage): MessageReceipt<C> = image.sendTo(this)

internal operator fun ByteArray.get(range: IntRange): String = buildString {
    range.forEach {
        append(this@get[it].fixToString())
    }
}

private fun Byte.fixToString(): String {
    return when (val b = this.toInt() and 0xff) {
        in 0..15 -> "0${this.toString(16).toUpperCase()}"
        else -> b.toString(16).toUpperCase()
    }
}