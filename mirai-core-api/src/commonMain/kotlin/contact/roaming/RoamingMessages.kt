/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmBlockingBridge

package net.mamoe.mirai.contact.roaming

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import net.mamoe.kjbb.JvmBlockingBridge
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.utils.BotConfiguration
import java.util.stream.Stream

/**
 * 漫游消息记录管理器.
 *
 * @since 2.8
 * @see RoamingSupported
 */
public interface RoamingMessages {
    ///////////////////////////////////////////////////////////////////////////
    // Get list
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 查询指定时间段内的漫游消息记录. 将会调用 [BotConfiguration.roamingAuthenticator] 获取漫游消息独立密码.
     *
     * 返回查询到的漫游消息记录, 顺序为由新到旧. 这些 [MessageChain] 与从事件中收到的消息链相似, 属于在线消息.
     * 可从 [MessageChain] 获取 [MessageSource] 来确定发送人等相关信息, 也可以进行引用回复或撤回.
     *
     * 性能提示: 请在 [filter] 执行筛选, 若 [filter] 返回 `false` 则不会解析消息链, 这对本函数的处理速度有决定性影响.
     *
     * @param timeStart 起始时间, UTC+8 时间戳, 单位为秒. 可以为 `0`, 即表示从可以获取的最早的消息起. 负数将会被看是 `0`.
     * @param timeEnd 结束时间, UTC+8 时间戳, 单位为秒. 可以为 [Long.MAX_VALUE], 即表示到可以获取的最晚的消息为止. 低于 [timeStart] 的值将会被看作是 [timeStart] 的值.
     * @param filter 过滤器.
     */
    public suspend fun getMessagesIn(
        timeStart: Long,
        timeEnd: Long,
        filter: RoamingMessageFilter? = null
    ): Flow<MessageChain>

    /**
     * 查询所有漫游消息记录. 将会调用 [BotConfiguration.roamingAuthenticator] 获取漫游消息独立密码.
     *
     * 返回查询到的漫游消息记录, 顺序为由新到旧. 这些 [MessageChain] 与从事件中收到的消息链相似, 属于在线消息.
     * 可从 [MessageChain] 获取 [MessageSource] 来确定发送人等相关信息, 也可以进行引用回复或撤回.
     *
     * 性能提示: 请在 [filter] 执行筛选, 若 [filter] 返回 `false` 则不会解析消息链, 这对本函数的处理速度有决定性影响.
     *
     * @param filter 过滤器.
     */
    public suspend fun getAllMessages(
        filter: RoamingMessageFilter? = null
    ): Flow<MessageChain> = getMessagesIn(0, Long.MAX_VALUE, filter)

    /**
     * 查询指定时间段内的漫游消息记录. 将会调用 [BotConfiguration.roamingAuthenticator] 获取漫游消息独立密码.
     *
     * 返回查询到的漫游消息记录, 顺序为由新到旧. 这些 [MessageChain] 与从事件中收到的消息链相似, 属于在线消息.
     * 可从 [MessageChain] 获取 [MessageSource] 来确定发送人等相关信息, 也可以进行引用回复或撤回.
     *
     * 性能提示: 请在 [filter] 执行筛选, 若 [filter] 返回 `false` 则不会解析消息链, 这对本函数的处理速度有决定性影响.
     *
     * @param timeStart 起始时间, UTC+8 时间戳, 单位为秒. 可以为 `0`, 即表示从可以获取的最早的消息起. 负数将会被看是 `0`.
     * @param timeEnd 结束时间, UTC+8 时间戳, 单位为秒. 可以为 [Long.MAX_VALUE], 即表示到可以获取的最晚的消息为止. 低于 [timeStart] 的值将会被看作是 [timeStart] 的值.
     * @param filter 过滤器.
     */
    @Suppress("OVERLOADS_INTERFACE")
    @JvmOverloads
    public suspend fun getMessagesStream(
        timeStart: Long,
        timeEnd: Long,
        filter: RoamingMessageFilter? = null
    ): Stream<MessageChain>

    /**
     * 查询所有漫游消息记录. 将会调用 [BotConfiguration.roamingAuthenticator] 获取漫游消息独立密码.
     *
     * 返回查询到的漫游消息记录, 顺序为由新到旧. 这些 [MessageChain] 与从事件中收到的消息链相似, 属于在线消息.
     * 可从 [MessageChain] 获取 [MessageSource] 来确定发送人等相关信息, 也可以进行引用回复或撤回.
     *
     * 性能提示: 请在 [filter] 执行筛选, 若 [filter] 返回 `false` 则不会解析消息链, 这对本函数的处理速度有决定性影响.
     *
     * @param filter 过滤器.
     */
    @Suppress("OVERLOADS_INTERFACE")
    @JvmOverloads
    public suspend fun getAllMessagesStream(
        filter: RoamingMessageFilter? = null
    ): Stream<MessageChain> = getMessagesStream(0, Long.MAX_VALUE, filter)

    ///////////////////////////////////////////////////////////////////////////
    // Get single
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 精确获取一条消息. 将会调用 [BotConfiguration.roamingAuthenticator] 获取漫游消息独立密码.
     *
     * 若该消息已过期, 返回 `null`.
     *
     * @param id [MessageSource.ids]
     * @param internalId [MessageSource.internalIds]
     * @param time [MessageSource.time] 服务器时间
     */
    public suspend fun getMessage(id: Int, internalId: Int, time: Long): MessageChain? {
        return getMessagesIn(time, time) { message ->
            id in message.ids && internalId in message.internalIds
        }.firstOrNull()
    }
}