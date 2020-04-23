/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MessageUtils")

package net.mamoe.mirai.message.data

import kotlinx.io.core.Input
import net.mamoe.mirai.contact.Contact
import java.io.File
import java.io.InputStream
import java.net.URL

/**
 * 自定义表情 (收藏的表情) 和普通图片.
 *
 * ### 上传和发送图片
 * @see Contact.uploadImage 上传图片并得到 [Image] 消息
 * @see Contact.sendImage 上传并发送单个图片作为一条消息
 * @see Image.sendTo 上传图片并得到 [Image] 消息
 *
 * @see File.uploadAsImage
 * @see InputStream.uploadAsImage
 * @see Input.uploadAsImage
 * @see URL.uploadAsImage
 *
 * @see File.sendAsImageTo
 * @see InputStream.sendAsImageTo
 * @see Input.sendAsImageTo
 * @see URL.sendAsImageTo
 *
 *
 *
 * @see FlashImage 闪照
 * @see Image.flash 转换普通图片为闪照
 */
actual interface Image : Message, MessageContent {
    actual companion object Key : Message.Key<Image> {
        actual override val typeName: String get() = "Image"
    }

    /**
     * 图片的 id.
     * 图片 id 不一定会长时间保存, 因此不建议使用 id 发送图片.
     * 图片 id 主要根据图片文件 md5 计算得到.
     *
     * 示例:
     * 好友图片的 id: `/f8f1ab55-bf8e-4236-b55e-955848d7069f` 或 `/000000000-3814297509-BFB7027B9354B8F899A062061D74E206`
     * 群图片的 id: `{01E9451B-70ED-EAE3-B37C-101F1EEBF5B5}.png`
     *
     * @see Image 使用 id 构造图片
     */
    actual val imageId: String
}