/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.data

import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge

/**
 * 好友分组列表 (管理器).
 * 运行存在重复名称的分组, 因此依赖于 name 判断不可靠, 需要依赖 ID 判断.
 *
 * @see FriendGroup
 * @since 2.13
 */
@JvmBlockingBridge
public interface FriendGroups : Iterable<FriendGroup> {
    /**
     * 新建一个好友分组.
     *
     * 允许名称重复, 当新建一个已存在名称的分组时, 服务器会返回一个拥有重复名字的新分组;
     * 当因为其他原因创建不成功时抛出 [IllegalStateException].
     */
    public suspend fun create(name: String): FriendGroup

    /**
     * 获取指定 ID 的好友分组, 不存在时返回 `null`
     */
    public operator fun get(id: Int): FriendGroup?
}