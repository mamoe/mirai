/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE")

package net.mamoe.mirai.contact.announcement

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.mamoe.mirai.contact.announcement.OfflineAnnouncement.Companion.serializer
import net.mamoe.mirai.utils.cast
import net.mamoe.mirai.utils.map
import net.mamoe.mirai.utils.safeCast
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * 表示在本地构建的 [Announcement].
 *
 * 支持序列化, 使用 [serializer].
 *
 * 可以通过 [OfflineAnnouncement], [OfflineAnnouncement.create] 等方法构建, 然后使用 [OfflineAnnouncement.publishTo] 或 [Announcements.publish] 发布公告到群.
 *
 * 在 [Announcement] 获取更多信息.
 *
 * @see OnlineAnnouncement.publishTo
 *
 * @since 2.7
 */
@Serializable(OfflineAnnouncement.Companion.Serializer::class)
@SerialName(OfflineAnnouncement.SERIAL_NAME)
public sealed interface OfflineAnnouncement : Announcement {
    public companion object {
        public const val SERIAL_NAME: String = "OfflineAnnouncement"

        /**
         * 创建 [OfflineAnnouncement]. 若 [announcement] 类型为 [OfflineAnnouncement] 则直接返回 [announcement].
         *
         * 若要转发获取到的公告到一个群, 可直接调用 [Announcement.publishTo] 而不需要构造 [OfflineAnnouncement].
         *
         * @see OnlineAnnouncement.toOffline
         */
        @JvmStatic
        public inline fun from(announcement: Announcement): OfflineAnnouncement =
            announcement.safeCast() ?: announcement.run { create(content, parameters) }

        /**
         * 创建 [OfflineAnnouncement].
         * @param content 公告内容
         * @param parameters 可选的附加参数
         */
        @JvmOverloads
        @JvmStatic
        public fun create(
            content: String,
            parameters: AnnouncementParameters = AnnouncementParameters.DEFAULT
        ): OfflineAnnouncement = OfflineAnnouncementImpl(content, parameters)

        /**
         * 创建 [AnnouncementParameters] 并创建 [OfflineAnnouncement].
         * @param content 公告内容
         * @param parameters 可选的附加参数
         * @see AnnouncementParametersBuilder
         */
        @JvmSynthetic
        public inline fun create(
            content: String,
            parameters: AnnouncementParametersBuilder.() -> Unit
        ): OfflineAnnouncement {
            contract { callsInPlace(parameters, InvocationKind.EXACTLY_ONCE) }
            return create(content, buildAnnouncementParameters(parameters))
        }

        internal object Serializer : KSerializer<OfflineAnnouncement> by OfflineAnnouncementImpl.serializer().map(
            resultantDescriptor = OfflineAnnouncementImpl.serializer().descriptor,
            deserialize = { it },
            serialize = { it.safeCast<OfflineAnnouncementImpl>() ?: create(content, parameters).cast() }
        )
    }
}

/**
 * 依据 [from] 创建 [OfflineAnnouncement]. 若 [from] 类型为 [OfflineAnnouncement] 则直接返回 [from].
 * @since 2.7
 */
public inline fun OfflineAnnouncement(from: Announcement): OfflineAnnouncement =
    OfflineAnnouncement.from(from)

/**
 * 创建 [AnnouncementParameters] 并创建 [OfflineAnnouncement].
 * @param content 公告内容
 * @param parameters 可选的附加参数
 * @since 2.7
 */
public inline fun OfflineAnnouncement(
    content: String,
    parameters: AnnouncementParameters = AnnouncementParameters.DEFAULT
): OfflineAnnouncement = OfflineAnnouncement.create(content, parameters)

/**
 * 创建 [AnnouncementParameters] 并创建 [OfflineAnnouncement].
 * @param content 公告内容
 * @param parameters 可选的附加参数
 * @see AnnouncementParametersBuilder
 * @since 2.7
 */
public inline fun OfflineAnnouncement(
    content: String,
    parameters: AnnouncementParametersBuilder.() -> Unit
): OfflineAnnouncement {
    contract { callsInPlace(parameters, InvocationKind.EXACTLY_ONCE) }
    return OfflineAnnouncement.create(content, parameters)
}

@SerialName(OfflineAnnouncement.SERIAL_NAME)
@Serializable
private data class OfflineAnnouncementImpl(
    override val content: String,
    override val parameters: AnnouncementParameters
) : OfflineAnnouncement {
    override fun toString() = "OfflineAnnouncement(body='$content', parameters=$parameters)"
}