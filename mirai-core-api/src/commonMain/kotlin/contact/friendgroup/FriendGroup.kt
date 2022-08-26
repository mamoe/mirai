/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.contact.friendgroup

import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.utils.NotStableForInheritance

/**
 * 一个好友分组.
 * 可能同时存在多个相同[名称][name]的分组, 但是每个分组的 [id] 都是唯一的.
 *
 *
 * 要获取一个分组, 可使用 [get] 根据 [ID][FriendGroup.id] 获取, 或者使用 [asCollection] 获取全部分组列表.
 * 也可以通过 [Friend.friendGroup] 获取一个好友所在的分组.
 *
 * 在每次登录会话中, [FriendGroup] 的实例是依据 [id] 唯一的. 存在于同一个分组中的多个好友的 [Friend.friendGroup] 会返回相同的 [FriendGroup] 实例.
 * 但当 bot 重新登录后, 实例可能变化.
 *
 * @see FriendGroups
 * @since 2.13
 */
@JvmBlockingBridge
@NotStableForInheritance
public interface FriendGroup {
    /**
     * 好友分组 ID
     */
    public val id: Int

    /**
     * 好友分组名
     */
    public val name: String

    /**
     * 好友分组内好友数量
     */
    public val count: Int

    /**
     * 属于本分组的好友集合
     */
    public val friends: Collection<Friend>

    /**
     * 更改好友分组名称.
     *
     * 允许存在同名分组.
     * 当操作成时返回 `true`; 当分组不存在时返回 `false`; 如果因为其他原因造成的改名失败时会抛出 [IllegalStateException]
     */
    public suspend fun renameTo(newName: String): Boolean

    /**
     * 把一名好友移动至本分组内.
     *
     * 当远程分组不存在时会自动移动该好友到 ID 为 0 的默认好友分组.
     * 当操作成功时返回 `true`; 当分组不存在 (如已经在远程被删除) 时返回 `false`; 因为其他原因移动不成功时抛出 [IllegalStateException].
     */
    public suspend fun moveIn(friend: Friend): Boolean

    /**
     * 删除本分组.
     *
     * 删除后组内全部好友移动至 ID 为 0 的默认好友分组, 本分组的好友列表会被清空.
     * 当操作成功时返回 `true`; 当分组不存在或试图删除 ID 为 0 的默认好友分组时返回 `false`;
     * 因为其他原因删除不成功时抛出 [IllegalStateException].
     */
    public suspend fun delete(): Boolean
}