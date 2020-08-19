/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused")

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
    suspend fun uploadImage(image: BufferedImage): Image = subject.uploadImage(image)

    @JvmSynthetic
    suspend fun uploadImage(image: InputStream): Image = subject.uploadImage(image)

    @JvmSynthetic
    suspend fun uploadImage(image: File): Image = subject.uploadImage(image)
    // endregion

    // region 发送图片
    @JvmSynthetic
    suspend fun sendImage(image: BufferedImage): MessageReceipt<TSubject> = subject.sendImage(image)

    @JvmSynthetic
    suspend fun sendImage(image: InputStream): MessageReceipt<TSubject> = subject.sendImage(image)

    @JvmSynthetic
    suspend fun sendImage(image: File): MessageReceipt<TSubject> = subject.sendImage(image)
    // endregion

    // region 上传图片 (扩展)
    @JvmSynthetic
    suspend fun BufferedImage.upload(): Image = upload(subject)

    @JvmSynthetic
    suspend fun InputStream.uploadAsImage(): Image = uploadAsImage(subject)

    @JvmSynthetic
    suspend fun File.uploadAsImage(): Image = uploadAsImage(subject)
    // endregion 上传图片 (扩展)

    // region 发送图片 (扩展)
    @JvmSynthetic
    suspend fun BufferedImage.send(): MessageReceipt<TSubject> = sendTo(subject)

    @JvmSynthetic
    suspend fun InputStream.sendAsImage(): MessageReceipt<TSubject> = sendAsImageTo(subject)

    @JvmSynthetic
    suspend fun File.sendAsImage(): MessageReceipt<TSubject> = sendAsImageTo(subject)
    // endregion 发送图片 (扩展)


    @Deprecated(
        "请自行通过 URL.openConnection 得到 InputStream 后调用其扩展",
        replaceWith = ReplaceWith("this.openConnection().sendAsImageTo(contact)"),
        level = DeprecationLevel.WARNING
    )
    @JvmSynthetic
    @Suppress("DEPRECATION")
    suspend fun URL.sendAsImage(): MessageReceipt<TSubject> = sendAsImageTo(subject)

    @Deprecated(
        "已弃用对 kotlinx.io 的支持",
        level = DeprecationLevel.ERROR
    )
    @Suppress("DEPRECATION_ERROR")
    @JvmSynthetic
    suspend fun Input.sendAsImage(): MessageReceipt<TSubject> = sendAsImageTo(subject)

    @Deprecated(
        "请自行通过 URL.openConnection 得到 InputStream 后调用其扩展",
        replaceWith = ReplaceWith("this.openConnection().sendAsImageTo(contact)"),
        level = DeprecationLevel.WARNING
    )
    @JvmSynthetic
    @Suppress("DEPRECATION")
    suspend fun uploadImage(image: URL): Image = subject.uploadImage(image)

    @Deprecated(
        "已弃用对 kotlinx.io 的支持",
        level = DeprecationLevel.ERROR
    )
    @Suppress("DEPRECATION_ERROR")
    @JvmSynthetic
    suspend fun uploadImage(image: Input): Image = subject.uploadImage(image)

    @Deprecated(
        "请自行通过 URL.openConnection 得到 InputStream 后调用其扩展",
        replaceWith = ReplaceWith("this.openConnection().sendAsImageTo(contact)"),
        level = DeprecationLevel.WARNING
    )
    @Suppress("DEPRECATION")
    @JvmSynthetic
    suspend fun sendImage(image: URL): MessageReceipt<TSubject> = subject.sendImage(image)

    @Deprecated(
        "已弃用对 kotlinx.io 的支持",
        level = DeprecationLevel.ERROR
    )
    @Suppress("DEPRECATION_ERROR")
    @JvmSynthetic
    suspend fun sendImage(image: Input): MessageReceipt<TSubject> = subject.sendImage(image)

    @Deprecated(
        "请自行通过 URL.openConnection 得到 InputStream 后调用其扩展",
        replaceWith = ReplaceWith("this.openConnection().sendAsImageTo(contact)"),
        level = DeprecationLevel.WARNING
    )
    @Suppress("DEPRECATION")
    @JvmSynthetic
    suspend fun URL.uploadAsImage(): Image = uploadAsImage(subject)

    @Deprecated(
        "已弃用对 kotlinx.io 的支持",
        level = DeprecationLevel.ERROR
    )
    @Suppress("DEPRECATION_ERROR")
    @JvmSynthetic
    suspend fun Input.uploadAsImage(): Image = uploadAsImage(subject)

}