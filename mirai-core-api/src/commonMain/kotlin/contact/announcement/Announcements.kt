/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("INAPPLICABLE_JVM_NAME")

package net.mamoe.mirai.contact.announcement

import kotlinx.coroutines.flow.Flow
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.PermissionDeniedException
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.NotStableForInheritance


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
 * 通过 [asFlow] 或 [asStream] 可以获取到*惰性*流, 在从流中收集数据时才会请求服务器获取数据. 通常建议在 Kotlin 使用协程的 [asFlow], 在 Java 使用 [asStream].
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
public expect interface Announcements {
    /**
     * 创建一个能获取该群内所有群公告列表的 [Flow]. 在 [Flow] 被使用时才会分页下载 [OnlineAnnouncement].
     *
     * 异常不会抛出, 只会记录到网络日志. 当获取发生异常时将会终止获取, 不影响已经成功获取的 [OfflineAnnouncement] 和 [Flow] 的[收集][Flow.collect].
     */
    public suspend fun asFlow(): Flow<OnlineAnnouncement>

    /**
     * 获取所有群公告列表, 将全部 [OnlineAnnouncement] 都下载后再返回.
     *
     * 异常不会抛出, 只会记录到网络日志. 当获取发生异常时将会终止获取并返回已经成功获取到的 [OfflineAnnouncement] 列表.
     *
     * @return 此时刻的群公告只读列表.
     */
    public open suspend fun toList(): List<OnlineAnnouncement>


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
}
