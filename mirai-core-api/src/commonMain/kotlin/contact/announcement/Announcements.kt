/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmBlockingBridge

package net.mamoe.mirai.contact.announcement

import kotlinx.coroutines.flow.Flow
import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.contact.PermissionDeniedException
import net.mamoe.mirai.utils.DeprecatedSinceMirai
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.NotStableForInheritance
import net.mamoe.mirai.utils.Streamable
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic


/**
 * 表示一个群的公告列表 (管理器).
 *
 * ## 获取群公告
 *
 * ### 获取 [Announcements] 实例
 *
 * 只可以通过 [Group.announcements] 获取一个群的公告列表, 即 [Announcements] 实例.
 *
 * ### 获取公告列表
 *
 * 通过 [asFlow] 或 `asStream` 可以获取到*惰性*流, 在从流中收集数据时才会请求服务器获取数据. 通常建议在 Kotlin 使用协程的 [asFlow], 在 Java 使用 `asStream`.
 *
 * 若要获取全部公告列表, 可使用 [toList].
 *
 * ## 发布群公告
 *
 * 查看 [Announcement]
 *
 * @since 2.7
 */
@NotStableForInheritance
public interface Announcements : Streamable<OnlineAnnouncement> {
    /**
     * 删除一条群公告. 需要管理员权限. 使用 [OnlineAnnouncement.delete] 与此方法效果相同.
     *
     * @param fid 公告的 [OnlineAnnouncement.fid]
     * @return 成功返回 `true`, 群公告不存在时返回 `false`
     *
     * @throws PermissionDeniedException 当没有权限时抛出
     * @throws IllegalStateException 当协议异常时抛出
     *
     * @see OnlineAnnouncement.delete
     */
    public suspend fun delete(fid: String): Boolean

    /**
     * 获取一条群公告.
     * @param fid 公告的 [OnlineAnnouncement.fid]
     * @return 返回 `null` 表示不存在该 [fid] 的群公告
     * @throws IllegalStateException 当协议异常时抛出
     */
    public suspend fun get(fid: String): OnlineAnnouncement?

    /**
     * 在该群发布群公告并获得 [OnlineAnnouncement], 需要管理员权限. 发布公告后群内将会出现 "有新公告" 系统提示.
     * @throws PermissionDeniedException 当没有权限时抛出
     * @throws IllegalStateException 当协议异常时抛出
     * @see Announcement.publishTo
     */
    public suspend fun publish(announcement: Announcement): OnlineAnnouncement

    /**
     * 上传资源作为群公告图片. 返回值可用于 [AnnouncementParameters.image].
     *
     * **注意**: 需要由调用方[关闭][ExternalResource.close] [resource].
     * @throws IllegalStateException 当协议异常时抛出
     */
    public suspend fun uploadImage(resource: ExternalResource): AnnouncementImage

    /**
     * 获取 已确认/未确认 的群成员
     *
     * @param fid 公告的 [OnlineAnnouncement.fid]
     * @param confirmed 是否确认
     * @return 群成员列表
     *
     * @throws PermissionDeniedException 当没有权限时抛出
     * @throws IllegalStateException 当协议异常时抛出
     *
     * @see OnlineAnnouncement.members
     * @since 2.14
     */
    public suspend fun members(fid: String, confirmed: Boolean): List<NormalMember>

    /**
     * 提醒 未确认 的群成员
     *
     * @param fid 公告的 [OnlineAnnouncement.fid]
     *
     * @throws PermissionDeniedException 当没有权限时抛出
     * @throws IllegalStateException 当协议异常时抛出
     *
     * @see OnlineAnnouncement.remind
     */
    public suspend fun remind(fid: String)

    // no blocking bridge for this method
    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("asFlow")
    @Deprecated(
        "Kept for binary compatibility. Use non-suspend overload instead.",
        level = DeprecationLevel.HIDDEN
    )
    @DeprecatedSinceMirai(hiddenSince = "2.13")
    @JvmSynthetic // no blocking bridge
    public suspend fun asFlow0(): Flow<OnlineAnnouncement> = asFlow()
}
