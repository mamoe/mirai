/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmBlockingBridge
@file:Suppress("INAPPLICABLE_JVM_NAME")

package net.mamoe.mirai.contact.announcement

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import net.mamoe.kjbb.JvmBlockingBridge
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.PermissionDeniedException
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.NotStableForInheritance
import java.util.stream.Stream


/**
 * 表示一个群的公告列表 (管理器).
 *
 * @since 2.7
 */
@NotStableForInheritance
public interface Announcements {
    /**
     * 创建一个能获取该群内所有群公告列表的 [Flow]. 在 [Flow] 被使用时才会分页下载 [OnlineAnnouncement].
     */
    public suspend fun asFlow(): Flow<OnlineAnnouncement>

    /**
     * 创建一个能获取该群内所有群公告列表的 [Stream]. 在 [Stream] 被使用时才会分页下载 [OnlineAnnouncement].
     */
    public fun asStream(): Stream<OnlineAnnouncement>

    /**
     * 获取所有群公告列表, 将全部 [OnlineAnnouncement] 都下载后再返回.
     * @return 此时刻的群公告列表
     */
    public suspend fun toList(): List<OnlineAnnouncement> = asFlow().toList()


    /**
     * 删除一条群公告. 需要管理员权限. 使用 [OnlineAnnouncement.delete] 与此方法效果相同.
     *
     * @param fid 公告的id [OnlineAnnouncement.fid]
     * @return 成功返回 `true`, 群公告不存在时返回 `false`
     *
     * @throws PermissionDeniedException 当没有权限时抛出
     *
     * @see OnlineAnnouncement.delete
     */
    public suspend fun delete(fid: String): Boolean

    /**
     * 获取一条群公告.
     * @param fid 公告的id [OnlineAnnouncement.fid]
     * @return 返回 `null` 表示不存在该 [fid] 的群公告
     */
    public suspend fun get(fid: String): OnlineAnnouncement?

    /**
     * 在该群发布群公告并获得 [OnlineAnnouncement].
     * @throws PermissionDeniedException 当没有权限时抛出
     */
    public suspend fun publish(announcement: Announcement): OnlineAnnouncement

    /**
     * 上传群公告图片
     *
     * 注意: 需要由调用方关闭 [resource].
     */
    public suspend fun uploadImage(resource: ExternalResource): AnnouncementImage
}
