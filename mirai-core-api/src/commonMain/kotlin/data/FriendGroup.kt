/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.data

import net.mamoe.mirai.contact.Friend

/**
 * 好友分组.
 * 运行存在同名分组, 但是每个好友分组的 id 确保不一样.
 * @see FriendGroups
 */
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
     * 允许存在同名分组.
     * @return 当操作成功时返回 `true`; 当分组不存在时返回 `false`
     * @throws IllegalStateException 当因为其他原因改名不成功时抛出
     */
    public suspend fun renameTo(newName: String): Boolean

    /**
     * 把一名好友移动至本分组内.
     * 当远程分组不存在时会自动移动该好友到 ID 为 0 的默认好友分组.
     * @return 当操作成功时返回 `true`; 当分组不存在 (如已经在远程被删除) 时返回 `false`
     * @throws IllegalStateException 当因为其他原因移动不成功时抛出
     */
    public suspend fun moveIn(friend: Friend): Boolean

    /**
     * 删除本分组.
     * 删除后组内全部好友移动至 id 为 0 的默认好友分组.
     * @return 当操作成功时返回 `true`; 当分组不存在时返回 `false`
     * @throws IllegalStateException 当因为其他原因删除不成功时抛出
     */
    public suspend fun delete(): Boolean
}