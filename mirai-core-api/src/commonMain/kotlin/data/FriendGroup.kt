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
     * 更改好友分组名称
     * @throws IllegalStateException 当改名不成功时抛出
     */
    public suspend fun renameTo(newName: String): Boolean

    /**
     * 把一名好友移动至本分组内
     * @throws IllegalStateException 当移动不成功时
     */
    public suspend fun moveIn(friend: Friend): Boolean

    /**
     * 删除本分组
     * @return 返回 `false` 当分组不存在时
     * @throws IllegalStateException 当因为其他原因删除不成功时抛出
     */
    public suspend fun delete(): Boolean

    /**
     * 新建一个好友分组
     * todo for review, 我不太清楚这个方法应该放哪, 要为这个写个factory吗
     * @throws IllegalStateException 当创建不成功时
     */
    public suspend fun new(name: String): FriendGroup
}