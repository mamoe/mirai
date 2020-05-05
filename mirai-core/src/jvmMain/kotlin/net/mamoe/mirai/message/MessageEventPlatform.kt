/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused", "DECLARATION_CANT_BE_INLINED")

package net.mamoe.mirai.message

import kotlinx.io.core.Input
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageChain
import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStream
import java.net.URL

/**
 * 消息事件在 JVM 平台的扩展
 * @see MessageEventExtensions
 */
internal actual interface MessageEventPlatformExtensions<out TSender : User, out TSubject : Contact> {
    actual val subject: TSubject
    actual val sender: TSender
    actual val message: MessageChain
    actual val bot: Bot

    // region 上传图片

    @JvmSynthetic
    suspend inline fun uploadImage(image: BufferedImage): Image = subject.uploadImage(image)

    @JvmSynthetic
    suspend inline fun uploadImage(image: URL): Image = subject.uploadImage(image)

    @JvmSynthetic
    suspend inline fun uploadImage(image: Input): Image = subject.uploadImage(image)

    @JvmSynthetic
    suspend inline fun uploadImage(image: InputStream): Image = subject.uploadImage(image)

    @JvmSynthetic
    suspend inline fun uploadImage(image: File): Image = subject.uploadImage(image)
    // endregion

    // region 发送图片
    @JvmSynthetic
    suspend inline fun sendImage(image: BufferedImage): MessageReceipt<TSubject> = subject.sendImage(image)

    @JvmSynthetic
    suspend inline fun sendImage(image: URL): MessageReceipt<TSubject> = subject.sendImage(image)

    @JvmSynthetic
    suspend inline fun sendImage(image: Input): MessageReceipt<TSubject> = subject.sendImage(image)

    @JvmSynthetic
    suspend inline fun sendImage(image: InputStream): MessageReceipt<TSubject> = subject.sendImage(image)

    @JvmSynthetic
    suspend inline fun sendImage(image: File): MessageReceipt<TSubject> = subject.sendImage(image)
    // endregion

    // region 上传图片 (扩展)
    @JvmSynthetic
    suspend inline fun BufferedImage.upload(): Image = upload(subject)

    @JvmSynthetic
    suspend inline fun URL.uploadAsImage(): Image = uploadAsImage(subject)

    @JvmSynthetic
    suspend inline fun Input.uploadAsImage(): Image = uploadAsImage(subject)

    @JvmSynthetic
    suspend inline fun InputStream.uploadAsImage(): Image = uploadAsImage(subject)

    @JvmSynthetic
    suspend inline fun File.uploadAsImage(): Image = uploadAsImage(subject)
    // endregion 上传图片 (扩展)

    // region 发送图片 (扩展)
    @JvmSynthetic
    suspend inline fun BufferedImage.send(): MessageReceipt<TSubject> = sendTo(subject)

    @JvmSynthetic
    suspend inline fun URL.sendAsImage(): MessageReceipt<TSubject> = sendAsImageTo(subject)

    @JvmSynthetic
    suspend inline fun Input.sendAsImage(): MessageReceipt<TSubject> = sendAsImageTo(subject)

    @JvmSynthetic
    suspend inline fun InputStream.sendAsImage(): MessageReceipt<TSubject> = sendAsImageTo(subject)

    @JvmSynthetic
    suspend inline fun File.sendAsImage(): MessageReceipt<TSubject> = sendAsImageTo(subject)
    // endregion 发送图片 (扩展)
}