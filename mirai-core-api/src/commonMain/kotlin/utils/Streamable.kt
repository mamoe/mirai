/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmBlockingBridge

package net.mamoe.mirai.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.contact.announcement.Announcement
import net.mamoe.mirai.contact.announcement.Announcements
import net.mamoe.mirai.utils.JdkStreamSupport.toStream
import java.util.stream.Stream
import kotlin.coroutines.EmptyCoroutineContext

/**
 * 表示一个可以创建数据流 [Flow] 和 [Stream] 的对象.
 *
 * 实现这个接口的对象可以看做为元素 [T] 的集合.
 * 例如 [Announcements] 可以看作是 [Announcement] 的集合,
 * 使用 [Announcements.asFlow] 可以获取到包含所有 [Announcement] 列表的 [Flow],
 * 使用 [Announcements.asStream] 可以获取到包含所有 [Announcement] 列表的 [Stream].
 *
 * @since 2.13
 */
public interface Streamable<T> {
    /**
     * 创建一个能获取 [T] 的 [Flow].
     */
    public fun asFlow(): Flow<T>

    /**
     * 创建一个能获取该群内所有 [T] 的 [Stream].
     *
     * 实现细节: 为了适合 Java 调用, 实现类似为阻塞式的 [asFlow], 因此不建议在 Kotlin 使用. 在 Kotlin 请使用 [asFlow].
     *
     * 注: 为了资源的正确释放, 使用 [Stream] 时需要使用 `try-with-resource`. 如
     *
     * ```java
     * Streamable<String> tmp;
     * try (var stream = tmp.asStream()) {
     *     System.out.println(stream.findFirst());
     * }
     * ```
     */
    public fun asStream(): Stream<T> = asFlow().toStream(
        context = if (this is CoroutineScope) this.coroutineContext else EmptyCoroutineContext,
    )

    /**
     * 获取所有 [T] 列表, 将全部 [T] 都加载后再返回.
     *
     * @return 此时刻的 [T] 只读列表.
     */
    public suspend fun toList(): List<T> = asFlow().toList()
}