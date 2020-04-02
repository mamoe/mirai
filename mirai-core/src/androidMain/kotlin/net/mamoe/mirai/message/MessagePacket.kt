/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.message

import android.graphics.Bitmap
import kotlinx.io.core.Input
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.utils.MiraiInternalAPI
import java.io.File
import java.io.InputStream
import java.net.URL

/**
 * 平台相关扩展
 */
@Suppress("DEPRECATION")
@Deprecated(
    message = "use ContactMessage",
    replaceWith = ReplaceWith("ContactMessage", "net.mamoe.mirai.message.ContactMessage")
)
@OptIn(MiraiInternalAPI::class)
actual sealed class MessagePacket<TSender : QQ, TSubject : Contact> actual constructor() :
    MessagePacketBase<TSender, TSubject>() {
    suspend inline fun uploadImage(image: Bitmap): Image = subject.uploadImage(image)
    suspend inline fun uploadImage(image: URL): Image = subject.uploadImage(image)
    suspend inline fun uploadImage(image: Input): Image = subject.uploadImage(image)
    suspend inline fun uploadImage(image: InputStream): Image = subject.uploadImage(image)
    suspend inline fun uploadImage(image: File): Image = subject.uploadImage(image)

    suspend inline fun sendImage(image: Bitmap): MessageReceipt<TSubject> = subject.sendImage(image)
    suspend inline fun sendImage(image: URL): MessageReceipt<TSubject> = subject.sendImage(image)
    suspend inline fun sendImage(image: Input): MessageReceipt<TSubject> = subject.sendImage(image)
    suspend inline fun sendImage(image: InputStream): MessageReceipt<TSubject> = subject.sendImage(image)
    suspend inline fun sendImage(image: File): MessageReceipt<TSubject> = subject.sendImage(image)

    suspend inline fun Bitmap.upload(): Image = upload(subject)
    suspend inline fun URL.uploadAsImage(): Image = uploadAsImage(subject)
    suspend inline fun Input.uploadAsImage(): Image = uploadAsImage(subject)
    suspend inline fun InputStream.uploadAsImage(): Image = uploadAsImage(subject)
    suspend inline fun File.uploadAsImage(): Image = uploadAsImage(subject)

    suspend inline fun Bitmap.send(): MessageReceipt<TSubject> = sendTo(subject)
    suspend inline fun URL.sendAsImage(): MessageReceipt<TSubject> = sendAsImageTo(subject)
    suspend inline fun Input.sendAsImage(): MessageReceipt<TSubject> = sendAsImageTo(subject)
    suspend inline fun InputStream.sendAsImage(): MessageReceipt<TSubject> = sendAsImageTo(subject)
    suspend inline fun File.sendAsImage(): MessageReceipt<TSubject> = sendAsImageTo(subject)
}