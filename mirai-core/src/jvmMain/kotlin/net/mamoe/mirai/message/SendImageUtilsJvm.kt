@file:Suppress("unused")

package net.mamoe.mirai.message

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.core.Input
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.network.protocol.tim.packet.action.OverFileSizeMaxException
import net.mamoe.mirai.utils.sendTo
import net.mamoe.mirai.utils.toExternalImage
import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStream
import java.net.URL

/*
 * 发送图片的一些扩展函数.
 */

// region Type extensions

/**
 * 将图片发送到指定联系人. 不会保存临时文件
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
suspend fun BufferedImage.sendAsImageTo(contact: Contact) = withContext(Dispatchers.IO) { toExternalImage() }.sendTo(contact)

/**
 * 下载 [URL] 到临时文件并将其作为图片发送到指定联系人
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
suspend fun URL.sendAsImageTo(contact: Contact) = withContext(Dispatchers.IO) { toExternalImage() }.sendTo(contact)

/**
 * 读取 [Input] 到临时文件并将其作为图片发送到指定联系人
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
suspend fun Input.sendAsImageTo(contact: Contact) = withContext(Dispatchers.IO) { toExternalImage() }.sendTo(contact)

/**
 * 读取 [InputStream] 到临时文件并将其作为图片发送到指定联系人
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
suspend fun InputStream.sendAsImageTo(contact: Contact) = withContext(Dispatchers.IO) { toExternalImage() }.sendTo(contact)

/**
 * 将文件作为图片发送到指定联系人
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
suspend fun File.sendAsImageTo(contact: Contact) = withContext(Dispatchers.IO) { toExternalImage() }.sendTo(contact)

// endregion


// region Contact extensions

/**
 * 将图片发送到指定联系人. 不会保存临时文件
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
suspend fun Contact.sendImage(bufferedImage: BufferedImage) = bufferedImage.sendAsImageTo(this)

/**
 * 下载 [URL] 到临时文件并将其作为图片发送到指定联系人
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
suspend fun Contact.sendImage(imageUrl: URL) = imageUrl.sendAsImageTo(this)

/**
 * 读取 [Input] 到临时文件并将其作为图片发送到指定联系人
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
suspend fun Contact.sendImage(imageInput: Input) = imageInput.sendAsImageTo(this)

/**
 * 读取 [InputStream] 到临时文件并将其作为图片发送到指定联系人
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
suspend fun Contact.sendImage(imageStream: InputStream) = imageStream.sendAsImageTo(this)

/**
 * 将文件作为图片发送到指定联系人
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
suspend fun Contact.sendImage(file: File) = file.sendAsImageTo(this)

// endregion