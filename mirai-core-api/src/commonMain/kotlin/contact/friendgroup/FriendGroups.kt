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
import net.mamoe.mirai.utils.NotStableForInheritance

/**
 * 好友分组列表 (管理器).
 * 允许存在重复名称的分组, 因此依赖于 name 判断不可靠, 需要依赖 ID 判断.
 *
 * @see FriendGroup
 * @since 2.13
 */
@JvmBlockingBridge
@NotStableForInheritance
public interface FriendGroups {
    /**
     * 获取 [ID][FriendGroup.id] 为 `0` 的默认分组 ("我的好友").
     */
    public val default: FriendGroup get() = get(0) ?: error("Internal error: could not find FriendGroup with id = 0.")

    /**
     * 新建一个好友分组.
     *
     * 允许名称重复, 当新建一个已存在名称的分组时, 服务器会返回一个拥有重复名字的新分组;
     * 当因为其他原因创建不成功时抛出 [IllegalStateException].
     *
     * 提示: 要删除一个好友分组, 使用 [FriendGroup.delete].
     */
    public suspend fun create(name: String): FriendGroup

    /**
     * 获取指定 ID 的好友分组, 不存在时返回 `null`
     */
    public operator fun get(id: Int): FriendGroup?

    /**
     * 获取包含全部 [FriendGroup] 的 [Collection]. 返回的 [Collection] 只可读取.
     *
     * 此方法快速返回, 不会在调用时实例化新的 [Collection] 对象.
     * 返回的 [Collection] 是对缓存的引用, 会随着服务器通知和机器人操作 (如 [create]) 变化.
     */
    public fun asCollection(): Collection<FriendGroup>
}