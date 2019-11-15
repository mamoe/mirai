@file:Suppress("unused")

package net.mamoe.mirai.message

import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.network.protocol.tim.packet.action.OverFileSizeMaxException
import net.mamoe.mirai.utils.ExternalImage
import net.mamoe.mirai.utils.sendTo
import net.mamoe.mirai.utils.toExternalImage
import net.mamoe.mirai.utils.upload
import java.io.File
import java.io.IOException
import java.io.InputStream

/*
 * 发送图片的一些扩展函数.
 */
/**
 * 保存为临时文件然后调用 [File.toExternalImage]
 */
@Throws(IOException::class)
fun Bitmap.toExternalImage(): ExternalImage {
    val file = createTempFile().apply { deleteOnExit() }
    file.outputStream().use {
        this.compress(Bitmap.CompressFormat.PNG, 100, it)
    }
    return file.toExternalImage()
}

/**
 * 在 [IO] 中进行 [InputStream.toExternalImage]
 */
@Suppress("unused")
suspend fun InputStream.suspendToExternalImage() = withContext(IO) { toExternalImage() }


/**
 * 在 [Dispatchers.IO] 中将图片发送到指定联系人. 会创建临时文件
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
suspend fun Bitmap.sendTo(contact: Contact) = withContext(IO) { toExternalImage() }.sendTo(contact)

/**
 * 在 [Dispatchers.IO] 中将图片上传后构造 [Image]. 会创建临时文件
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
suspend fun Bitmap.upload(contact: Contact): Image = withContext(IO) { toExternalImage() }.upload(contact)

/**
 * 在 [Dispatchers.IO] 中将图片发送到指定联系人. 会保存临时文件
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
suspend inline fun Contact.sendImage(bitmap: Bitmap) = bitmap.sendTo(this)

/**
 * 在 [Dispatchers.IO] 中将图片发送到指定联系人. 会保存临时文件
 * @throws OverFileSizeMaxException
 */
@Throws(OverFileSizeMaxException::class)
suspend inline fun Contact.uploadImage(bitmap: Bitmap): Image = bitmap.upload(this)