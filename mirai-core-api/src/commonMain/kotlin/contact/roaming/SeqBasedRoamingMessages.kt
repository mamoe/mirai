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

public expect interface SeqBasedRoamingMessages:RoamingMessages {

    /**
     * 从 [from] 开始,向上查询 [count] 条消息记录.
     * @param filter 过滤器
     * @param from 起始消息位置
     * @param count 数目
     */
    public open suspend fun getMessagesIn(
        from: MessageSource,
        count: Int = 10,
        filter: RoamingMessageFilter? = null
    ): Flow<MessageChain>

    /**
     * Also see: [getMessagesIn]
     * 直接通过 seq 查找
     */
    public open suspend fun getMessagesIn(
        seq: Int,
        count: Int = 10,
        filter: RoamingMessageFilter? = null
    ): Flow<MessageChain>
}