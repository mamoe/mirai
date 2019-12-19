@file:Suppress("unused")

package net.mamoe.mirai.message

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.core.Input
import kotlinx.io.core.use
import kotlinx.io.streams.inputStream
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.utils.ExternalImage
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.toExternalImage
import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.net.URL
import javax.imageio.ImageIO

/**
 * JVM 平台相关扩展
 */
@UseExperimental(MiraiInternalAPI::class)
actual abstract class MessagePacket<TSender : QQ, TSubject : Contact> actual constructor(bot: Bot) : MessagePacketBase<TSender, TSubject>(bot) {
    suspend inline fun uploadImage(image: BufferedImage): Image = subject.uploadImage(image)
    suspend inline fun uploadImage(image: URL): Image = subject.uploadImage(image)
    suspend inline fun uploadImage(image: Input): Image = subject.uploadImage(image)
    suspend inline fun uploadImage(image: InputStream): Image = subject.uploadImage(image)
    suspend inline fun uploadImage(image: File): Image = subject.uploadImage(image)

    suspend inline fun sendImage(image: BufferedImage) = subject.sendImage(image)
    suspend inline fun sendImage(image: URL) = subject.sendImage(image)
    suspend inline fun sendImage(image: Input) = subject.sendImage(image)
    suspend inline fun sendImage(image: InputStream) = subject.sendImage(image)
    suspend inline fun sendImage(image: File) = subject.sendImage(image)

    suspend inline fun BufferedImage.upload(): Image = upload(subject)
    suspend inline fun URL.uploadAsImage(): Image = uploadAsImage(subject)
    suspend inline fun Input.uploadAsImage(): Image = uploadAsImage(subject)
    suspend inline fun InputStream.uploadAsImage(): Image = uploadAsImage(subject)
    suspend inline fun File.uploadAsImage(): Image = uploadAsImage(subject)

    suspend inline fun BufferedImage.send() = sendTo(subject)
    suspend inline fun URL.sendAsImage() = sendAsImageTo(subject)
    suspend inline fun Input.sendAsImage() = sendAsImageTo(subject)
    suspend inline fun InputStream.sendAsImage() = sendAsImageTo(subject)
    suspend inline fun File.sendAsImage() = sendAsImageTo(subject)

    suspend inline fun Image.downloadTo(file: File): Long = file.outputStream().use { downloadTo(it) }
    /**
     * 这个函数结束后不会关闭 [output]
     */
    suspend inline fun Image.downloadTo(output: OutputStream): Long =
        download().inputStream().use { input -> withContext(Dispatchers.IO) { input.copyTo(output) } }

    suspend inline fun Image.downloadAsStream(): InputStream = download().inputStream()
    suspend inline fun Image.downloadAsExternalImage(): ExternalImage = withContext(Dispatchers.IO) { download().toExternalImage() }
    suspend inline fun Image.downloadAsBufferedImage(): BufferedImage = withContext(Dispatchers.IO) { ImageIO.read(downloadAsStream()) }
}