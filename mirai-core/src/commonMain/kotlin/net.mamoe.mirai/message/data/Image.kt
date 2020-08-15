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

@file:Suppress(
    "EXPERIMENTAL_API_USAGE",
    "unused",
    "WRONG_MODIFIER_CONTAINING_DECLARATION",
    "DEPRECATION",
    "UnusedImport",
    "EXPOSED_SUPER_CLASS",
    "DEPRECATION_ERROR"
)

package net.mamoe.mirai.message.data

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.code.CodableMessage
import net.mamoe.mirai.utils.ExternalImage
import net.mamoe.mirai.utils.sendImage
import kotlin.js.JsName
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic

/**
 * 自定义表情 (收藏的表情) 和普通图片.
 *
 *
 * 最推荐的存储方式是存储图片原文件, 每次发送图片时都使用文件上传.
 * 在上传时服务器会根据其缓存情况回复已有的图片 ID 或要求客户端上传. 详见 [Contact.uploadImage]
 *
 *
 * ### [toString] 和 [contentToString]
 * - [toString] 固定返回 `[mirai:image:<ID>]` 格式字符串, 其中 `<ID>` 代表 [imageId].
 * - [contentToString] 固定返回 "\[图片]"
 *
 * ### 上传和发送图片
 * @see Contact.uploadImage 上传 [图片文件][ExternalImage] 并得到 [Image] 消息
 * @see Contact.sendImage 上传 [图片文件][ExternalImage] 并发送返回的 [Image] 作为一条消息
 * @see Image.sendTo 上传图片并得到 [Image] 消息
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
public expect interface Image : Message, MessageContent, CodableMessage {
    public companion object Key : Message.Key<Image> {
        public override val typeName: String
    }

    /**
     * 图片的 id.
     *
     * 图片 id 不一定会长时间保存, 也可能在将来改变格式, 因此不建议使用 id 发送图片.
     *
     * ### 格式
     * 群图片:
     * - [GROUP_IMAGE_ID_REGEX], 示例: `{01E9451B-70ED-EAE3-B37C-101F1EEBF5B5}.ext` (ext系扩展名)
     *
     * 好友图片:
     * - [FRIEND_IMAGE_ID_REGEX_1], 示例: `/f8f1ab55-bf8e-4236-b55e-955848d7069f`
     * - [FRIEND_IMAGE_ID_REGEX_2], 示例: `/000000000-3814297509-BFB7027B9354B8F899A062061D74E206`
     *
     * @see Image 使用 id 构造图片
     * @see md5 得到图片文件 MD5
     */
    public val imageId: String

    /* 实现:
    final override fun toString(): String = _stringValue!!

    final override fun contentToString(): String = "[图片]"
    */

    @Deprecated(
        """
        不要自行实现 Image, 它必须由协议模块实现, 否则会无法发送也无法解析.
    """, level = DeprecationLevel.HIDDEN
    )
    @Suppress("PropertyName")
    @get:JvmSynthetic
    internal val DoNotImplementThisClass: Nothing?
}

/**
 * 计算图片的 md5 校验值.
 *
 * 在 Java 使用: `MessageUtils.calculateImageMd5(image)`
 */
@get:JvmName("calculateImageMd5")
public val Image.md5: ByteArray
    get() = calculateImageMd5ByImageId(imageId)


/**
 * 好友图片
 *
 * [imageId] 形如 `/f8f1ab55-bf8e-4236-b55e-955848d7069f` (37 长度)  或 `/000000000-3814297509-BFB7027B9354B8F899A062061D74E206` (54 长度)
 */
// NotOnlineImage
public abstract class FriendImage internal constructor() : AbstractImage() { // change to sealed in the future.
    public companion object Key : Message.Key<FriendImage> {
        public override val typeName: String get() = "FriendImage"
    }
}

/**
 * 群图片.
 *
 * @property imageId 形如 `{01E9451B-70ED-EAE3-B37C-101F1EEBF5B5}.ext` (ext系扩展名)
 * @see Image 查看更多说明
 */
// CustomFace
public abstract class GroupImage internal constructor() : AbstractImage() { // change to sealed in the future.
    public companion object Key : Message.Key<GroupImage> {
        public override val typeName: String get() = "GroupImage"
    }
}

/**
 * 好友图片 ID 正则表达式
 *
 * `/f8f1ab55-bf8e-4236-b55e-955848d7069f`
 * @see FRIEND_IMAGE_ID_REGEX_2
 */
// Java: MessageUtils.FRIEND_IMAGE_ID_REGEX_1
public val FRIEND_IMAGE_ID_REGEX_1: Regex = Regex("""/[0-9a-fA-F]{8}-([0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}""")

/**
 * 好友图片 ID 正则表达式 2
 *
 * `/000000000-3814297509-BFB7027B9354B8F899A062061D74E206`
 * @see FRIEND_IMAGE_ID_REGEX_1
 */
// Java: MessageUtils.FRIEND_IMAGE_ID_REGEX_2
public val FRIEND_IMAGE_ID_REGEX_2: Regex = Regex("""/[0-9]*-[0-9]*-[0-9a-fA-F]{32}""")

/**
 * 群图片 ID 正则表达式
 *
 * `{01E9451B-70ED-EAE3-B37C-101F1EEBF5B5}.ext`
 */
@Suppress("RegExpRedundantEscape")
// This is required on Android
// Java: MessageUtils.GROUP_IMAGE_ID_REGEX
public val GROUP_IMAGE_ID_REGEX: Regex = Regex("""\{[0-9a-fA-F]{8}-([0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}\}\..{3,5}""")

/**
 * 通过 [Image.imageId] 构造一个 [Image] 以便发送.
 * 这个图片必须是服务器已经存在的图片.
 * 图片 id 不一定会长时间保存, 因此不建议使用 id 发送图片.
 *
 * 请查看 `ExternalImageJvm` 获取更多创建 [Image] 的方法
 *
 * @see Image 获取更多说明
 * @see Image.imageId 获取更多说明
 */
@Suppress("FunctionName", "DEPRECATION")
@JsName("newImage")
@JvmName("newImage")
public fun Image(imageId: String): OfflineImage = when {
    imageId matches FRIEND_IMAGE_ID_REGEX_1 -> OfflineFriendImage(imageId)
    imageId matches FRIEND_IMAGE_ID_REGEX_2 -> OfflineFriendImage(imageId)
    imageId matches GROUP_IMAGE_ID_REGEX -> OfflineGroupImage(imageId)
    else -> throw IllegalArgumentException("Illegal imageId: $imageId. $ILLEGAL_IMAGE_ID_EXCEPTION_MESSAGE")
}

/**
 * 查询原图下载链接.
 *
 * - 当图片为从服务器接收的消息中的图片时, 可以直接获取下载链接, 本函数不会挂起协程.
 * - 其他情况下协程可能会挂起并向服务器查询下载链接, 或不挂起并拼接一个链接.
 *
 * @return 原图 HTTP 下载链接 (非 HTTPS)
 * @throws IllegalStateException 当无任何 [Bot] 在线时抛出 (因为无法获取相关协议)
 */
@JvmSynthetic
public suspend fun Image.queryUrl(): String {
    @Suppress("DEPRECATION")
    return when (this) {
        is ConstOriginUrlAware -> this.originUrl
        is DeferredOriginUrlAware -> this.getUrl(firstOnlineBotInstance)
        is SuspendDeferredOriginUrlAware -> this.getUrl(firstOnlineBotInstance)
        else -> error("Internal error: unsupported Image class: ${this::class}")
    }
}