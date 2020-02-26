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

import kotlinx.io.core.Input
import kotlinx.io.core.use
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.copyAndClose
import net.mamoe.mirai.utils.copyTo
import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.net.URL

/**
 * 一条从服务器接收到的消息事件.
 * JVM 平台相关扩展
 */
@UseExperimental(MiraiInternalAPI::class)
actual abstract class MessagePacket<TSender : QQ, TSubject : Contact> actual constructor() : MessagePacketBase<TSender, TSubject>() {
    // region 上传图片
    suspend inline fun uploadImage(image: BufferedImage): Image = subject.uploadImage(image)

    suspend inline fun uploadImage(image: URL): Image = subject.uploadImage(image)
    suspend inline fun uploadImage(image: Input): Image = subject.uploadImage(image)
    suspend inline fun uploadImage(image: InputStream): Image = subject.uploadImage(image)
    suspend inline fun uploadImage(image: File): Image = subject.uploadImage(image)
    // endregion

    // region 发送图片
    suspend inline fun sendImage(image: BufferedImage) = subject.sendImage(image)

    suspend inline fun sendImage(image: URL) = subject.sendImage(image)
    suspend inline fun sendImage(image: Input) = subject.sendImage(image)
    suspend inline fun sendImage(image: InputStream) = subject.sendImage(image)
    suspend inline fun sendImage(image: File) = subject.sendImage(image)
    // endregion

    // region 上传图片 (扩展)
    suspend inline fun BufferedImage.upload(): Image = upload(subject)

    suspend inline fun URL.uploadAsImage(): Image = uploadAsImage(subject)
    suspend inline fun Input.uploadAsImage(): Image = uploadAsImage(subject)
    suspend inline fun InputStream.uploadAsImage(): Image = uploadAsImage(subject)
    suspend inline fun File.uploadAsImage(): Image = uploadAsImage(subject)
    // endregion 上传图片 (扩展)

    // region 发送图片 (扩展)
    suspend inline fun BufferedImage.send() = sendTo(subject)

    suspend inline fun URL.sendAsImage() = sendAsImageTo(subject)
    suspend inline fun Input.sendAsImage() = sendAsImageTo(subject)
    suspend inline fun InputStream.sendAsImage() = sendAsImageTo(subject)
    suspend inline fun File.sendAsImage() = sendAsImageTo(subject)
    // endregion 发送图片 (扩展)

    // region 下载图片 (扩展)
    suspend inline fun Image.downloadTo(file: File) = file.outputStream().use { downloadTo(it) }

    /**
     * 下载图片到 [output] 但不关闭这个 [output]
     */
    suspend inline fun Image.downloadTo(output: OutputStream) = channel().copyTo(output)

    /**
     * 下载图片到 [output] 并关闭这个 [output]
     */
    suspend inline fun Image.downloadAndClose(output: OutputStream) = channel().copyAndClose(output)

    /*
    suspend inline fun Image.downloadAsStream(): InputStream = channel().asInputStream()
    suspend inline fun Image.downloadAsExternalImage(): ExternalImage = withContext(Dispatchers.IO) { downloadAsStream().toExternalImage() }
    suspend inline fun Image.downloadAsBufferedImage(): BufferedImage = withContext(Dispatchers.IO) { ImageIO.read(downloadAsStream()) }
     */
    // endregion
}