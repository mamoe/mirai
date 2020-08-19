/*
 *
 *  * Copyright 2020 Mamoe Technologies and contributors.
 *  *
 *  * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *  *
 *  * https://github.com/mamoe/mirai/blob/master/LICENSE
 *
 */

@file:Suppress("DEPRECATION", "DEPRECATION_ERROR")
@file:JvmMultifileClass
@file:JvmName("SendImageUtilsJvmKt")

package net.mamoe.mirai.message

import kotlinx.coroutines.Dispatchers
import kotlinx.io.core.Input
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.utils.OverFileSizeMaxException
import net.mamoe.mirai.utils.sendTo
import net.mamoe.mirai.utils.toExternalImage
import net.mamoe.mirai.utils.upload
import java.net.URL


/**
 * 在 [Dispatchers.IO] 中下载 [URL] 到临时文件并将其作为图片发送到指定联系人
 * @throws OverFileSizeMaxException
 */
@Deprecated(
    "请自行通过 URL.openConnection 得到 InputStream 后调用其扩展",
    replaceWith = ReplaceWith("this.openConnection().sendAsImageTo(contact)"),
    level = DeprecationLevel.WARNING
)
@Throws(OverFileSizeMaxException::class)
public suspend fun <C : Contact> URL.sendAsImageTo(contact: C): MessageReceipt<C> =
    toExternalImage().sendTo(contact)

/**
 * 在 [Dispatchers.IO] 中读取 [Input] 到临时文件并将其作为图片发送到指定联系人
 * @throws OverFileSizeMaxException
 */
@Deprecated(
    "已弃用对 kotlinx.io 的支持",
    level = DeprecationLevel.ERROR
)
@Suppress("DEPRECATION_ERROR")
@Throws(OverFileSizeMaxException::class)
public suspend fun <C : Contact> Input.sendAsImageTo(contact: C): MessageReceipt<C> =
    toExternalImage().sendTo(contact)



/**
 * 在 [Dispatchers.IO] 中下载 [URL] 到临时文件并将其作为图片上传后构造 [Image]
 * @throws OverFileSizeMaxException
 */
@Deprecated(
    "请自行通过 URL.openConnection 得到 InputStream 后调用其扩展",
    replaceWith = ReplaceWith("this.openConnection().sendAsImageTo(contact)"),
    level = DeprecationLevel.WARNING
)
@Throws(OverFileSizeMaxException::class)
public suspend fun URL.uploadAsImage(contact: Contact): Image =
    toExternalImage().upload(contact)


/**
 * 在 [Dispatchers.IO] 中下载 [URL] 到临时文件并将其作为图片上传, 但不发送
 * @throws OverFileSizeMaxException
 */
@Deprecated(
    "请自行通过 URL.openConnection 得到 InputStream 后调用其扩展",
    replaceWith = ReplaceWith("this.openConnection().sendAsImageTo(contact)"),
    level = DeprecationLevel.WARNING
)
@Throws(OverFileSizeMaxException::class)
public suspend inline fun Contact.uploadImage(imageUrl: URL): Image = imageUrl.uploadAsImage(this)

/**
 * 在 [Dispatchers.IO] 中读取 [Input] 到临时文件并将其作为图片上传, 但不发送
 * @throws OverFileSizeMaxException
 */
@Deprecated(
    "已弃用对 kotlinx.io 的支持",
    level = DeprecationLevel.ERROR
)
@Throws(OverFileSizeMaxException::class)
public suspend inline fun Contact.uploadImage(imageInput: Input): Image = imageInput.uploadAsImage(this)


/**
 * 在 [Dispatchers.IO] 中下载 [URL] 到临时文件并将其作为图片发送到指定联系人
 * @throws OverFileSizeMaxException
 */
@Deprecated(
    "请自行通过 URL.openConnection 得到 InputStream 后调用其扩展",
    replaceWith = ReplaceWith("this.openConnection().sendAsImageTo(contact)"),
    level = DeprecationLevel.WARNING
)
@Throws(OverFileSizeMaxException::class)
public suspend inline fun <C : Contact> C.sendImage(imageUrl: URL): MessageReceipt<C> = imageUrl.sendAsImageTo(this)

/**
 * 在 [Dispatchers.IO] 中读取 [Input] 到临时文件并将其作为图片发送到指定联系人
 * @throws OverFileSizeMaxException
 */

@Deprecated(
    "已弃用对 kotlinx.io 的支持",
    level = DeprecationLevel.ERROR
)
@Throws(OverFileSizeMaxException::class)
public suspend inline fun <C : Contact> C.sendImage(imageInput: Input): MessageReceipt<C> =
    imageInput.sendAsImageTo(this)

/**
 * 在 [Dispatchers.IO] 中读取 [Input] 到临时文件并将其作为图片上传后构造 [Image]
 * @throws OverFileSizeMaxException
 */
@Deprecated(
    "已弃用对 kotlinx.io 的支持",
    level = DeprecationLevel.ERROR
)
@Throws(OverFileSizeMaxException::class)
public suspend fun Input.uploadAsImage(contact: Contact): Image =
    toExternalImage().upload(contact)
