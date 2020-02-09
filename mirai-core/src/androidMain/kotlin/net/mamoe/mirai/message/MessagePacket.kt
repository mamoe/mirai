/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.message

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.utils.MiraiInternalAPI

/**
 * 平台相关扩展
 */
@UseExperimental(MiraiInternalAPI::class)
actual abstract class MessagePacket<TSender : QQ, TSubject : Contact> actual constructor(bot: Bot) : MessagePacketBase<TSender, TSubject>(bot) {
    //   suspend inline fun uploadImage(image: Bitmap): Image = subject.uploadImage(image)
    //suspend inline fun uploadImage(image: URL): Image = subject.uploadImage(image)
    //suspend inline fun uploadImage(image: Input): Image = subject.uploadImage(image)
    //suspend inline fun uploadImage(image: InputStream): Image = subject.uploadImage(image)
    //suspend inline fun uploadImage(image: File): Image = subject.uploadImage(image)

    //// suspend inline fun sendImage(image: Bitmap) = subject.sendImage(image)
    //suspend inline fun sendImage(image: URL) = subject.sendImage(image)
    //suspend inline fun sendImage(image: Input) = subject.sendImage(image)
    //suspend inline fun sendImage(image: InputStream) = subject.sendImage(image)
    //suspend inline fun sendImage(image: File) = subject.sendImage(image)

    ////  suspend inline fun Bitmap.upload(): Image = upload(subject)
    //suspend inline fun URL.uploadAsImage(): Image = uploadAsImage(subject)
    //suspend inline fun Input.uploadAsImage(): Image = uploadAsImage(subject)
    //suspend inline fun InputStream.uploadAsImage(): Image = uploadAsImage(subject)
    //suspend inline fun File.uploadAsImage(): Image = uploadAsImage(subject)

    ////  suspend inline fun Bitmap.send() = sendTo(subject)
    //suspend inline fun URL.sendAsImage() = sendAsImageTo(subject)
    //suspend inline fun Input.sendAsImage() = sendAsImageTo(subject)
    //suspend inline fun InputStream.sendAsImage() = sendAsImageTo(subject)
    //suspend inline fun File.sendAsImage() = sendAsImageTo(subject)
}