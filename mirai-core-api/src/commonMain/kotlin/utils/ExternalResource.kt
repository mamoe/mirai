/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE", "unused")

package net.mamoe.mirai.utils

import net.mamoe.mirai.Bot
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.sendTo
import java.awt.image.BufferedImage
import java.io.Closeable
import java.io.File
import java.io.InputStream
import java.io.RandomAccessFile


/**
 *
 */
public interface ExternalResource : Closeable {
    public val md5: ByteArray
    public val formatName: String
    public val size: Int
    public fun inputStream(): InputStream

    public companion object {
        @JvmStatic
        @JvmName("create")
        public fun File.toExternalResource(formatName: String?): ExternalResource =
            RandomAccessFile(this, "r").toExternalResource(
                formatName ?: inputStream().detectFileTypeAndClose()
            )

        @JvmStatic
        @JvmName("create")
        public fun RandomAccessFile.toExternalResource(formatName: String?): ExternalResource =
            ExternalResourceImplByFile(this, formatName)

        @JvmStatic
        @JvmName("create")
        public fun ByteArray.toExternalResource(formatName: String?): ExternalResource =
            ExternalResourceImplByByteArray(this, formatName)


        /**
         * 将 [BufferedImage] 保存为临时文件, 然后构造 [ExternalResource]
         */
        @JvmStatic
        @JvmOverloads
        @JvmName("create")
        public fun BufferedImage.toExternalResource(formatName: String = "png"): ExternalResource =
            Mirai.FileCacheStrategy.newCache(this, formatName)

        /**
         * 将 [InputStream] 委托为 [ExternalResource].
         * 只会在上传图片时才读取 [InputStream] 的内容. 具体行为取决于相关 [Bot] 的 [FileCacheStrategy]
         */
        @JvmStatic
        @JvmName("create")
        public fun InputStream.toExternalResource(formatName: String?): ExternalResource =
            Mirai.FileCacheStrategy.newCache(this, formatName)
    }
}

public fun ExternalResource.calculateResourceId(): String {
    return generateImageId(md5, formatName.ifEmpty { "mirai" })
}


private fun InputStream.detectFileTypeAndClose(): String? {
    val buffer = ByteArray(8)
    return use {
        kotlin.runCatching { it.read(buffer) }.onFailure { return null }
        getFileType(buffer)
    }
}

internal class ExternalResourceImplByFileWithMd5(
    private val file: RandomAccessFile,
    override val md5: ByteArray,
    formatName: String?
) : ExternalResource {
    override val size: Int = file.length().toInt()
    override val formatName: String by lazy {
        formatName ?: inputStream().detectFileTypeAndClose().orEmpty()
    }

    override fun inputStream(): InputStream = file.inputStream()
    override fun close() {}
}

internal class ExternalResourceImplByFile(
    private val file: RandomAccessFile,
    formatName: String?
) : ExternalResource {
    override val size: Int = file.length().toInt()
    override val md5: ByteArray by lazy { inputStream().md5() }
    override val formatName: String by lazy {
        formatName ?: inputStream().detectFileTypeAndClose().orEmpty()
    }

    override fun inputStream(): InputStream = file.inputStream()
    override fun close() {}
}

internal class ExternalResourceImplByByteArray(
    private val data: ByteArray,
    formatName: String?
) : ExternalResource {
    override val size: Int = data.size
    override val md5: ByteArray by lazy { data.md5() }
    override val formatName: String by lazy {
        formatName ?: getFileType(data.copyOf(8)).orEmpty()
    }

    override fun inputStream(): InputStream = data.inputStream()
    override fun close() {}
}

private fun RandomAccessFile.inputStream(): InputStream {
    val file = this
    return object : InputStream() {
        override fun read(): Int = file.read()
        override fun read(b: ByteArray, off: Int, len: Int): Int = file.read(b, off, len)
        // don't close file on stream.close. stream may be obtained at multiple times.
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
public suspend fun <C : Contact> ExternalResource.sendAsImageTo(contact: C): MessageReceipt<C> = when (contact) {
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
public suspend fun ExternalResource.uploadAsImage(contact: Contact): Image = when (contact) {
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
public suspend inline fun <C : Contact> C.sendImage(image: ExternalResource): MessageReceipt<C> =
    image.sendAsImageTo(this)