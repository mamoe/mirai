/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.data

public interface FriendGroups : Iterable<FriendGroup> {
    /**
     * 新建一个好友分组
     * @throws IllegalStateException 当创建不成功时抛出
     */
    public suspend fun create(name: String): FriendGroup

    /**
     * 删除一个好友分组分组
     * @return 返回 `false` 当分组不存在时
     * @throws IllegalStateException 当因为其他原因删除不成功时抛出
     */
    public suspend fun delete(friendGroup: FriendGroup): Boolean

    /**
     * 获取指定 ID 的好友分组, 不存在时返回 null
     */
    public operator fun get(id: Int): FriendGroup?
}