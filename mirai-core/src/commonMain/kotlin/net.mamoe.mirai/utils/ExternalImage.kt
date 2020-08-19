/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE", "unused")

package net.mamoe.mirai.utils

import kotlinx.io.core.readBytes
import kotlinx.io.core.use
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.sendTo
import net.mamoe.mirai.message.data.toUHexString
import net.mamoe.mirai.utils.internal.DeferredReusableInput
import net.mamoe.mirai.utils.internal.ReusableInput
import java.io.File
import kotlin.jvm.JvmSynthetic

/**
 * mirai 将在未来重构 [ExternalImage] 相关 API, 请尽量避免使用他们.
 *
 * 可以直接通过 [File.uploadAsImageTo] 等 API 替代.
 */
@RequiresOptIn(
    "mirai 将在 2.0.0 时重构 ExternalImage 相关 API, 请尽量避免使用他们. 可以直接通过 File.uploadAsImageTo() 等 API 替代.",
    level = RequiresOptIn.Level.WARNING
)
@Retention(AnnotationRetention.BINARY)
@UnstableExternalImage
@SinceMirai("1.2.0")
public annotation class UnstableExternalImage

/**
 * 外部图片. 图片数据还没有读取到内存.
 *
 * 在 JVM, 请查看 'ExternalImageJvm.kt'
 *
 * @see ExternalImage.sendTo 上传图片并以纯图片消息发送给联系人
 * @See ExternalImage.upload 上传图片并得到 [Image] 消息
 */
@UnstableExternalImage
public class ExternalImage internal constructor(
    internal val input: ReusableInput
) {
    internal val md5: ByteArray get() = input.md5
    public val formatName: String by lazy {
        val hex = input.asInput().use {
            it.readBytes(8).toUHexString("")
        }
        return@lazy hex.detectFormatName()
    }

    init {
        if (input !is DeferredReusableInput) {
            require(input.size < 30L * 1024 * 1024) { "Image file is too big. Maximum is 30 MiB, but recommended to be 20 MiB" }
        }
    }

    public companion object {
        public const val defaultFormatName: String = "mirai"


        @MiraiExperimentalAPI
        public fun generateUUID(md5: ByteArray): String {
            return "${md5[0, 3]}-${md5[4, 5]}-${md5[6, 7]}-${md5[8, 9]}-${md5[10, 15]}"
        }

        @MiraiExperimentalAPI
        public fun generateImageId(md5: ByteArray): String {
            return """{${generateUUID(md5)}}.$defaultFormatName"""
        }
    }

    public override fun toString(): String {
        if (input is DeferredReusableInput) {
            if (!input.initialized) {
                return "ExternalImage(uninitialized)"
            }
        }
        return "ExternalImage(${generateUUID(md5)})"
    }

    internal fun calculateImageResourceId(): String = generateImageId(md5)

    private fun String.detectFormatName(): String = when {
        startsWith("FFD8") -> "jpg"
        startsWith("89504E47") -> "png"
        startsWith("47494638") -> "gif"
        startsWith("424D") -> "bmp"
        else -> defaultFormatName
    }
}

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

/**
 * 将图片作为单独的消息发送给指定联系人.
 *
 * @see Contact.uploadImage 上传图片
 * @see Contact.sendMessage 最终调用, 发送消息.
 */
@JvmSynthetic
public suspend fun <C : Contact> ExternalImage.sendTo(contact: C): MessageReceipt<C> = when (contact) {
    is Group -> contact.uploadImage(this).sendTo(contact)
    is User -> contact.uploadImage(this).sendTo(contact)
    else -> error("unreachable")
}

/**
 * 上传图片并构造 [Image].
 * 这个函数可能需消耗一段时间.
 *
 * @param contact 图片上传对象. 由于好友图片与群图片不通用, 上传时必须提供目标联系人
 *
 * @see Contact.uploadImage 最终调用, 上传图片.
 */
@JvmSynthetic
public suspend fun ExternalImage.upload(contact: Contact): Image = when (contact) {
    is Group -> contact.uploadImage(this)
    is User -> contact.uploadImage(this)
    else -> error("unreachable")
}

/**
 * 将图片作为单独的消息发送给 [this]
 *
 * @see Contact.sendMessage 最终调用, 发送消息.
 */
@JvmSynthetic
public suspend inline fun <C : Contact> C.sendImage(image: ExternalImage): MessageReceipt<C> = image.sendTo(this)


@JvmSynthetic
internal operator fun ByteArray.get(rangeStart: Int, rangeEnd: Int): String = buildString {
    for (it in rangeStart..rangeEnd) {
        append(this@get[it].fixToString())
    }
}

private fun Byte.fixToString(): String {
    return when (val b = this.toInt() and 0xff) {
        in 0..15 -> "0${this.toString(16).toUpperCase()}"
        else -> b.toString(16).toUpperCase()
    }
}