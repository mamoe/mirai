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
suspend fun BufferedImage.sendTo(contact: Contact) = withContext(Dispatchers.IO) { toExternalImage() }.sendTo(contact)

/**
 * 在 [Dispatchers.IO] 中下载 [URL] 到临时文件并将其作为图片发送到指定联系人
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
suspend fun URL.sendAsImageTo(contact: Contact) = withContext(Dispatchers.IO) { toExternalImage() }.sendTo(contact)

/**
 * 在 [Dispatchers.IO] 中读取 [Input] 到临时文件并将其作为图片发送到指定联系人
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
suspend fun Input.sendAsImageTo(contact: Contact) = withContext(Dispatchers.IO) { toExternalImage() }.sendTo(contact)

/**
 * 在 [Dispatchers.IO] 中读取 [InputStream] 到临时文件并将其作为图片发送到指定联系人
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
suspend fun InputStream.sendAsImageTo(contact: Contact) = withContext(Dispatchers.IO) { toExternalImage() }.sendTo(contact)

/**
 * 在 [Dispatchers.IO] 中将文件作为图片发送到指定联系人
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
suspend fun File.sendAsImageTo(contact: Contact) {
    require(this.exists() && this.canRead())
    withContext(Dispatchers.IO) { toExternalImage() }.sendTo(contact)
}

// endregion

// region IMAGE.Upload(Contact): Image

/**
 * 在 [Dispatchers.IO] 中将图片上传后构造 [Image]. 不会创建临时文件
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
suspend fun BufferedImage.upload(contact: Contact): Image = withContext(Dispatchers.IO) { toExternalImage() }.upload(contact)

/**
 * 在 [Dispatchers.IO] 中下载 [URL] 到临时文件并将其作为图片上传后构造 [Image]
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
suspend fun URL.uploadAsImage(contact: Contact): Image = withContext(Dispatchers.IO) { toExternalImage() }.upload(contact)

/**
 * 在 [Dispatchers.IO] 中读取 [Input] 到临时文件并将其作为图片上传后构造 [Image]
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
suspend fun Input.uploadAsImage(contact: Contact): Image = withContext(Dispatchers.IO) { toExternalImage() }.upload(contact)

/**
 * 在 [Dispatchers.IO] 中读取 [InputStream] 到临时文件并将其作为图片上传后构造 [Image]
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
suspend fun InputStream.uploadAsImage(contact: Contact): Image = withContext(Dispatchers.IO) { toExternalImage() }.upload(contact)

/**
 * 在 [Dispatchers.IO] 中将文件作为图片上传后构造 [Image]
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
suspend fun File.uploadAsImage(contact: Contact): Image {
    require(this.exists() && this.canRead())
    return withContext(Dispatchers.IO) { toExternalImage() }.upload(contact)
}

// endregion

// region Contact.sendImage(IMAGE)

/**
 * 在 [Dispatchers.IO] 中将图片发送到指定联系人. 不会保存临时文件
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
suspend inline fun Contact.sendImage(bufferedImage: BufferedImage) = bufferedImage.sendTo(this)

/**
 * 在 [Dispatchers.IO] 中下载 [URL] 到临时文件并将其作为图片发送到指定联系人
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
suspend inline fun Contact.sendImage(imageUrl: URL) = imageUrl.sendAsImageTo(this)

/**
 * 在 [Dispatchers.IO] 中读取 [Input] 到临时文件并将其作为图片发送到指定联系人
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
suspend inline fun Contact.sendImage(imageInput: Input) = imageInput.sendAsImageTo(this)

/**
 * 在 [Dispatchers.IO] 中读取 [InputStream] 到临时文件并将其作为图片发送到指定联系人
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
suspend inline fun Contact.sendImage(imageStream: InputStream) = imageStream.sendAsImageTo(this)

/**
 * 在 [Dispatchers.IO] 中将文件作为图片发送到指定联系人
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
suspend inline fun Contact.sendImage(file: File) = file.sendAsImageTo(this)

// endregion

// region Contact.uploadImage(IMAGE)

/**
 * 在 [Dispatchers.IO] 中将图片发送到指定联系人. 不会保存临时文件
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
suspend inline fun Contact.uploadImage(bufferedImage: BufferedImage): Image = bufferedImage.upload(this)

/**
 * 在 [Dispatchers.IO] 中下载 [URL] 到临时文件并将其作为图片发送到指定联系人
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
suspend inline fun Contact.uploadImage(imageUrl: URL): Image = imageUrl.uploadAsImage(this)

/**
 * 在 [Dispatchers.IO] 中读取 [Input] 到临时文件并将其作为图片发送到指定联系人
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
suspend inline fun Contact.uploadImage(imageInput: Input): Image = imageInput.uploadAsImage(this)

/**
 * 在 [Dispatchers.IO] 中读取 [InputStream] 到临时文件并将其作为图片发送到指定联系人
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
suspend inline fun Contact.uploadImage(imageStream: InputStream): Image = imageStream.uploadAsImage(this)

/**
 * 在 [Dispatchers.IO] 中将文件作为图片发送到指定联系人
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
suspend inline fun Contact.uploadImage(file: File): Image = file.uploadAsImage(this)

// endregion