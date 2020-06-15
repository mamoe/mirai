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

import kotlinx.serialization.Serializable
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.utils.PlannedRemoval
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic


/////////////////////////
///// 以下 API 已弃用 /////
/////////////////////////


// region 已启用

internal const val ONLINE_OFFLINE_DEPRECATION_MESSAGE = """
自 1.0.0 起, mirai 已经能正常处理离线图片和在线图片的下载链接等功能. 
使用者无需考虑一个图片为在线图片还是离线图片, 只需使用 Image 类型.
"""


@PlannedRemoval("1.2.0") // 改为 internal
@Deprecated(
    ONLINE_OFFLINE_DEPRECATION_MESSAGE,
    level = DeprecationLevel.ERROR,
    replaceWith = ReplaceWith("Image", "net.mamoe.mirai.message.data.Image")
)
@Suppress("EXPOSED_SUPER_INTERFACE")
interface OnlineImage : Image, ConstOriginUrlAware {
    companion object Key : Message.Key<OnlineImage> {
        override val typeName: String get() = "OnlineImage"
    }

    override val originUrl: String
}

/**
 * 离线的图片, 即为客户端主动上传到服务器而获得的 [Image] 实例.
 * 不能直接获取它在服务器上的链接. 需要通过 [Bot.queryImageUrl] 查询
 *
 * 一般由 [Contact.uploadImage] 得到
 */
@PlannedRemoval("1.2.0") // 改为 internal
@Deprecated(
    ONLINE_OFFLINE_DEPRECATION_MESSAGE,
    level = DeprecationLevel.ERROR,
    replaceWith = ReplaceWith("Image", "net.mamoe.mirai.message.data.Image")
)
interface OfflineImage : Image {
    companion object Key : Message.Key<OfflineImage> {
        override val typeName: String get() = "OfflineImage"
    }
}

@PlannedRemoval("1.2.0") // 删除
@Deprecated(
    ONLINE_OFFLINE_DEPRECATION_MESSAGE,
    level = DeprecationLevel.HIDDEN
)
@JvmSynthetic
suspend fun OfflineImage.queryUrl(): String {
    return Bot._instances.peekFirst()?.get()?.queryImageUrl(this) ?: error("No Bot available to query image url")
}

/**
 * 通过 [Group.uploadImage] 上传得到的 [GroupImage]. 它的链接需要查询 [Bot.queryImageUrl]
 *
 * @param imageId 参考 [Image.imageId]
 */
@PlannedRemoval("1.2.0") // 改为 internal
@Deprecated(
    ONLINE_OFFLINE_DEPRECATION_MESSAGE,
    level = DeprecationLevel.ERROR,
    replaceWith = ReplaceWith("Image", "net.mamoe.mirai.message.data.Image")
)
@Serializable
data class OfflineGroupImage(
    override val imageId: String
) : GroupImage(), OfflineImage, DeferredOriginUrlAware {
    override fun getUrl(bot: Bot): String {
        return "http://gchat.qpic.cn/gchatpic_new/${bot.id}/0-0-${imageId.substring(1..36)
            .replace("-", "")}/0?term=2"
    }

    init {
        @Suppress("DEPRECATION")
        require(imageId matches GROUP_IMAGE_ID_REGEX) {
            "Illegal imageId. It must matches GROUP_IMAGE_ID_REGEX"
        }
    }
}

/**
 * 接收消息时获取到的 [GroupImage]. 它可以直接获取下载链接 [originUrl]
 */
@PlannedRemoval("1.2.0") // 改为 internal
@Deprecated(
    ONLINE_OFFLINE_DEPRECATION_MESSAGE,
    level = DeprecationLevel.ERROR,
    replaceWith = ReplaceWith("Image", "net.mamoe.mirai.message.data.Image")
)
abstract class OnlineGroupImage : GroupImage(), OnlineImage

/**
 * 通过 [Group.uploadImage] 上传得到的 [GroupImage]. 它的链接需要查询 [Bot.queryImageUrl]
 *
 * @param imageId 参考 [Image.imageId]
 */
@PlannedRemoval("1.2.0") // 改为 internal
@Deprecated(
    ONLINE_OFFLINE_DEPRECATION_MESSAGE,
    level = DeprecationLevel.ERROR,
    replaceWith = ReplaceWith("Image", "net.mamoe.mirai.message.data.Image")
)
@Serializable
data class OfflineFriendImage(
    override val imageId: String
) : FriendImage(), OfflineImage, DeferredOriginUrlAware {
    override fun getUrl(bot: Bot): String {
        return "http://c2cpicdw.qpic.cn/offpic_new/${bot.id}/${this.imageId}/0?term=2"
    }

    init {
        require(imageId matches FRIEND_IMAGE_ID_REGEX_1 || imageId matches FRIEND_IMAGE_ID_REGEX_2) {
            "Illegal imageId. It must matches either FRIEND_IMAGE_ID_REGEX_1 or FRIEND_IMAGE_ID_REGEX_2"
        }
    }
}

/**
 * 接收消息时获取到的 [FriendImage]. 它可以直接获取下载链接 [originUrl]
 */
@PlannedRemoval("1.2.0") // 改为 internal
@Deprecated(
    ONLINE_OFFLINE_DEPRECATION_MESSAGE,
    level = DeprecationLevel.ERROR,
    replaceWith = ReplaceWith("Image", "net.mamoe.mirai.message.data.Image")
)
abstract class OnlineFriendImage : FriendImage(), OnlineImage

// endregion
