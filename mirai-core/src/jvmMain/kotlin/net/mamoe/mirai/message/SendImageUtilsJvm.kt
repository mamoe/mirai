/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

/**
 * 发送图片的一些扩展函数.
 */

@file:Suppress("unused")
@file:JvmMultifileClass
@file:JvmName("SendImageUtilsJvmKt")

package net.mamoe.mirai.message

import kotlinx.coroutines.Dispatchers
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Voice
import net.mamoe.mirai.utils.*
import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStream

// region IMAGE.sendAsImageTo(Contact)

/**
 * 在 [Dispatchers.IO] 中将图片发送到指定联系人. 不会创建临时文件
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
public suspend fun <C : Contact> BufferedImage.sendTo(contact: C): MessageReceipt<C> =
    toExternalImage().sendTo(contact)

/**
 * 在 [Dispatchers.IO] 中读取 [InputStream] 到临时文件并将其作为图片发送到指定联系人
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
public suspend fun <C : Contact> InputStream.sendAsImageTo(contact: C): MessageReceipt<C> =
    toExternalImage().sendTo(contact)

/**
 * 在 [Dispatchers.IO] 中将文件作为图片发送到指定联系人
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
public suspend fun <C : Contact> File.sendAsImageTo(contact: C): MessageReceipt<C> {
    require(this.exists() && this.canRead())
    return toExternalImage().sendTo(contact)
}

// endregion

// region IMAGE.Upload(Contact): Image

/**
 * 在 [Dispatchers.IO] 中将图片上传后构造 [Image]. 不会创建临时文件
 * @throws OverFileSizeMaxException
 */
@JvmSynthetic
@Throws(OverFileSizeMaxException::class)
public suspend fun BufferedImage.upload(contact: Contact): Image =
    toExternalImage().upload(contact)

/**
 * 在 [Dispatchers.IO] 中读取 [InputStream] 到临时文件并将其作为图片上传后构造 [Image]
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
public suspend fun InputStream.uploadAsImage(contact: Contact): Image =
    toExternalImage().upload(contact)

/**
 * 在 [Dispatchers.IO] 中将文件作为图片上传后构造 [Image]
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
public suspend fun File.uploadAsImage(contact: Contact): Image {
    require(this.isFile && this.exists() && this.canRead()) { "file ${this.path} is not readable" }
    return toExternalImage().upload(contact)
}

/**
 * 在 [Dispatchers.IO] 中将文件作为语音上传后构造 [Voice]
 *
 * - 请手动关闭输入流
 * - 请使用 amr 或 silk 格式
 *
 * @suppress 注意，这只是个实验性功能且随时可能会删除
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
@MiraiExperimentalAPI("语音支持处于实验性阶段")
@SinceMirai("1.2.0")
public suspend fun InputStream.uploadAsGroupVoice(group: Group): Voice {
    return group.uploadVoice(this)
}

// endregion

// region Contact.sendImage(IMAGE)

/**
 * 在 [Dispatchers.IO] 中将图片发送到指定联系人. 不会保存临时文件
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
public suspend inline fun <C : Contact> C.sendImage(bufferedImage: BufferedImage): MessageReceipt<C> =
    bufferedImage.sendTo(this)

/**
 * 在 [Dispatchers.IO] 中读取 [InputStream] 到临时文件并将其作为图片发送到指定联系人
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
public suspend inline fun <C : Contact> C.sendImage(imageStream: InputStream): MessageReceipt<C> =
    imageStream.sendAsImageTo(this)

/**
 * 在 [Dispatchers.IO] 中将文件作为图片发送到指定联系人
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
public suspend inline fun <C : Contact> C.sendImage(file: File): MessageReceipt<C> = file.sendAsImageTo(this)

// endregion

// region Contact.uploadImage(IMAGE)

/**
 * 在 [Dispatchers.IO] 中将图片上传, 但不发送. 不会保存临时文件
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
public suspend inline fun Contact.uploadImage(bufferedImage: BufferedImage): Image = bufferedImage.upload(this)

/**
 * 在 [Dispatchers.IO] 中读取 [InputStream] 到临时文件并将其作为图片上传, 但不发送
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
public suspend inline fun Contact.uploadImage(imageStream: InputStream): Image = imageStream.uploadAsImage(this)

/**
 * 在 [Dispatchers.IO] 中将文件作为图片上传, 但不发送
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
public suspend inline fun Contact.uploadImage(file: File): Image = file.uploadAsImage(this)

// endregion