/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

/**
 * 为 Kotlin 使用者实现的发送图片的一些扩展函数.
 */

@file:Suppress("unused")
@file:JvmMultifileClass
@file:JvmName("SendResourceUtilsJvmKt ")

package net.mamoe.mirai.utils

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Voice
import net.mamoe.mirai.utils.ExternalResource.Companion.sendAsImageTo
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import java.io.File
import java.io.InputStream

// region IMAGE.sendAsImageTo(Contact)

/**
 * 读取 [InputStream] 到临时文件并将其作为图片发送到指定联系人
 *
 * 注意：本函数不会关闭流
 *
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
@JvmSynthetic
public suspend inline fun <C : Contact> InputStream.sendAsImageTo(contact: C): MessageReceipt<C> =
    runBIO {
        @Suppress("BlockingMethodInNonBlockingContext")
        toExternalResource("png")
    }.withUse { sendAsImageTo(contact) }

/**
 * 将文件作为图片发送到指定联系人
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
@JvmSynthetic
public suspend inline fun <C : Contact> File.sendAsImageTo(contact: C): MessageReceipt<C> {
    require(this.exists() && this.canRead())
    return toExternalResource("png").withUse { sendAsImageTo(contact) }
}

// endregion

// region IMAGE.Upload(Contact): Image

/**
 * 读取 [InputStream] 到临时文件并将其作为图片上传后构造 [Image]
 *
 * 注意：本函数不会关闭流
 *
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
@JvmSynthetic
public suspend inline fun InputStream.uploadAsImage(contact: Contact): Image =
    @Suppress("BlockingMethodInNonBlockingContext")
    runBIO { toExternalResource("png") }.withUse { uploadAsImage(contact) }

/**
 * 将文件作为图片上传后构造 [Image]
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
@JvmSynthetic
public suspend inline fun File.uploadAsImage(contact: Contact): Image {
    require(this.isFile && this.exists() && this.canRead()) { "file ${this.path} is not readable" }
    return toExternalResource("png").withUse { uploadAsImage(contact) }
}

/**
 * 将文件作为语音上传后构造 [Voice]
 *
 * - 请手动关闭输入流
 * - 请使用 amr 或 silk 格式
 *
 * @suppress 注意，这只是个实验性功能且随时可能会删除
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
@MiraiExperimentalApi("语音支持处于实验性阶段")
public suspend inline fun InputStream.uploadAsGroupVoice(group: Group): Voice {
    return group.uploadVoice(this)
}

// endregion

// region Contact.uploadImage(IMAGE)

/**
 * 读取 [InputStream] 到临时文件并将其作为图片上传, 但不发送
 *
 * 注意：本函数不会关闭流
 *
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
@JvmSynthetic
public suspend inline fun Contact.uploadImage(imageStream: InputStream): Image =
    imageStream.uploadAsImage(this@uploadImage)

/**
 * 将文件作为图片上传, 但不发送
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
@JvmSynthetic
public suspend inline fun Contact.uploadImage(file: File): Image = file.uploadAsImage(this)

// endregion