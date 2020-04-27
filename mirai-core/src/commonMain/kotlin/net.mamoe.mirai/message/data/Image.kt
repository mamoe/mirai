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

@file:Suppress("EXPERIMENTAL_API_USAGE", "unused")

package net.mamoe.mirai.message.data

import kotlinx.serialization.Serializable
import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotImpl
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.PlannedRemoval
import net.mamoe.mirai.utils.SinceMirai
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
 * - [contentToString] 固定返回 ```"[图片]"```
 *
 * ### 上传和发送图片
 * @see Contact.uploadImage 上传 [图片文件][ExternalImage] 并得到 [Image] 消息
 * @see Contact.sendImage 上传 [图片文件][ExternalImage] 并发送返回的 [Image] 作为一条消息
 * @see Image.sendTo 上传图片并得到 [Image] 消息
 *
 * 查看平台 `actual` 定义以获取上传方式扩展.
 *
 * @see FlashImage 闪照
 * @see Image.flash 转换普通图片为闪照
 */
expect interface Image : Message, MessageContent {
    companion object Key : Message.Key<Image> {
        override val typeName: String
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
    val imageId: String

    /* 实现:
    final override fun toString(): String = _stringValue!!

    final override fun contentToString(): String = "[图片]"
    */

    @Deprecated("""
        不要自行实现 OnlineGroupImage, 它必须由协议模块实现, 否则会无法发送也无法解析.
    """, level = DeprecationLevel.HIDDEN)
    @Suppress("PropertyName", "DeprecatedCallableAddReplaceWith")
    @get:JvmSynthetic
    val DoNotImplementThisClass: Nothing?
}

/**
 * 好友图片 ID 正则表达式
 *
 * `/f8f1ab55-bf8e-4236-b55e-955848d7069f`
 * @see FRIEND_IMAGE_ID_REGEX_2
 */
@SinceMirai("0.39.2")
// Java: MessageUtils.FRIEND_IMAGE_ID_REGEX_1
val FRIEND_IMAGE_ID_REGEX_1 = Regex("""/.{8}-(.{4}-){3}.{12}""")

/**
 * 好友图片 ID 正则表达式 2
 *
 * `/000000000-3814297509-BFB7027B9354B8F899A062061D74E206`
 * @see FRIEND_IMAGE_ID_REGEX_1
 */
@SinceMirai("0.39.2")
// Java: MessageUtils.FRIEND_IMAGE_ID_REGEX_2
val FRIEND_IMAGE_ID_REGEX_2 = Regex("""/[0-9]*-[0-9]*-[0-9a-zA-Z]{32}""")

/**
 * 群图片 ID 正则表达式
 *
 * `{01E9451B-70ED-EAE3-B37C-101F1EEBF5B5}.mirai`
 */
@SinceMirai("0.39.2")
// Java: MessageUtils.GROUP_IMAGE_ID_REGEX
val GROUP_IMAGE_ID_REGEX = Regex("""\{.{8}-(.{4}-){3}.{12}\}\.mirai""")

/**
 * 在 `0.39.0` 前的图片的正则表示
 */
@Deprecated("Only for temporal use",
    replaceWith = ReplaceWith("GROUP_IMAGE_ID_REGEX", "net.mamoe.mirai.message.data.GROUP_IMAGE_ID_REGEX"))
@SinceMirai("0.39.2")
@PlannedRemoval("1.0.0")
// Java: MessageUtils.GROUP_IMAGE_ID_REGEX_OLD
val GROUP_IMAGE_ID_REGEX_OLD = Regex("""\{.{8}-(.{4}-){3}.{12}\}\..*""")

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
fun Image(imageId: String): OfflineImage = when {
    imageId matches FRIEND_IMAGE_ID_REGEX_1 -> OfflineFriendImage(imageId)
    imageId matches FRIEND_IMAGE_ID_REGEX_2 -> OfflineFriendImage(imageId)
    imageId matches GROUP_IMAGE_ID_REGEX -> OfflineGroupImage(imageId)
    imageId matches GROUP_IMAGE_ID_REGEX_OLD -> OfflineGroupImage(imageId)
    else -> throw IllegalArgumentException("illegal imageId: $imageId. $ILLEGAL_IMAGE_ID_EXCEPTION_MESSAGE")
}

// region 在线图片

/**
 * 在服务器上的图片. 它可以直接获取下载链接.
 *
 * 一般由 [Contact.uploadImage] 得到
 */
interface OnlineImage : Image {
    companion object Key : Message.Key<OnlineImage> {
        override val typeName: String get() = "OnlineImage"
    }

    /**
     * 原图下载链接. 包含域名
     */
    val originUrl: String
}

/**
 * 查询原图下载链接.
 */
@JvmSynthetic
suspend fun Image.queryUrl(): String {
    @OptIn(MiraiInternalAPI::class)
    return when (this) {
        is OnlineImage -> this.originUrl
        else -> BotImpl.instances.peekFirst().get()?.queryImageUrl(this)
            ?: error("No Bot available to query image url")
    }
}

// endregion 在线图片


// region 离线图片

/**
 * 离线的图片, 即为客户端主动上传到服务器而获得的 [Image] 实例.
 * 不能直接获取它在服务器上的链接. 需要通过 [Bot.queryImageUrl] 查询
 *
 * 一般由 [Contact.uploadImage] 得到
 */
interface OfflineImage : Image {
    companion object Key : Message.Key<OfflineImage> {
        override val typeName: String get() = "OfflineImage"
    }
}

/**
 * 原图下载链接. 包含域名
 */
@JvmSynthetic
suspend fun OfflineImage.queryUrl(): String {
    @OptIn(MiraiInternalAPI::class)
    return BotImpl.instances.peekFirst().get()?.queryImageUrl(this) ?: error("No Bot available to query image url")
}

// endregion 离线图片

// region 群图片


/**
 * 群图片.
 *
 * [imageId] 形如 `{01E9451B-70ED-EAE3-B37C-101F1EEBF5B5}.mirai` (45 长度)
 */
@Suppress("DEPRECATION_ERROR")
// CustomFace
@OptIn(MiraiInternalAPI::class)
sealed class GroupImage : AbstractImage() {
    companion object Key : Message.Key<GroupImage> {
        override val typeName: String get() = "GroupImage"
    }
}

/**
 * 通过 [Group.uploadImage] 上传得到的 [GroupImage]. 它的链接需要查询 [Bot.queryImageUrl]
 *
 * @param imageId 参考 [Image.imageId]
 */
@Serializable
data class OfflineGroupImage(
    override val imageId: String
) : GroupImage(), OfflineImage {
    init {
        @Suppress("DEPRECATION")
        require(imageId matches GROUP_IMAGE_ID_REGEX || imageId matches GROUP_IMAGE_ID_REGEX_OLD) {
            "Illegal imageId. It must matches GROUP_IMAGE_ID_REGEX"
        }
    }
}

@get:JvmName("calculateImageMd5")
@SinceMirai("0.39.0")
val Image.md5: ByteArray
    get() = calculateImageMd5ByImageId(imageId)

/**
 * 接收消息时获取到的 [GroupImage]. 它可以直接获取下载链接 [originUrl]
 */
abstract class OnlineGroupImage : GroupImage(), OnlineImage


// endregion 群图片

// region 好友图片


/**
 * 好友图片
 *
 * [imageId] 形如 `/f8f1ab55-bf8e-4236-b55e-955848d7069f` (37 长度)  或 `/000000000-3814297509-BFB7027B9354B8F899A062061D74E206` (54 长度)
 */ // NotOnlineImage
@Suppress("DEPRECATION_ERROR")
@OptIn(MiraiInternalAPI::class)
sealed class FriendImage : AbstractImage() {
    companion object Key : Message.Key<FriendImage> {
        override val typeName: String get() = "FriendImage"
    }
}

/**
 * 通过 [Group.uploadImage] 上传得到的 [GroupImage]. 它的链接需要查询 [Bot.queryImageUrl]
 *
 * @param imageId 参考 [Image.imageId]
 */
@Serializable
data class OfflineFriendImage(
    override val imageId: String
) : FriendImage(), OfflineImage {
    init {
        require(imageId matches FRIEND_IMAGE_ID_REGEX_1 || imageId matches FRIEND_IMAGE_ID_REGEX_2) {
            "Illegal imageId. It must matches either FRIEND_IMAGE_ID_REGEX_1 or FRIEND_IMAGE_ID_REGEX_2"
        }
    }
}

/**
 * 接收消息时获取到的 [FriendImage]. 它可以直接获取下载链接 [originUrl]
 */
abstract class OnlineFriendImage : FriendImage(), OnlineImage

// endregion


@PlannedRemoval("1.0.0")
@JvmSynthetic
@Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
@Suppress("FunctionName")
@JsName("newImage")
@JvmName("newImage")
fun Image2(imageId: String): Image = Image(imageId)


/**
 * 所有 [Image] 实现的基类.
 */
@Deprecated(
    "This is internal API. Use Image instead",
    level = DeprecationLevel.HIDDEN, // so that others can't see this class
    replaceWith = ReplaceWith("Image")
)
@MiraiInternalAPI("Use Image instead")
sealed class AbstractImage : Image {
    @Deprecated("""
        不要自行实现 OnlineGroupImage, 它必须由协议模块实现, 否则会无法发送也无法解析.
    """, level = DeprecationLevel.HIDDEN)
    @Suppress("PropertyName", "DeprecatedCallableAddReplaceWith")
    @get:JvmSynthetic
    final override val DoNotImplementThisClass: Nothing?
        get() = error("stub")

    private var _stringValue: String? = null
        get() = field ?: kotlin.run {
            field = "[mirai:image:$imageId]"
            field
        }
    override val length: Int get() = _stringValue!!.length
    override fun get(index: Int): Char = _stringValue!![index]
    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence =
        _stringValue!!.subSequence(startIndex, endIndex)

    override fun compareTo(other: String): Int = _stringValue!!.compareTo(other)
    final override fun toString(): String = _stringValue!!
    final override fun contentToString(): String = "[图片]"
}
