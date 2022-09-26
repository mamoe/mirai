/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmBlockingBridge

package net.mamoe.mirai.utils

import kotlinx.coroutines.flow.Flow
import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.contact.announcement.Announcements
import net.mamoe.mirai.contact.announcement.OnlineAnnouncement

/**
 * 表示一个可以创建[数据流][Flow]的对象.
 *
 * 实现这个接口的对象可以看做为元素 [T] 的集合.
 * 例如 [Announcements] 可以看作是 [OnlineAnnouncement] 的集合,
 * 使用 [Announcements.asFlow] 可以获取到包含所有 [OnlineAnnouncement] 列表的 [Flow]
 * 在 JVM, 还可以使用 `Announcements.asStream` 可以获取到包含所有 [OnlineAnnouncement] 列表的 `Stream`.
 *
 * @since 2.13
 */
public expect interface Streamable<T> {
    /**
     * 创建一个能获取 [T] 的 [Flow].
     */
    public fun asFlow(): Flow<T>

    /**
     * 获取所有 [T] 列表, 将全部 [T] 都加载后再返回.
     *
     * @return 此时刻的 [T] 只读列表.
     */
    public open suspend fun toList(): List<T>
}