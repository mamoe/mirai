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
import kotlinx.serialization.Transient
import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotImpl
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.utils.MiraiInternalAPI
import kotlin.js.JsName
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic

/**
 * 自定义表情 (收藏的表情), 图片
 *
 * @see FlashImage 闪照
 * @see Image.flash 转换普通图片为闪照
 */
interface Image : Message, MessageContent {
    companion object Key : Message.Key<Image>

    /**
     * 图片的 id.
     * 图片 id 不一定会长时间保存, 因此不建议使用 id 发送图片.
     *
     * 示例:
     * 好友图片的 id: `/f8f1ab55-bf8e-4236-b55e-955848d7069f` 或 `/000000000-3814297509-BFB7027B9354B8F899A062061D74E206`
     * 群图片的 id: `{01E9451B-70ED-EAE3-B37C-101F1EEBF5B5}.png`
     *
     * @see Image 使用 id 构造图片
     */
    val imageId: String
}

/**
 * 通过 [Image.imageId] 构造一个 [Image] 以便发送.
 * 这个图片必须是服务器已经存在的图片.
 * 图片 id 不一定会长时间保存, 因此不建议使用 id 发送图片.
 *
 * 请查看 `ExternalImageJvm` 获取更多创建 [Image] 的方法
 */
@Suppress("FunctionName")
@JsName("newImage")
@JvmName("newImage")
fun Image(imageId: String): Image = when {
    imageId.startsWith('/') -> OfflineFriendImage(imageId) // /f8f1ab55-bf8e-4236-b55e-955848d7069f
    imageId.length == 42 || imageId.startsWith('{') && imageId.endsWith('}') -> OfflineGroupImage(imageId) // {01E9451B-70ED-EAE3-B37C-101F1EEBF5B5}.png
    else -> throw IllegalArgumentException("Bad imageId, expecting length=37 or 42, got ${imageId.length}")
}

@MiraiInternalAPI("使用 Image")
sealed class AbstractImage : Image {

    private var _stringValue: String? = null
        get() {
            return field ?: kotlin.run {
                field = "[mirai:image:$imageId]"
                field
            }
        }
    override val length: Int get() = _stringValue!!.length
    override fun get(index: Int): Char = _stringValue!![index]
    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence =
        _stringValue!!.subSequence(startIndex, endIndex)

    override fun compareTo(other: String): Int = _stringValue!!.compareTo(other)
    final override fun toString(): String = _stringValue!!
}

// region 在线图片

/**
 * 在服务器上的图片. 它可以直接获取下载链接.
 *
 * 一般由 [Contact.uploadImage] 得到
 */
interface OnlineImage : Image {
    companion object Key : Message.Key<OnlineImage>

    /**
     * 原图下载链接. 包含域名
     */
    val originUrl: String
}

/**
 * 查询原图下载链接.
 */
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
    companion object Key : Message.Key<OfflineImage>
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
 * [imageId] 形如 `{01E9451B-70ED-EAE3-B37C-101F1EEBF5B5}.png` (42 长度)
 */
// CustomFace
@OptIn(MiraiInternalAPI::class)
sealed class GroupImage : AbstractImage() {
    companion object Key : Message.Key<GroupImage>

    abstract val filepath: String
    abstract val fileId: Int
    abstract val serverIp: Int
    abstract val serverPort: Int
    abstract val fileType: Int
    abstract val signature: ByteArray
    abstract val useful: Int
    abstract val md5: ByteArray
    abstract val bizType: Int
    abstract val imageType: Int
    abstract val width: Int
    abstract val height: Int
    abstract val source: Int
    abstract val size: Int
    abstract val pbReserve: ByteArray
    abstract val original: Int
}

/**
 * 通过 [Group.uploadImage] 上传得到的 [GroupImage]. 它的链接需要查询 [Bot.queryImageUrl]
 */
@Serializable
data class OfflineGroupImage(
    override val filepath: String, // {01E9451B-70ED-EAE3-B37C-101F1EEBF5B5}.png
    override val md5: ByteArray
) : GroupImage(), OfflineImage {
    constructor(imageId: String) : this(filepath = imageId, md5 = calculateImageMd5ByImageId(imageId))

    override val fileId: Int get() = 0
    override val serverIp: Int get() = 0
    override val serverPort: Int get() = 0
    override val fileType: Int get() = 0 // 0
    override val signature: ByteArray get() = EMPTY_BYTE_ARRAY
    override val useful: Int get() = 1
    override val bizType: Int get() = 0
    override val imageType: Int get() = 0
    override val width: Int get() = 0
    override val height: Int get() = 0
    override val source: Int get() = 200
    override val size: Int get() = 0
    override val original: Int get() = 1
    override val pbReserve: ByteArray get() = EMPTY_BYTE_ARRAY
    override val imageId: String get() = filepath

    override fun hashCode(): Int {
        return filepath.hashCode() + 31 * md5.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is OfflineGroupImage && other.md5.contentEquals(this.md5) && other.filepath == this.filepath
    }
}

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
@OptIn(MiraiInternalAPI::class)
sealed class FriendImage : AbstractImage() {
    companion object Key : Message.Key<FriendImage>

    abstract val resourceId: String
    abstract val md5: ByteArray
    abstract val filepath: String
    abstract val fileLength: Int
    abstract val height: Int
    abstract val width: Int
    open val bizType: Int get() = 0
    open val imageType: Int get() = 1000
    abstract val fileId: Int
    open val downloadPath: String get() = resourceId
    open val original: Int get() = 1

    override val imageId: String get() = resourceId
}

/**
 * 通过 [Group.uploadImage] 上传得到的 [GroupImage]. 它的链接需要查询 [Bot.queryImageUrl]
 */
data class OfflineFriendImage(
    override val resourceId: String,
    override val md5: ByteArray,
    @Transient override val filepath: String = resourceId,
    @Transient override val fileLength: Int = 0,
    @Transient override val height: Int = 0,
    @Transient override val width: Int = 0,
    @Transient override val bizType: Int = 0,
    @Transient override val imageType: Int = 1000,
    @Transient override val downloadPath: String = resourceId,
    @Transient override val fileId: Int = 0
) : FriendImage(), OfflineImage {
    constructor(imageId: String) : this(resourceId = imageId, md5 = calculateImageMd5ByImageId(imageId))

    override fun hashCode(): Int {
        return resourceId.hashCode() + 31 * md5.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is OfflineFriendImage && other.md5
            .contentEquals(this.md5) && other.resourceId == this.resourceId
    }
}

/**
 * 接收消息时获取到的 [FriendImage]. 它可以直接获取下载链接 [originUrl]
 */
abstract class OnlineFriendImage : FriendImage(), OnlineImage


// endregion


// region internal

private val EMPTY_BYTE_ARRAY = ByteArray(0)


// /000000000-3814297509-BFB7027B9354B8F899A062061D74E206
private val FRIEND_IMAGE_ID_REGEX_1 = Regex("""/[0-9]*-[0-9]*-[0-9a-zA-Z]{32}""")

// /f8f1ab55-bf8e-4236-b55e-955848d7069f
private val FRIEND_IMAGE_ID_REGEX_2 = Regex("""/.{8}-(.{4}-){3}.{12}""")

// {01E9451B-70ED-EAE3-B37C-101F1EEBF5B5}.png
private val GROUP_IMAGE_ID_REGEX = Regex("""\{.{8}-(.{4}-){3}.{12}}\..*""")

@Suppress("NOTHING_TO_INLINE") // no stack waste
internal inline fun Char.hexDigitToByte(): Int {
    return when (this) {
        in '0'..'9' -> this - '0'
        in 'A'..'F' -> 10 + (this - 'A')
        in 'a'..'f' -> 10 + (this - 'a')
        else -> throw IllegalArgumentException("illegal hex digit: $this")
    }
}

internal fun String.skipToSecondHyphen(): Int {
    var count = 0
    this.forEachIndexed { index, c ->
        if (c == '-' && ++count == 2) return index
    }
    error("cannot find two hyphens")
}

internal fun String.imageIdToMd5(offset: Int): ByteArray {
    val result = ByteArray(16)
    var cur = 0
    var hasCurrent = false
    var lastChar: Char = 0.toChar()
    for (index in offset..this.lastIndex) {
        val char = this[index]
        if (char == '-') continue
        if (hasCurrent) {
            result[cur++] = (lastChar.hexDigitToByte().shl(4) or char.hexDigitToByte()).toByte()
            if (cur == 16) return result
            hasCurrent = false
        } else {
            lastChar = char
            hasCurrent = true
        }
    }
    error("No enough chars")
}

@OptIn(ExperimentalStdlibApi::class)
internal fun calculateImageMd5ByImageId(imageId: String): ByteArray {
    return when {
        imageId.matches(FRIEND_IMAGE_ID_REGEX_1) -> imageId.imageIdToMd5(imageId.skipToSecondHyphen() + 1)
        imageId.matches(FRIEND_IMAGE_ID_REGEX_2) ->
            imageId.imageIdToMd5(1)
        imageId.matches(GROUP_IMAGE_ID_REGEX) -> {
            imageId.imageIdToMd5(1)
        }
        else -> error(
            "illegal imageId: $imageId. " +
                    "ImageId must matches Regex `${FRIEND_IMAGE_ID_REGEX_1.pattern}`, " +
                    "`${FRIEND_IMAGE_ID_REGEX_2.pattern}` or " +
                    "`${GROUP_IMAGE_ID_REGEX.pattern}`"
        )
    }
}


// endregion