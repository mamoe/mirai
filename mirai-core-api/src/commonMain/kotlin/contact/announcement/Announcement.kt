/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("INAPPLICABLE_JVM_NAME", "NOTHING_TO_INLINE")
@file:JvmBlockingBridge

package net.mamoe.mirai.contact.announcement

import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.PermissionDeniedException
import net.mamoe.mirai.contact.announcement.Announcement.Companion.publishAnnouncement
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import kotlin.jvm.JvmSynthetic


/**
 * 表示一个 (群) 公告.
 *
 * ## 公告类型
 *
 * [Announcement] 可以是 [OnlineAnnouncement] 或 [OfflineAnnouncement].
 *
 * - [OnlineAnnouncement] 表示从 [Announcements.get] 等途径在线获取的, 已经存在于服务器的公告.
 * - [OfflineAnnouncement] 表示在本地构建的 [Announcement].
 *
 * ## 发布公告
 *
 * ### 构造一条新公告并发布
 *
 * 构造 [OfflineAnnouncement] 然后调用其 [OfflineAnnouncement.publishTo] 或 [Announcements.publish].
 *
 * 在构造时可提供可选的 [AnnouncementParameters] 来设置一些附加属性.
 *
 * 也可以使用 [Announcement.publishAnnouncement] 扩展快捷创建并发布公告.
 *
 * ### 转发获取的公告到其他群
 *
 * 通过一个群的 [Announcements] 获取到 [OnlineAnnouncement], 然后调用 [OnlineAnnouncement.publishTo] 到另一个群即可.
 * 由于目前不支持获取公告的图片, 转发的公告也就不会带有原公告的图片.
 *
 * ## 序列化
 *
 * [OfflineAnnouncement] 支持 kotlinx-serialization 序列化, 可使用 serializer [OfflineAnnouncement.serializer].
 *
 * [OnlineAnnouncement] 无法序列化. 只能将其转为 [OfflineAnnouncement] 再序列化. 在 Kotlin 使用 [Announcement.toOffline], 在 Java 使用 [OfflineAnnouncement.from].
 *
 * @see Announcement
 *
 * @since 2.7
 */
public sealed interface Announcement {
    /**
     * 内容
     */
    public val content: String

    /**
     * 附加参数. 可以通过 [AnnouncementParametersBuilder] 构建获得.
     * @see AnnouncementParameters
     * @see AnnouncementParametersBuilder
     */
    public val parameters: AnnouncementParameters

    /**
     * 在该群发布群公告并获得 [OnlineAnnouncement], 需要管理员权限. 发布公告后群内将会出现 "有新公告" 系统提示.
     * @throws PermissionDeniedException 当没有权限时抛出
     * @throws IllegalStateException 当协议异常时抛出
     * @see Announcements.publish
     */
    public suspend fun publishTo(group: Group): OnlineAnnouncement = group.announcements.publish(this)

    public companion object {
        /**
         * 在该群发布群公告并获得 [OnlineAnnouncement], 需要管理员权限. 发布公告后群内将会出现 "有新公告" 系统提示.
         *
         * @param content 公告内容
         * @param parameters 可选的附加参数
         *
         * @throws PermissionDeniedException 当没有权限时抛出
         * @throws IllegalStateException 当协议异常时抛出
         *
         * @see OfflineAnnouncement
         * @see Announcement.publishTo
         * @see AnnouncementParametersBuilder
         */
        @JvmOverloads
        @JvmStatic
        public suspend fun Group.publishAnnouncement(
            content: String,
            parameters: AnnouncementParameters = AnnouncementParameters.DEFAULT
        ): OnlineAnnouncement = this.announcements.publish(OfflineAnnouncement(content, parameters))

        /**
         * 在该群发布群公告并获得 [OnlineAnnouncement], 需要管理员权限. 发布公告后群内将会出现 "有新公告" 系统提示.
         *
         * @param content 公告内容
         * @param parameters 可选的附加参数
         *
         * @throws PermissionDeniedException 当没有权限时抛出
         * @throws IllegalStateException 当协议异常时抛出
         *
         * @see OfflineAnnouncement
         * @see Announcement.publishTo
         * @see AnnouncementParametersBuilder
         */
        @JvmSynthetic
        public suspend inline fun Group.publishAnnouncement(
            content: String,
            parameters: AnnouncementParametersBuilder.() -> Unit
        ): OnlineAnnouncement {
            // contract { callsInPlace(parameters, InvocationKind.EXACTLY_ONCE) }
            // no contract and no inline: IDE fails to analyze this funciton
            return this.announcements.publish(OfflineAnnouncement(content, parameters))
        }
    }
}

/**
 * 创建 [OfflineAnnouncement]. 若 [this] 类型为 [OfflineAnnouncement] 则直接返回 [this].
 * @since 2.7
 */
public inline fun Announcement.toOffline(): OfflineAnnouncement = OfflineAnnouncement.from(this)


