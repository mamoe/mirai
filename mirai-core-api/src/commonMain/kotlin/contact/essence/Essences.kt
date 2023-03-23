/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.contact.essence

import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.utils.NotStableForInheritance
import net.mamoe.mirai.utils.Streamable

/**
 * 表示一个群精华消息管理.
 *
 * ## 获取 [Essences] 实例
 *
 * 只可以通过 [Group.essences] 获取一个群的精华消息管理, 即 [Essences] 实例.
 *
 * ### 获取精华消息列表
 *
 * 通过 [asFlow] 或 `asStream` 可以获取到*惰性*流, 在从流中收集数据时才会请求服务器获取数据. 通常建议在 Kotlin 使用协程的 [asFlow], 在 Java 使用 `asStream`.
 *
 * 若要获取全部精华消息列表, 可使用 [toList].
 *
 * ### 获取精华消息分享链接
 *
 * 通过 [share] 可以获得一个精华消息的分享链接
 *
 * ### 移除精华消息
 *
 * 通过 [remove] 可以从列表中移除指定精华消息 (WEB API)
 *
 * @since 2.15
 */
@NotStableForInheritance
public interface Essences : Streamable<EssenceMessageRecord> {

    /**
     * 按页获取精华消息记录
     * @param start 起始索引 从 0 开始
     * @param limit 页大小 返回的记录最大数量，最大取 50
     * @throws IllegalStateException [limit] 过大或其他参数错误时会触发异常
     */
    @JvmBlockingBridge
    public suspend fun getPage(start: Int, limit: Int): List<EssenceMessageRecord>

    /**
     * 分享精华消息
     * @param source 要分享的消息源
     * @throws IllegalStateException [source] 不为精华消息时将会触发异常
     * @return 分享 URL
     */
    @JvmBlockingBridge
    public suspend fun share(source: MessageSource): String

    /**
     * 移除精华消息
     * @throws IllegalStateException [source] 不为精华消息或权限不足时将会触发异常
     * @param source 要移除的消息源
     */
    @JvmBlockingBridge
    public suspend fun remove(source: MessageSource)
}