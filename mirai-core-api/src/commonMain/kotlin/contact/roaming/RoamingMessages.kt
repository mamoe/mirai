/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.contact.roaming

import kotlinx.coroutines.flow.Flow
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource

/**
 * 漫游消息记录管理器. 可通过 [RoamingSupported.roamingMessages] 获得.
 * 关于时间范围查询: [TimeBasedRoamingMessages]
 * 关于消息序列查询: [SeqBasedRoamingMessages]
 *
 *
 * @since 2.8
 * @see RoamingSupported
 */
public expect interface RoamingMessages {
    ///////////////////////////////////////////////////////////////////////////
    // Get list
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 查询所有漫游消息记录.
     *
     * 返回查询到的漫游消息记录, 顺序为由新到旧. 这些 [MessageChain] 与从事件中收到的消息链相似, 属于在线消息.
     * 可从 [MessageChain] 获取 [MessageSource] 来确定发送人等相关信息, 也可以进行引用回复或撤回.
     *
     * 注意, 返回的消息记录既包含机器人发送给目标用户的消息, 也包含目标用户发送给机器人的消息.
     * 可通过 [MessageChain] 获取 [MessageSource] (用法为 `messageChain.source`), 判断 [MessageSource.fromId] (发送人).
     * 消息的其他*元数据*信息也要通过 [MessageSource] 获取 (如 [MessageSource.time] 获取时间).
     *
     * 若只需要获取单向消息 (机器人发送给目标用户的消息或反之), 可使用 [RoamingMessageFilter.SENT] 或 [RoamingMessageFilter.RECEIVED] 作为 [filter] 参数传递.
     *
     * 性能提示: 请在 [filter] 执行筛选, 若 [filter] 返回 `false` 则不会解析消息链, 这对本函数的处理速度有决定性影响.
     *
     * @param filter 过滤器.
     */
    public open suspend fun getAllMessages(
        filter: RoamingMessageFilter? = null
    ): Flow<MessageChain>
}