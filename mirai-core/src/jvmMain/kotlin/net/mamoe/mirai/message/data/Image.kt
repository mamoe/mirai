/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MessageUtils")

package net.mamoe.mirai.message.data

import kotlinx.io.core.Input
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.code.CodableMessage
import net.mamoe.mirai.message.sendAsImageTo
import net.mamoe.mirai.message.sendImage
import net.mamoe.mirai.message.uploadAsImage
import net.mamoe.mirai.message.uploadImage
import net.mamoe.mirai.utils.ExternalImage
import net.mamoe.mirai.utils.sendImage
import java.io.File
import java.io.InputStream
import java.net.URL

/**
 * 自定义表情 (收藏的表情) 和普通图片.
 *
 *
 * 最推荐的存储方式是存储图片原文件, 每次发送图片时都使用文件上传.
 * 在上传时服务器会根据其缓存情况回复已有的图片 ID 或要求客户端上传. 详见 [Contact.uploadImage]
 *
 *
 * ### 上传和发送图片
 * @see Contact.uploadImage 上传 [图片文件][ExternalImage] 并得到 [Image] 消息
 * @see Contact.sendImage 上传 [图片文件][ExternalImage] 并发送返回的 [Image] 作为一条消息
 * @see Image.sendTo 上传 [图片文件][ExternalImage] 并得到 [Image] 消息
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
 * ### 下载图片
 * @see Image.queryUrl 扩展函数. 查询图片下载链接
 * @see Bot.queryImageUrl 查询图片下载链接 (Java 使用)
 *
 * 查看平台 `actual` 定义以获取上传方式扩展.
 *
 * ## mirai 码支持
 * 格式: &#91;mirai:image:*[Image.imageId]*&#93;
 *
 * @see FlashImage 闪照
 * @see Image.flash 转换普通图片为闪照
 */
public actual interface Image : Message, MessageContent, CodableMessage {
    public actual companion object Key : Message.Key<Image> {
        actual override val typeName: String get() = "Image"
    }


    /**
     * 图片的 id.
     *
     * 图片 id 不一定会长时间保存, 也可能在将来改变格式, 因此不建议使用 id 发送图片.
     *
     * ### 格式
     * 群图片:
     * - [GROUP_IMAGE_ID_REGEX], 示例: `{01E9451B-70ED-EAE3-B37C-101F1EEBF5B5}.mirai` (后缀一定为 ".mirai")
     *
     * 好友图片:
     * - [FRIEND_IMAGE_ID_REGEX_1], 示例: `/f8f1ab55-bf8e-4236-b55e-955848d7069f`
     * - [FRIEND_IMAGE_ID_REGEX_2], 示例: `/000000000-3814297509-BFB7027B9354B8F899A062061D74E206`
     *
     * @see Image 使用 id 构造图片
     */
    public actual val imageId: String


    @Deprecated(
        """
        不要自行实现 OnlineGroupImage, 它必须由协议模块实现, 否则会无法发送也无法解析.
    """, level = DeprecationLevel.HIDDEN
    )
    @Suppress("WRONG_MODIFIER_CONTAINING_DECLARATION", "PropertyName", "unused")
    @get:JvmSynthetic
    internal actual val DoNotImplementThisClass: Nothing?
}