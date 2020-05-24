/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.message

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.core.Input
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.utils.OverFileSizeMaxException
import net.mamoe.mirai.utils.sendTo
import net.mamoe.mirai.utils.toExternalImage
import net.mamoe.mirai.utils.upload
import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStream
import java.net.URL

/*
 * 发送图片的一些扩展函数.
 */

// region IMAGE.sendAsImageTo(Contact)

/**
 * 在 [Dispatchers.IO] 中将图片发送到指定联系人. 不会创建临时文件
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
suspend fun <C : Contact> BufferedImage.sendTo(contact: C): MessageReceipt<C> =
    toExternalImage().sendTo(contact)

/**
 * 在 [Dispatchers.IO] 中下载 [URL] 到临时文件并将其作为图片发送到指定联系人
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
suspend fun <C : Contact> URL.sendAsImageTo(contact: C): MessageReceipt<C> =
    toExternalImage().sendTo(contact)

/**
 * 在 [Dispatchers.IO] 中读取 [Input] 到临时文件并将其作为图片发送到指定联系人
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
suspend fun <C : Contact> Input.sendAsImageTo(contact: C): MessageReceipt<C> =
    toExternalImage().sendTo(contact)

/**
 * 在 [Dispatchers.IO] 中读取 [InputStream] 到临时文件并将其作为图片发送到指定联系人
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
suspend fun <C : Contact> InputStream.sendAsImageTo(contact: C): MessageReceipt<C> =
    toExternalImage().sendTo(contact)

/**
 * 在 [Dispatchers.IO] 中将文件作为图片发送到指定联系人
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
suspend fun <C : Contact> File.sendAsImageTo(contact: C): MessageReceipt<C> {
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
suspend fun BufferedImage.upload(contact: Contact): Image =
    toExternalImage().upload(contact)

/**
 * 在 [Dispatchers.IO] 中下载 [URL] 到临时文件并将其作为图片上传后构造 [Image]
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
suspend fun URL.uploadAsImage(contact: Contact): Image =
    toExternalImage().upload(contact)

/**
 * 在 [Dispatchers.IO] 中读取 [Input] 到临时文件并将其作为图片上传后构造 [Image]
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
suspend fun Input.uploadAsImage(contact: Contact): Image =
    toExternalImage().upload(contact)

/**
 * 在 [Dispatchers.IO] 中读取 [InputStream] 到临时文件并将其作为图片上传后构造 [Image]
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
suspend fun InputStream.uploadAsImage(contact: Contact): Image =
    toExternalImage().upload(contact)

/**
 * 在 [Dispatchers.IO] 中将文件作为图片上传后构造 [Image]
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
suspend fun File.uploadAsImage(contact: Contact): Image {
    require(this.isFile && this.exists() && this.canRead()) { "file ${this.path} is not readable" }
    return toExternalImage().upload(contact)
}

// endregion

// region Contact.sendImage(IMAGE)

/**
 * 在 [Dispatchers.IO] 中将图片发送到指定联系人. 不会保存临时文件
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
suspend inline fun <C : Contact> C.sendImage(bufferedImage: BufferedImage): MessageReceipt<C> =
    bufferedImage.sendTo(this)

/**
 * 在 [Dispatchers.IO] 中下载 [URL] 到临时文件并将其作为图片发送到指定联系人
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
suspend inline fun <C : Contact> C.sendImage(imageUrl: URL): MessageReceipt<C> = imageUrl.sendAsImageTo(this)

/**
 * 在 [Dispatchers.IO] 中读取 [Input] 到临时文件并将其作为图片发送到指定联系人
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
suspend inline fun <C : Contact> C.sendImage(imageInput: Input): MessageReceipt<C> = imageInput.sendAsImageTo(this)

/**
 * 在 [Dispatchers.IO] 中读取 [InputStream] 到临时文件并将其作为图片发送到指定联系人
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
suspend inline fun <C : Contact> C.sendImage(imageStream: InputStream): MessageReceipt<C> =
    imageStream.sendAsImageTo(this)

/**
 * 在 [Dispatchers.IO] 中将文件作为图片发送到指定联系人
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
suspend inline fun <C : Contact> C.sendImage(file: File): MessageReceipt<C> = file.sendAsImageTo(this)

// endregion

// region Contact.uploadImage(IMAGE)

/**
 * 在 [Dispatchers.IO] 中将图片上传, 但不发送. 不会保存临时文件
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
suspend inline fun Contact.uploadImage(bufferedImage: BufferedImage): Image = bufferedImage.upload(this)

/**
 * 在 [Dispatchers.IO] 中下载 [URL] 到临时文件并将其作为图片上传, 但不发送
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
suspend inline fun Contact.uploadImage(imageUrl: URL): Image = imageUrl.uploadAsImage(this)

/**
 * 在 [Dispatchers.IO] 中读取 [Input] 到临时文件并将其作为图片上传, 但不发送
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
suspend inline fun Contact.uploadImage(imageInput: Input): Image = imageInput.uploadAsImage(this)

/**
 * 在 [Dispatchers.IO] 中读取 [InputStream] 到临时文件并将其作为图片上传, 但不发送
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
suspend inline fun Contact.uploadImage(imageStream: InputStream): Image = imageStream.uploadAsImage(this)

/**
 * 在 [Dispatchers.IO] 中将文件作为图片上传, 但不发送
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
suspend inline fun Contact.uploadImage(file: File): Image = file.uploadAsImage(this)

// endregion